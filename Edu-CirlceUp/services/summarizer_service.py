"""Summarization using Ollama LLM"""

import httpx
from langchain_ollama import OllamaLLM
from config import get_settings

settings = get_settings()
class SummarizerService:

    def __init__(self):
        self.llm = OllamaLLM(
            base_url=settings.ollama_base_url,
            model=settings.ollama_model
        )

    async def summarize(self, note_id: int) -> dict:
        async with httpx.AsyncClient() as client:
            resp = await client.get(
                f"http://springboot:8080/api/notes/{note_id}/text",
                timeout=30
            )
            text = resp.json().get("text", "")[:4000]

        prompt = f"""Summarize the following study material in clear, concise bullet points.
Focus on key concepts, definitions, and important facts.

Material:
{text}

Summary:"""

        summary = self.llm.invoke(prompt)

        return {
            "summary": summary.strip(),
            "note_id": note_id
        }