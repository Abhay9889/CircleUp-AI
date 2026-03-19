"""FastAPI AI Service Tests"""

import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock, AsyncMock
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from main import app

client = TestClient(app)


# ── Health ────────────────────────────────────────────────
class TestHealth:

    def test_health_returns_ok(self):
        with patch("main.health_check", return_value=True):
            resp = client.get("/health")
        assert resp.status_code == 200
        assert resp.json()["status"] in ("ok", "degraded")


# ── Translation ───────────────────────────────────────────
class TestTranslation:

    @patch("services.translation_service.TranslationService.translate", new_callable=AsyncMock)
    def test_translate_success(self, mock_translate):
        mock_translate.return_value = "नमस्ते"
        resp = client.post("/translate", json={"text": "Hello", "target_language": "hindi"})
        assert resp.status_code == 200
        assert resp.json()["translated_text"] == "नमस्ते"

    def test_translate_missing_text(self):
        resp = client.post("/translate", json={"target_language": "hindi"})
        assert resp.status_code == 422   # Pydantic validation error


# ── Difficulty ────────────────────────────────────────────
class TestDifficulty:

    @patch("services.difficulty_service.DifficultyService.score", new_callable=AsyncMock)
    def test_difficulty_score(self, mock_score):
        mock_score.return_value = {
            "score": 65.5, "grade": 8.2, "level": "medium",
            "syllables": 1200, "note_id": 1
        }
        resp = client.get("/tools/difficulty/1")
        assert resp.status_code == 200
        data = resp.json()
        assert data["level"] == "medium"
        assert data["score"] == 65.5


# ── Quiz ──────────────────────────────────────────────────
class TestQuiz:

    @patch("services.quiz_service.QuizService.generate", new_callable=AsyncMock)
    def test_generate_quiz(self, mock_generate):
        mock_generate.return_value = {
            "questions": [
                {"question": "What is Python?", "answer": "A", "options": ["A)Lang","B)OS","C)DB","D)FW"]}
            ],
            "count": 1
        }
        resp = client.post("/quiz/generate", json={"note_id": 1, "count": 1, "type": "MCQ"})
        assert resp.status_code == 200
        assert len(resp.json()["questions"]) == 1

    def test_generate_quiz_invalid_body(self):
        resp = client.post("/quiz/generate", json={})
        assert resp.status_code == 422


# ── Plagiarism ────────────────────────────────────────────
class TestPlagiarism:

    def test_check_text_same_text(self):
        resp = client.post("/plagiarism/check-text", json={
            "text1": "Python is a programming language",
            "text2": "Python is a programming language"
        })
        assert resp.status_code == 200
        data = resp.json()
        assert data["percentage"] > 90

    def test_check_text_different(self):
        resp = client.post("/plagiarism/check-text", json={
            "text1": "The sun rises in the east",
            "text2": "Machine learning uses neural networks"
        })
        assert resp.status_code == 200
        data = resp.json()
        assert data["percentage"] < 50


# ── OCR ───────────────────────────────────────────────────
class TestOcr:

    def test_ocr_non_image_rejected(self):
        resp = client.post(
            "/ocr/extract",
            files={"image": ("test.txt", b"not an image", "text/plain")}
        )
        assert resp.status_code == 400


# ── SM-2 Logic unit test (pure Python, no HTTP) ───────────
class TestSM2Logic:
    """Test SM-2 algorithm logic directly"""

    def _sm2(self, repetitions, ease_factor, interval_days, quality):
        """Inline SM-2 from flashcard service"""
        if quality >= 3:
            if repetitions == 0:
                new_interval = 1
            elif repetitions == 1:
                new_interval = 6
            else:
                new_interval = round(interval_days * ease_factor)
            new_reps = repetitions + 1
        else:
            new_interval = 1
            new_reps = 0

        new_ef = ease_factor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        new_ef = max(1.3, new_ef)
        return new_reps, new_ef, new_interval

    def test_perfect_first_review(self):
        reps, ef, interval = self._sm2(0, 2.5, 1, 5)
        assert reps == 1
        assert interval == 1
        assert ef > 2.5

    def test_perfect_second_review(self):
        reps, ef, interval = self._sm2(1, 2.5, 1, 5)
        assert interval == 6

    def test_blackout_resets(self):
        reps, ef, interval = self._sm2(10, 2.5, 30, 0)
        assert reps == 0
        assert interval == 1

    def test_ef_minimum_bound(self):
        _, ef, _ = self._sm2(0, 1.3, 1, 0)
        assert ef >= 1.3