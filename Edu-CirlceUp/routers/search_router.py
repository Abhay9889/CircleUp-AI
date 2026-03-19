
from fastapi import APIRouter
from models.schemas import SearchRequest, SearchResponse
from services.search_service import brave_search

router = APIRouter()


@router.post("/web", response_model=SearchResponse)
def web_search(req: SearchRequest):
    results = brave_search(req.query, req.count)
    return {"results": results}
