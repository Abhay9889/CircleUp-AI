"""
Translation Service
Primary:  Helsinki-NLP MarianMT models (HuggingFace — offline, free)
Fallback: LibreTranslate (Docker service)
"""

import logging
import requests
from functools import lru_cache

from config import get_settings

logger   = logging.getLogger(__name__)
settings = get_settings()

LANGUAGE_CODE_MAP = {
    "hindi":      "hi",
    "tamil":      "ta",
    "telugu":     "te",
    "bengali":    "bn",
    "marathi":    "mr",
    "gujarati":   "gu",
    "kannada":    "kn",
    "malayalam":  "ml",
    "punjabi":    "pa",
    "urdu":       "ur",
    "french":     "fr",
    "german":     "de",
    "spanish":    "es",
    "arabic":     "ar",
    "chinese":    "zh",
    "japanese":   "ja",
    "korean":     "ko",
    "russian":    "ru",
}


@lru_cache(maxsize=8)
def _load_helsinki_pipeline(target_code: str):
    """Load MarianMT model — cached so it loads only once per language."""
    from transformers import pipeline
    model_name = f"Helsinki-NLP/opus-mt-en-{target_code}"
    try:
        return pipeline("translation", model=model_name)
    except Exception as e:
        logger.warning("Helsinki model not available for %s: %s", target_code, e)
        return None


def translate_text(text: str, target_language: str) -> str:
    """
    Translate English text to target language.
    Tries Helsinki-NLP first, falls back to LibreTranslate.
    """
    if not text or not text.strip():
        return text

    target_code = LANGUAGE_CODE_MAP.get(target_language.lower())
    if not target_code:
        logger.warning("Unsupported language: %s", target_language)
        return text

    # Try Helsinki-NLP (offline, free)
    try:
        pipe = _load_helsinki_pipeline(target_code)
        if pipe:
            result = pipe(text[:1000])  # MarianMT limit
            return result[0]["translation_text"]
    except Exception as e:
        logger.warning("Helsinki translation failed: %s", e)

    # Fallback: LibreTranslate
    return _libretranslate(text, target_code)


def _libretranslate(text: str, target_code: str) -> str:
    """Call local LibreTranslate container."""
    try:
        resp = requests.post(
            f"{settings.libre_translate_url}/translate",
            json={"q": text, "source": "en", "target": target_code, "format": "text"},
            timeout=15
        )
        if resp.ok:
            return resp.json().get("translatedText", text)
    except Exception as e:
        logger.error("LibreTranslate failed: %s", e)
    return text


def supported_languages() -> list:
    return list(LANGUAGE_CODE_MAP.keys())
