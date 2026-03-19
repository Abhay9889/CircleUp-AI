"""Plagiarism detection using sentence embeddings cosine similarity"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer, util
import httpx
from config import get_settings

settings = get_settings()
router   = APIRouter()

# Load model once at import time
_model = SentenceTransformer("all-MiniLM-L6-v2")


class PlagiarismRequest(BaseModel):
    note_id_1: int
    note_id_2: int


class TextPlagiarismRequest(BaseModel):
    text1: str
    text2: str


async def fetch_note_text(note_id: int) -> str:
    async with httpx.AsyncClient(timeout=30) as client:
        resp = await client.get(f"http://springboot:8080/api/notes/{note_id}/text")
        return resp.json().get("text", "")


def similarity_score(text1: str, text2: str) -> float:
    """Returns cosine similarity 0.0 - 1.0"""
    if not text1 or not text2:
        return 0.0
    emb1 = _model.encode(text1[:3000], convert_to_tensor=True)
    emb2 = _model.encode(text2[:3000], convert_to_tensor=True)
    return float(util.cos_sim(emb1, emb2)[0][0])


@router.post("/check")
async def check_plagiarism(req: PlagiarismRequest):
    """Compare two notes for similarity"""
    text1, text2 = await fetch_note_text(req.note_id_1), await fetch_note_text(req.note_id_2)

    if not text1 or not text2:
        raise HTTPException(status_code=400, detail="One or both notes have no text")

    score = similarity_score(text1, text2)
    level = (
        "very_high" if score >= 0.85 else
        "high"      if score >= 0.70 else
        "medium"    if score >= 0.50 else
        "low"
    )

    return {
        "similarity":  round(score, 4),
        "percentage":  round(score * 100, 1),
        "level":       level,
        "note_id_1":   req.note_id_1,
        "note_id_2":   req.note_id_2,
    }


@router.post("/check-text")
async def check_text_plagiarism(req: TextPlagiarismRequest):
    """Compare raw text snippets directly"""
    score = similarity_score(req.text1, req.text2)
    return {
        "similarity": round(score, 4),
        "percentage": round(score * 100, 1),
        "level":      "high" if score >= 0.70 else "low",
    }