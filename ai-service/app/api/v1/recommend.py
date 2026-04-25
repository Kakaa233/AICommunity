"""API v1: 相关推荐 (Content Recommendation)

Input:  articleId + limit + candidate articles
Output: list of recommended articles with reason
"""

from __future__ import annotations

import time

import structlog
from fastapi import APIRouter

from ...core.config import settings
from ...models.schemas import ApiResponse, RecommendRequest, RecommendResponse
from ...services.impl import RecommendService

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/recommend", tags=["recommend"])


@router.post("", response_model=ApiResponse[RecommendResponse])
async def recommend(body: RecommendRequest):
    start = time.monotonic()
    service = RecommendService()
    # Java backend should send the current article and candidates.
    # Placeholder — real integration will populate these from the request body.
    current_article = f"articleId: {body.articleId}"
    candidates_json = "[]"
    data, fallback = await service.run(
        current_article=current_article,
        candidates_json=candidates_json,
    )
    elapsed_ms = round((time.monotonic() - start) * 1000)
    return ApiResponse(
        code=0 if data else 5002,
        message="ok" if data else "fallback empty",
        data=data or RecommendResponse(),
        latencyMs=elapsed_ms,
        fallbackHit=fallback,
        model=settings.MODEL_NAME,
    )
