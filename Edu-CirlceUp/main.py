"""
EduCircle — FastAPI AI Service
All routers: RAG · Quiz · Flashcards · Tools · Translation · Voice
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import logging

from routers import (
    rag_router,
    quiz_router,
    tools_router,
    translate_router,
    flashcard_router,
    voice_router,
)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s — %(message)s"
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("EduCircle AI Service starting up...")
    yield
    logger.info("EduCircle AI Service shutting down.")


app = FastAPI(
    title="EduCircle AI Service",
    description="AI/ML backend — RAG, Quiz, Flashcards, Voice, Translation",
    version="1.0.0",
    docs_url="/docs",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "http://localhost:5173",
        "http://springboot:8080",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Routers ────────────────────────────────────────────────
app.include_router(rag_router.router,        prefix="/rag",        tags=["RAG"])
app.include_router(quiz_router.router,       prefix="/quiz",       tags=["Quiz"])
app.include_router(flashcard_router.router,  prefix="/flashcards", tags=["Flashcards"])
app.include_router(tools_router.router,      prefix="/tools",      tags=["Tools"])
app.include_router(translate_router.router,  prefix="/translate",  tags=["Translation"])
app.include_router(voice_router.router,      prefix="/voice",      tags=["Voice"])


# ── Health ─────────────────────────────────────────────────
@app.get("/health", tags=["Health"])
async def health():
    from db.database import health_check
    db_ok = health_check()
    return {
        "status": "ok" if db_ok else "degraded",
        "db":     "connected" if db_ok else "disconnected",
        "service": "educircle-ai",
    }
