"""
Tools Service
- Summarizer     : BART/T5 via HuggingFace transformers
- Mind Map       : spaCy entity/noun extraction → graph nodes
- Difficulty     : textstat Flesch-Kincaid
- Plagiarism     : SBERT sentence-transformers cosine similarity
- OCR            : Tesseract via pytesseract
- Keywords       : YAKE unsupervised keyword extraction
"""

import logging
import re
from typing import List

import spacy
import textstat
import yake
import pytesseract
from PIL import Image
import io

from transformers import pipeline
from sentence_transformers import SentenceTransformer, util

logger = logging.getLogger(__name__)

# ── Load models once ──────────────────────────────────────────
_nlp              = None
_summarizer       = None
_sbert_model      = None

def _get_nlp():
    global _nlp
    if _nlp is None:
        _nlp = spacy.load("en_core_web_sm")
    return _nlp

def _get_summarizer():
    global _summarizer
    if _summarizer is None:
        logger.info("Loading summarizer model...")
        _summarizer = pipeline("summarization", model="sshleifer/distilbart-cnn-12-6")
    return _summarizer

def _get_sbert():
    global _sbert_model
    if _sbert_model is None:
        logger.info("Loading SBERT model...")
        _sbert_model = SentenceTransformer("all-MiniLM-L6-v2")
    return _sbert_model


# ── Summarizer ────────────────────────────────────────────────
def summarize(text: str) -> dict:
    """Summarize long text using DistilBART."""
    if len(text) < 200:
        return {"summary": text, "key_points": []}

    try:
        summarizer = _get_summarizer()
        # BART max input is ~1024 tokens, chunk if needed
        chunk   = text[:3000]
        result  = summarizer(chunk, max_length=200, min_length=50, do_sample=False)
        summary = result[0]["summary_text"]

        # Extract key sentences as bullet points
        sentences  = [s.strip() for s in text.split(".") if len(s.strip()) > 30]
        key_points = sentences[:5]

        return {"summary": summary, "key_points": key_points}
    except Exception as e:
        logger.error("Summarization failed: %s", e)
        return {"summary": text[:500] + "...", "key_points": []}


# ── Mind Map ──────────────────────────────────────────────────
def generate_mind_map(text: str, title: str = "Main Topic") -> dict:
    """
    Extract entities and noun phrases → build mind map nodes/edges.
    Returns: {nodes: [...], edges: [...]}
    """
    nlp  = _get_nlp()
    doc  = nlp(text[:5000])

    nodes = [{"id": 0, "label": title, "group": "root"}]
    edges = []
    seen  = set()
    idx   = 1

    # Named entities
    for ent in doc.ents:
        label = ent.text.strip()
        if label in seen or len(label) < 3:
            continue
        seen.add(label)
        nodes.append({"id": idx, "label": label, "group": ent.label_})
        edges.append({"from": 0, "to": idx})
        idx += 1
        if idx > 25:
            break

    # Noun chunks if we need more nodes
    if idx < 10:
        for chunk in doc.noun_chunks:
            label = chunk.text.strip()
            if label in seen or len(label) < 4:
                continue
            seen.add(label)
            nodes.append({"id": idx, "label": label, "group": "concept"})
            edges.append({"from": 0, "to": idx})
            idx += 1
            if idx > 25:
                break

    return {"nodes": nodes, "edges": edges}


# ── Difficulty Score ──────────────────────────────────────────
def get_difficulty(text: str) -> dict:
    """
    Flesch-Kincaid readability score.
    Score: 0-30 = Very Hard, 30-50 = Hard, 50-70 = Medium, 70+ = Easy
    """
    flesch = textstat.flesch_reading_ease(text)
    grade  = textstat.flesch_kincaid_grade(text)

    if flesch >= 70:
        label = "Easy"
    elif flesch >= 50:
        label = "Medium"
    elif flesch >= 30:
        label = "Hard"
    else:
        label = "Very Hard"

    return {
        "score":        round(flesch, 2),
        "grade_level":  round(grade, 1),
        "label":        label,
        "flesch_score": round(flesch, 2),
    }


# ── Plagiarism Check ──────────────────────────────────────────
def check_plagiarism(text1: str, text2: str) -> dict:
    """
    Compare two texts using SBERT cosine similarity.
    Score 0.0–1.0 (>0.8 likely plagiarised)
    """
    model = _get_sbert()
    emb1  = model.encode(text1[:2000], convert_to_tensor=True)
    emb2  = model.encode(text2[:2000], convert_to_tensor=True)
    score = float(util.cos_sim(emb1, emb2)[0][0])

    return {
        "similarity_score": round(score, 3),
        "is_plagiarised":   score > 0.80,
        "details":          f"Cosine similarity: {score:.1%}. {'High similarity detected.' if score > 0.80 else 'Texts appear original.'}",
    }


# ── OCR ───────────────────────────────────────────────────────
def run_ocr(image_bytes: bytes, language: str = "eng") -> dict:
    """
    Extract text from image/scanned PDF using Tesseract.
    language: 'eng' | 'hin' | 'tam' etc.
    """
    try:
        image = Image.open(io.BytesIO(image_bytes))
        text  = pytesseract.image_to_string(image, lang=language)
        return {"text": text.strip(), "language": language}
    except Exception as e:
        logger.error("OCR failed: %s", e)
        return {"text": "", "language": language}


# ── Keyword Extraction ────────────────────────────────────────
def extract_keywords(text: str, count: int = 10) -> List[str]:
    """
    Extract top keywords using YAKE (unsupervised, fast).
    """
    extractor = yake.KeywordExtractor(
        lan="en", n=2, dedupLim=0.7, top=count
    )
    keywords = extractor.extract_keywords(text[:3000])
    # YAKE returns (keyword, score) — lower score = more relevant
    return [kw for kw, score in sorted(keywords, key=lambda x: x[1])[:count]]
