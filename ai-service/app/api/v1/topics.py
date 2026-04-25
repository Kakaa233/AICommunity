"""API v1: 专题聚合 (Topic Aggregation)

Input:  timeRangeDays + maxTopics + articles (JSON array from Java backend)
Output: list of topics with article IDs
"""

from __future__ import annotations

import json
import time

import structlog
from fastapi import APIRouter

from ...core.config import settings
from ...models.schemas import ApiResponse, TopicsRequest, TopicsResponse
from ...services.impl import TopicsService

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/topics", tags=["topics"])


@router.post("", response_model=ApiResponse[TopicsResponse])
async def topics(body: TopicsRequest):
    start = time.monotonic()
    service = TopicsService()
    articles_json = json.dumps(body.articles, ensure_ascii=False) if body.articles else "[]"
    data, fallback = await service.run(articles_json=articles_json)
    elapsed_ms = round((time.monotonic() - start) * 1000)
    return ApiResponse(
        code=0 if data else 5002,
        message="ok" if data else "fallback empty",
        data=data or TopicsResponse(),
        latencyMs=elapsed_ms,
        fallbackHit=fallback,
        model=settings.MODEL_NAME,
    )
