import logging
import shutil
from pathlib import Path

from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_core.documents import Document
from langchain_core.prompts import PromptTemplate

from config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

FAISS_DIR = Path("faiss_indices")
FAISS_DIR.mkdir(exist_ok=True)

__all__ = ["index_note", "ask", "delete_index", "FAISS_DIR"]

splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)

RAG_PROMPT = PromptTemplate(
    input_variables=["context", "question"],
    template="""You are an intelligent study assistant.
Answer the question using ONLY the context below.
If you cannot answer from the context, say so clearly.

Context:
{context}

Question: {question}

Answer (be clear and educational):"""
)

_embeddings = None
_llm = None


def _get_embeddings():
    global _embeddings
    if _embeddings is None:
        from langchain_ollama import OllamaEmbeddings
        _embeddings = OllamaEmbeddings(
            model=settings.ollama_embed_model,
            base_url=settings.ollama_base_url
        )
    return _embeddings


def _get_llm():
    global _llm
    if _llm is None:
        from langchain_ollama import OllamaLLM
        _llm = OllamaLLM(
            model=settings.ollama_model,
            base_url=settings.ollama_base_url,
            temperature=0.3,
        )
    return _llm


def index_note(note_id: int, text: str) -> str:
    chunks = splitter.split_text(text)
    docs = [
        Document(page_content=c, metadata={"note_id": note_id, "chunk": i})
        for i, c in enumerate(chunks)
    ]
    from langchain_community.vectorstores import FAISS
    vectorstore = FAISS.from_documents(docs, _get_embeddings())
    index_path = str(FAISS_DIR / f"note_{note_id}")
    vectorstore.save_local(index_path)
    logger.info("Indexed note %d - %d chunks", note_id, len(chunks))
    return index_path


def ask(note_id: int, question: str, language: str = "english") -> dict:
    index_path = FAISS_DIR / f"note_{note_id}"

    try:
        llm = _get_llm()

        if not index_path.exists():
            logger.warning("Note %d not indexed - using general LLM fallback", note_id)
            answer = llm.invoke(
                f"You are a helpful study assistant. Answer this question clearly: {question}"
            )
            return {"answer": answer, "sources": [], "note": "General answer (note not indexed yet)"}

        from langchain_community.vectorstores import FAISS
        vectorstore = FAISS.load_local(
            str(index_path),
            _get_embeddings(),
            allow_dangerous_deserialization=True
        )
        retriever = vectorstore.as_retriever(search_kwargs={"k": 4})
        docs = retriever.invoke(question)
        context = "\n\n".join(d.page_content for d in docs)
        prompt_text = RAG_PROMPT.format(context=context, question=question)
        answer = llm.invoke(prompt_text)
        sources = [doc.page_content[:200] for doc in docs]

        if language.lower() not in ("english", "en"):
            try:
                from services.translation_service import translate_text
                answer = translate_text(answer, language)
            except Exception:
                pass

        return {"answer": answer, "sources": sources}

    except Exception as e:
        logger.error("RAG ask failed for note %d: %s", note_id, str(e))
        return {"answer": f"AI service error: {str(e)}", "sources": []}


def delete_index(note_id: int):
    index_path = FAISS_DIR / f"note_{note_id}"
    if index_path.exists():
        shutil.rmtree(index_path)
        logger.info("Deleted index for note %d", note_id)