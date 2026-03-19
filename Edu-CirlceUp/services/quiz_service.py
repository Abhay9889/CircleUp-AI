import json
import logging
import re
from typing import List
from langchain_ollama import OllamaLLM
from config import get_settings

logger=logging.getLogger(__name__)
settings=get_settings()
llm=OllamaLLM(
    model=settings.ollama_model,
    base_url=settings.ollama_base_url,
    temperature=0.5,
)


MCQ_PROMPT = """
You are an exam question generator. Based on the text below, generate {count} multiple choice questions.

Text:
{text}

Return ONLY a valid JSON array, no explanation. Format:
[
  {{
    "question": "...",
    "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
    "answer": "A. ...",
    "explanation": "..."
  }}
]
"""

TF_PROMPT = """
You are an exam question generator. Based on the text below, generate {count} True/False questions.

Text:
{text}

Return ONLY a valid JSON array:
[
  {{
    "question": "...",
    "options": ["True", "False"],
    "answer": "True",
    "explanation": "..."
  }}
]
"""

SA_PROMPT = """
You are an exam question generator. Based on the text below, generate {count} short answer questions.

Text:
{text}

Return ONLY a valid JSON array:
[
  {{
    "question": "...",
    "options": null,
    "answer": "...",
    "explanation": "..."
  }}
]
"""


def genrate_questions(text:str,count:int,quiz_type:str)->List[dict]:
    """
    Generate quiz questions from note text.
    quiz_type: mcq | true_false | short_answer
    """
    prompt_map = {
        "mcq":          MCQ_PROMPT,
        "true_false":   TF_PROMPT,
        "short_answer": SA_PROMPT,
    }
    prompt_template=prompt_map.get(quiz_type.lower(),MCQ_PROMPT)
    prompt=prompt_template.format(text=text[:4000],count=count)
    try:
        raw=llm.invoke(prompt)
        match=re.search(r'\[.*\]',raw,re.DOTALL)
        if match:
            questions=json.loads(match.group())
            return questions[:count]
        else:
            logger.error("LLM did not return valid JSON for quiz")
            return _fallback_questions(count)
    except Exception as e:
        logger.error("Quiz generation failed: %s", e)
        return _fallback_questions(count)


def evaluate_answers(questions_json: str, user_answers: dict) -> dict:

    try:
        questions = json.loads(questions_json) if isinstance(questions_json, str) else questions_json
    except Exception:
        return {"score": 0, "feedback": []}
    score    = 0
    feedback = []
    for i,q in enumerate(questions):
        user_ans=str(user_answers.get(str(i),"")).strip().lower() 
        correct_ans=str(q.get("answer","")).strip().lower()
        is_correct=user_ans==correct_ans or user_ans in correct_ans

        if is_correct:
            score+=1

        feedback.append({
            "question":    q.get("question"),
            "your_answer": user_answers.get(str(i), ""),
            "correct":     q.get("answer"),
            "is_correct":  is_correct,
            "explanation": q.get("explanation", ""),
        })
    return {"score": score, "feedback": feedback}

def _fallback_questions(count: int) -> List[dict]:
    return [
        {
            "question":    f"Sample question {i+1}",
            "options":     ["A. Option A", "B. Option B", "C. Option C", "D. Option D"],
            "answer":      "A. Option A",
            "explanation": "Unable to generate question from content."
        }
        for i in range(count)
    ]

