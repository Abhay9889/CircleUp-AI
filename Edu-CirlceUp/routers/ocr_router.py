"""OCR Service — extract text from handwritten notes / scanned images"""

import pytesseract
from PIL import Image
import httpx
import io
from fastapi import APIRouter, UploadFile, File, HTTPException
from config import get_settings

settings = get_settings()
router   = APIRouter()


class OcrService:

    @staticmethod
    def extract_from_bytes(image_bytes: bytes, language: str = "eng") -> str:
        """Extract text from image bytes using Tesseract OCR"""
        try:
            image = Image.open(io.BytesIO(image_bytes))

            # Map language name to Tesseract language code
            lang_map = {
                "english": "eng",
                "hindi":   "hin",
                "tamil":   "tam",
                "telugu":  "tel",
            }
            tess_lang = lang_map.get(language.lower(), "eng")

            text = pytesseract.image_to_string(
                image,
                lang=tess_lang,
                config="--psm 3"   # automatic page segmentation
            )
            return text.strip()
        except Exception as e:
            raise RuntimeError(f"OCR failed: {e}")


ocr_service = OcrService()


@router.post("/extract")
async def extract_text(
    image:    UploadFile = File(...),
    language: str        = "english",
):
    """Extract text from uploaded image (handwritten notes, scanned docs)"""
    if not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    image_bytes = await image.read()
    if len(image_bytes) > 10 * 1024 * 1024:
        raise HTTPException(status_code=400, detail="Image too large (max 10MB)")

    try:
        text = ocr_service.extract_from_bytes(image_bytes, language)
        return {
            "text":     text,
            "language": language,
            "chars":    len(text),
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))