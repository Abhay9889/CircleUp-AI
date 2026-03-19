
from fastapi import APIRouter
from models.schemas import TranslateRequest, TranslateResponse
from services.translation_service import translate_text, supported_languages

router = APIRouter()


@router.post("", response_model=TranslateResponse)
def translate(req: TranslateRequest):
    translated = translate_text(req.text, req.target_language)
    return {
        "original":        req.text,
        "translated":      translated,
        "target_language": req.target_language,
    }


@router.get("/languages")
def get_languages():
    return {"languages": supported_languages()}
