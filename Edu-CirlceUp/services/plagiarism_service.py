class PlagiarismService:
    def __init__(self):
        try:
            from sentence_transformers import SentenceTransformer
            import numpy as np
            self.model = SentenceTransformer("all-MiniLM-L6-v2")
            self.np = np
        except:
            self.model = None

    def check(self, text1: str, text2: str) -> dict:
        if not self.model:
            return {"similarity": 0.0, "is_plagiarised": False}
        e1, e2 = self.model.encode([text1, text2])
        sim = float(self.np.dot(e1, e2) /
                   (self.np.linalg.norm(e1) * self.np.linalg.norm(e2)))
        return {"similarity": round(sim, 3), "is_plagiarised": sim > 0.85}