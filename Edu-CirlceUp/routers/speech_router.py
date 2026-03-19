
from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from fastapi.responses import Response
from models.schemas import TTSRequest, STTResponse
from services import speech_service

router = APIRouter()

@router.post("/stt", response_model=STTResponse)
async def speech_to_text(
        audio: UploadFile = File(...),
        language: str     = Form(default=None)):
   
    try:
        audio_bytes = await audio.read()
        result      = speech_service.transcribe_audio(audio_bytes, language)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/tts")
def text_to_speech(req: TTSRequest):
    """
    Convert text to MP3 audio using gTTS.
    Returns: audio/mpeg bytes
    """
    try:
        audio_bytes = speech_service.text_to_speech(req.text, req.language)
        return Response(
            content=audio_bytes,
            media_type="audio/mpeg",
            headers={"Content-Disposition": "attachment; filename=speech.mp3"}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
