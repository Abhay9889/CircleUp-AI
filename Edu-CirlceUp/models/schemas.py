

from pydantic import BaseModel, Field
from typing import Optional, List, Any


class AskRequest(BaseModel):
    note_id: int
    question: str
    use_web_search: bool = False
    language: str = "english"

class AskResponse(BaseModel):
    answer: str
    sources: List[str] = []
    web_results: List[dict] = []


class QuizGenerateRequest(BaseModel):
    note_id: int
    count: int = Field(default=5, ge=1, le=20)
    type: str = "mcq"   

class QuizQuestion(BaseModel):
    question: str
    options: Optional[List[str]] = None   
    answer: str
    explanation: str = ""

class QuizGenerateResponse(BaseModel):
    questions: List[QuizQuestion]

class QuizEvaluateRequest(BaseModel):
    quiz_id: int
    questions: Any      
    answers: dict       

class QuizEvaluateResponse(BaseModel):
    score: int
    max_score: int
    feedback: List[dict]


class FlashcardGenerateRequest(BaseModel):
    note_id: int
    count: int = Field(default=10, ge=1, le=30)

class FlashcardPair(BaseModel):
    question: str
    answer: str


class TranslateRequest(BaseModel):
    text: str
    target_language: str

class TranslateResponse(BaseModel):
    original: str
    translated: str
    target_language: str


class TTSRequest(BaseModel):
    text: str
    language: str = "en"

class STTResponse(BaseModel):
    transcript: str
    language: str
    confidence: float


class SummarizeRequest(BaseModel):
    note_id: int

class SummarizeResponse(BaseModel):
    summary: str
    key_points: List[str] = []

class MindMapRequest(BaseModel):
    note_id: int

class MindMapResponse(BaseModel):
    nodes: List[dict]   
    edges: List[dict]   

class PlagiarismRequest(BaseModel):
    text1: str
    text2: str

class PlagiarismResponse(BaseModel):
    similarity_score: float
    is_plagiarised: bool
    details: str = ""

class DifficultyResponse(BaseModel):
    score: float
    label: str          
    flesch_score: float


class SearchRequest(BaseModel):
    query: str
    count: int = 5

class SearchResult(BaseModel):
    title: str
    url: str
    snippet: str

class SearchResponse(BaseModel):
    results: List[SearchResult]
