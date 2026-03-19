class OCRService:
    def extract_text(self, image_bytes: bytes) -> str:
        try:
            import pytesseract
            from PIL import Image
            import io
            return pytesseract.image_to_string(Image.open(io.BytesIO(image_bytes)))
        except Exception as e:
            return f"OCR unavailable: {str(e)}"