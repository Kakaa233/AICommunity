"""API v1: 相关推荐 (Content Recommendation)

Input:  articleId + limit + candidate articles
Output: list of recommended articles with reason
"""

from __future__ import annotations

import json
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
    # Build current article description with title + first 500 chars of content
    current_parts = [f"articleId: {body.articleId}"]
    if body.currentTitle:
        current_parts.append(f"title: {body.currentTitle}")
    if body.currentContent:
        current_parts.append(f"content: {body.currentContent[:500]}")
    current_article = "\n".join(current_parts)
    candidates_json = json.dumps(body.candidates, ensure_ascii=False) if body.candidates else "[]"
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
