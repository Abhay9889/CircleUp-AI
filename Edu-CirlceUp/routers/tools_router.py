
from fastapi import APIRouter, UploadFile, File, HTTPException
from models.schemas import (
    SummarizeRequest, SummarizeResponse,
    MindMapRequest, MindMapResponse,
    PlagiarismRequest, PlagiarismResponse,
    DifficultyResponse,
)
from services import tools_service
from db import redis_cache
from pathlib import Path
FAISS_DIR = Path("faiss_indices")

router = APIRouter()


def _load_note_text(note_id: int) -> str:
    index_path = FAISS_DIR / f"note_{note_id}"
    if not index_path.exists():
        raise HTTPException(status_code=404, detail="Note not indexed yet.")

    from langchain_community.vectorstores import FAISS
    from langchain_community.embeddings import OllamaEmbeddings
    from config import get_settings
    settings    = get_settings()
    embeddings  = OllamaEmbeddings(model=settings.ollama_embed_model, base_url=settings.ollama_base_url)
    vectorstore = FAISS.load_local(str(index_path), embeddings, allow_dangerous_deserialization=True)
    docs        = vectorstore.similarity_search("summary key concepts", k=15)
    return "\n".join(d.page_content for d in docs)


@router.post("/summarize", response_model=SummarizeResponse)
def summarize(req: SummarizeRequest):
    cache_key = f"summary:{req.note_id}"
    cached    = redis_cache.get(cache_key)
    if cached:
        return cached

    text   = _load_note_text(req.note_id)
    result = tools_service.summarize(text)
    redis_cache.set(cache_key, result, ttl_seconds=86400)  # 24h cache
    return result


@router.post("/mindmap", response_model=MindMapResponse)
def mindmap(req: MindMapRequest):
    cache_key = f"mindmap:{req.note_id}"
    cached    = redis_cache.get(cache_key)
    if cached:
        return cached

    text   = _load_note_text(req.note_id)
    result = tools_service.generate_mind_map(text)
    redis_cache.set(cache_key, result, ttl_seconds=86400)
    return result


@router.get("/difficulty/{note_id}", response_model=DifficultyResponse)
def difficulty(note_id: int):
    cache_key = f"difficulty:{note_id}"
    cached    = redis_cache.get(cache_key)
    if cached:
        return cached

    text   = _load_note_text(note_id)
    result = tools_service.get_difficulty(text)
    redis_cache.set(cache_key, result, ttl_seconds=86400)
    return result


@router.post("/plagiarism", response_model=PlagiarismResponse)
def plagiarism(req: PlagiarismRequest):
    return tools_service.check_plagiarism(req.text1, req.text2)


@router.post("/ocr")
async def ocr(
        image: UploadFile = File(...),
        language: str     = "eng"):
    """Extract text from uploaded image using Tesseract OCR."""
    try:
        image_bytes = await image.read()
        result      = tools_service.run_ocr(image_bytes, language)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/ocr/{note_id}")
def ocr_by_note_id(note_id: int):
    """Placeholder — OCR by note_id (file fetched from MinIO in full impl)."""
    return {"text": "", "note_id": note_id, "message": "Upload image directly to /tools/ocr"}
