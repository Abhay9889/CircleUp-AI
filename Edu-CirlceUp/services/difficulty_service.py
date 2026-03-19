"""Difficulty scoring using textstat"""

import textstat
import httpx
from config import get_settings

settings = get_settings()

class DifficultyService:

    async def score(self, note_id: int) -> dict:
        async with httpx.AsyncClient() as client:
            resp = await client.get(
                f"http://springboot:8080/api/notes/{note_id}/text", timeout=30
            )
            text = resp.json().get("text", "")

        if not text:
            return {"score": 0, "level": "unknown", "note_id": note_id}

        flesch  = textstat.flesch_reading_ease(text)
        grade   = textstat.flesch_kincaid_grade(text)
        syllables = textstat.syllable_count(text)

        if flesch >= 70:
            level = "easy"
        elif flesch >= 50:
            level = "medium"
        elif flesch >= 30:
            level = "hard"
        else:
            level = "very_hard"

        return {
            "score":    round(flesch, 2),
            "grade":    round(grade, 1),
            "level":    level,
            "syllables": syllables,
            "note_id":  note_id
        }