
import json
from fastapi import APIRouter, HTTPException
from models.schemas import QuizGenerateRequest, QuizGenerateResponse, QuizEvaluateRequest, QuizEvaluateResponse
from services import quiz_service
from pathlib import Path
FAISS_DIR = Path("faiss_indices")

router = APIRouter()


@router.post("/generate", response_model=QuizGenerateResponse)
def generate_quiz(req: QuizGenerateRequest):
    index_path = FAISS_DIR / f"note_{req.note_id}"
    if not index_path.exists():
        raise HTTPException(status_code=404, detail="Note not indexed yet. Upload and wait for processing.")

    from langchain_community.vectorstores import FAISS
    from langchain_community.embeddings import OllamaEmbeddings
    from config import get_settings
    settings = get_settings()

    embeddings  = OllamaEmbeddings(model=settings.ollama_embed_model, base_url=settings.ollama_base_url)
    vectorstore = FAISS.load_local(str(index_path), embeddings, allow_dangerous_deserialization=True)
    docs        = vectorstore.similarity_search("main concepts", k=10)
    text        = "\n".join(d.page_content for d in docs)

    questions = quiz_service.generate_questions(text, req.count, req.type)
    return {"questions": questions}


@router.post("/evaluate", response_model=QuizEvaluateResponse)
def evaluate_quiz(req: QuizEvaluateRequest):
    """Evaluate user answers and return score + feedback."""
    questions_json = json.dumps(req.questions) if not isinstance(req.questions, str) else req.questions
    result         = quiz_service.evaluate_answers(questions_json, req.answers)
    return result
