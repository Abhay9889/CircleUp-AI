from fastapi import APIRouter,HTTPException
from typing import List
from models.schemas import FlashcardGenerateRequest,FlashcardPair
from services import flashcard_ai_service as flashcard_service
#from services.rag_service import FAISS_DIR
router=APIRouter()
from services import rag_service
FAISS_DIR = rag_service.FAISS_DIR

@router.post("/generate",response_model=List[FlashcardPair])

def generate_flashcards(req:FlashcardGenerateRequest):
    index_path=FAISS_DIR/f"note_{req.note_id}"
    if not index_path.exists():
        raise HTTPException(status_code=404,detail="Note not indexed yet..")
    
    from langchain_community.vectorstores import FAISS
    from langchain_community.embeddings import OllamaEmbeddings
    from config import get_settings

    settings=get_settings()

    embeddings=OllamaEmbeddings(model=settings.ollama_embed_model,base_url=settings.ollama_base_url)
    vectorstore=FAISS.load_local(str(index_path),embeddings,allow_dangerous_deserialization=True)
    docs=vectorstore.similarity_search("Key concept defination ",k=12)
    text="\n".join(d.page_content for d in docs)
    cards=flashcard_service.genrate_flashcards(text,req.count)
    return cards