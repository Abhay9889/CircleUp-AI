import logging
import requests
from typing import List

from config import get_settings

logger   = logging.getLogger(__name__)
settings = get_settings()


def brave_search(query: str, count: int = 5) -> List[dict]:
    if not settings.brave_api_key:
        logger.warning("No Brave API key — falling back to DuckDuckGo")
        return duckduckgo_search(query, count)

    try:
        resp = requests.get(
            "https://api.search.brave.com/res/v1/web/search",
            headers={
                "Accept":                "application/json",
                "Accept-Encoding":       "gzip",
                "X-Subscription-Token":  settings.brave_api_key,
            },
            params={"q": query, "count": count},
            timeout=10
        )
        resp.raise_for_status()
        data    = resp.json()
        results = data.get("web", {}).get("results", [])
        return [
            {
                "title":   r.get("title", ""),
                "url":     r.get("url", ""),
                "snippet": r.get("description", ""),
            }
            for r in results[:count]
        ]
    except Exception as e:
        logger.error("Brave search failed: %s — falling back", e)
        return duckduckgo_search(query, count)


def duckduckgo_search(query: str, count: int = 5) -> List[dict]:
    """Search using DuckDuckGo Instant Answer API (no key needed)."""
    try:
        resp = requests.get(
            "https://api.duckduckgo.com/",
            params={"q": query, "format": "json", "no_html": 1},
            timeout=10
        )
        data    = resp.json()
        results = []

        # Related topics
        for topic in data.get("RelatedTopics", [])[:count]:
            if "Text" in topic and "FirstURL" in topic:
                results.append({
                    "title":   topic["Text"][:80],
                    "url":     topic["FirstURL"],
                    "snippet": topic["Text"],
                })

        return results
    except Exception as e:
        logger.error("DuckDuckGo search failed: %s", e)
        return []
