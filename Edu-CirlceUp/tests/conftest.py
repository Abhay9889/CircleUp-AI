"""Pytest configuration and shared fixtures"""

import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
import sys, os

sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))


@pytest.fixture(scope="session")
def app():
    from main import app as _app
    return _app


@pytest.fixture(scope="session")
def client(app):
    with TestClient(app) as c:
        yield c


@pytest.fixture
def mock_note_text():
    """Mock note text returned by Spring Boot"""
    return {
        "text": """
        Machine Learning is a subset of Artificial Intelligence.
        Supervised learning uses labeled training data.
        Unsupervised learning finds patterns without labels.
        Neural networks are inspired by the human brain.
        Deep learning uses multiple layers of neural networks.
        """,
        "noteId": 1,
        "title": "ML Notes",
    }


@pytest.fixture
def mock_llm_response():
    """Mock LLM response for testing quiz/flashcard generation"""
    return '[{"question": "What is ML?", "answer": "A", "options": ["A)AI subset","B)OS","C)DB","D)FW"]}]'