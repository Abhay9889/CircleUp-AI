import json
import logging
import re
from typing import List

from langchain_ollama import OllamaLLM
from config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

llm = OllamaLLM(
    model=settings.ollama_model,
    base_url=settings.ollama_base_url,
    temperature=0.4
)

FLASHCARD_PROMPT = """
You are a study assistant. Based on the text below, generate {count} flashcard question-answer pairs.
Focus on key concepts, definitions, and important facts.

Text:
{text}

Return ONLY a valid JSON array, no explanation:
[
  {{
    "question": "What is ...?",
    "answer": "..."
  }}
]
"""


def generate_flashcards(text: str, count: int = 10) -> List[dict]:

    prompt = FLASHCARD_PROMPT.format(text=text[:4000], count=count)

    try:
        raw = llm.invoke(prompt)

        # Extract JSON array from LLM output
        match = re.search(r'\[.*\]', raw, re.DOTALL)

        if match:
            cards = json.loads(match.group())
            return cards[:count]

        else:
            logger.error("LLM did not return valid JSON for flashcards")
            return []

    except Exception as e:
        logger.error("Flashcard generation failed: %s", e)
        return []