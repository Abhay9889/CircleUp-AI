"""Mind Map generation using spaCy"""

import spacy
import httpx
from collections import defaultdict
from config import get_settings

settings = get_settings()

try:
    nlp = spacy.load("en_core_web_sm")
except OSError:
    import subprocess
    subprocess.run(["python", "-m", "spacy", "download", "en_core_web_sm"])
    nlp = spacy.load("en_core_web_sm")


class MindMapService:

    async def generate(self, note_id: int) -> dict:
        async with httpx.AsyncClient() as client:
            resp = await client.get(
                f"http://springboot:8080/api/notes/{note_id}/text", timeout=30
            )
            text = resp.json().get("text", "")[:5000]

        doc = nlp(text)

        # Extract entities and noun chunks as nodes
        entities  = [(ent.text, ent.label_) for ent in doc.ents]
        key_nouns = [chunk.root.lemma_ for chunk in doc.noun_chunks
                     if len(chunk.root.lemma_) > 3][:20]

        nodes = []
        edges = []

        # Central node
        nodes.append({"id": "root", "label": "Main Topic", "type": "root"})

        # Entity nodes
        seen = set()
        for i, (text_val, label) in enumerate(entities[:15]):
            if text_val not in seen:
                node_id = f"ent_{i}"
                nodes.append({"id": node_id, "label": text_val, "type": label})
                edges.append({"from": "root", "to": node_id})
                seen.add(text_val)

        return {"nodes": nodes, "edges": edges, "note_id": note_id}