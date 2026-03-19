from fastapi import APIRouter, UploadFile, File, HTTPException
from fastapi.responses import StreamingResponse
import io
import logging
from services import speech_service   # ← use speech_service, not voice_service

router = APIRouter(prefix="/voice", tags=["voice"])
logger = logging.getLogger(__name__)

@router.post("/transcribe")
async def transcribe(file: UploadFile = File(...), language: str = "en"):
    try:
        audio_bytes = await file.read()
        result = speech_service.transcribe_audio(audio_bytes, language)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/synthesize")
async def synthesize(text: str, language: str = "en"):
    try:
        audio_bytes = speech_service.text_to_speech(text, language)
        return StreamingResponse(io.BytesIO(audio_bytes), media_type="audio/mpeg")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))