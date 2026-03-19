
from fastapi import APIRouter, HTTPException, UploadFile, File, Form
from models.schemas import AskRequest, AskResponse
from services import file_parser_service, rag_service, tools_service
from db import redis_cache

router = APIRouter()


# @router.post("/ask", response_model=AskResponse)
# def ask(req: AskRequest):
#     cache_key = f"rag:{req.note_id}:{hash(req.question)}:{req.language}"
#     cached    = redis_cache.get(cache_key)
#     if cached:
#         return cached

#     result = rag_service.ask(req.note_id, req.question, req.language)

#     if req.use_web_search:
#         from services.search_service import brave_search
#         web_results = brave_search(req.question, count=3)
#         result["web_results"] = web_results

#     redis_cache.set(cache_key, result, ttl_seconds=1800)
#     return result

# REPLACE with:
@router.post("/ask")
async def ask(req: dict):
    note_id  = req.get("note_id")
    question = req.get("question", "")
    language = req.get("language", "english")

    if not note_id or not question:
        raise HTTPException(status_code=400, detail="note_id and question are required")

    try:
        result = rag_service.ask(          # ← 'ask' not 'answer_question'
            note_id=int(note_id),
            question=question,
            language=language
        )
        return result
    except Exception as e:
        return {"answer": f"Could not answer: {str(e)}", "sources": []}

@router.post("/index/{note_id}")
async def index_note(
        note_id: int,
        file: UploadFile = File(...),
        file_type: str   = Form(...)):
    try:
        file_bytes = await file.read()
        
        # Try file_parser_service first, fall back to raw decode
        try:
            text = file_parser_service.extract_text(file_bytes, file_type)
        except Exception:
            text = ""
        
        # Fallback: decode bytes directly for text files
        if not text or not text.strip():
            for enc in ("utf-8", "utf-16", "latin-1"):
                try:
                    text = file_bytes.decode(enc).strip()
                    if text:
                        break
                except Exception:
                    continue

        if not text or not text.strip():
            raise HTTPException(status_code=422, detail="Could not extract text from file")

        rag_service.index_note(note_id, text)
        keywords   = tools_service.extract_keywords(text, count=10) if hasattr(tools_service, 'extract_keywords') else []
        difficulty = tools_service.get_difficulty(text) if hasattr(tools_service, 'get_difficulty') else 0.0

        return {
            "note_id":    note_id,
            "indexed":    True,
            "keywords":   keywords,
            "difficulty": difficulty,
            "char_count": len(text),
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
@router.delete("/index/{note_id}")
def delete_index(note_id: int):
    """Remove FAISS index when note is deleted."""
    rag_service.delete_index(note_id)
    return {"deleted": True, "note_id": note_id}
