import io
import logging
import tempfile
from pathlib import Path
from typing import Optional, Dict

from gtts import gTTS                  # gTTS is fine at top level

logger = logging.getLogger(__name__)

_whisper_model = None


def _get_whisper():
    global _whisper_model

    if _whisper_model is None:
        logger.info("Loading Whisper model...")
        import importlib
        whisper = importlib.import_module("whisper")   # ← lazy, avoids the bad whisper.py
        _whisper_model = whisper.load_model("base")
        logger.info("Whisper model loaded successfully.")

    return _whisper_model


def transcribe_audio(audio_bytes: bytes, language: Optional[str] = None) -> Dict:

    model = _get_whisper()

    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        tmp.write(audio_bytes)
        tmp_path = tmp.name

    try:
        options = {}
        if language:
            options["language"] = language

        result = model.transcribe(tmp_path, **options)

        return {
            "transcript": result.get("text", "").strip(),
            "language":   result.get("language", "en"),
            "confidence": 1.0
        }

    except Exception as e:
        logger.error("Whisper transcription failed: %s", str(e))
        return {
            "transcript": "",
            "language":   "en",
            "confidence": 0.0
        }

    finally:
        Path(tmp_path).unlink(missing_ok=True)


def text_to_speech(text: str, language: str = "en") -> bytes:

    lang_map = {
        "english": "en",
        "hindi":   "hi",
        "tamil":   "ta",
        "telugu":  "te",
        "bengali": "bn",
        "french":  "fr",
        "german":  "de",
        "spanish": "es",
        "arabic":  "ar",
        "chinese": "zh-cn",
        "japanese":"ja",
        "korean":  "ko",
    }

    lang_code = lang_map.get(language.lower(), language)

    try:
        tts = gTTS(text=text[:3000], lang=lang_code, slow=False)
        buffer = io.BytesIO()
        tts.write_to_fp(buffer)
        buffer.seek(0)
        return buffer.read()

    except Exception as e:
        logger.error("gTTS failed for language %s: %s", language, str(e))
        raise RuntimeError(f"TTS failed: {e}")