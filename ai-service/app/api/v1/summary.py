"""API v1: 长文摘要与要点 (Summary & Key Points)

Input:  content
Output: summary (<=200 chars) + keyPoints (3-7 items)
"""

from __future__ import annotations

import time

import structlog
from fastapi import APIRouter

from ...core.config import settings
from ...models.schemas import ApiResponse, SummaryRequest, SummaryResponse
from ...services.impl import SummaryService

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/summary", tags=["summary"])


@router.post("", response_model=ApiResponse[SummaryResponse])
async def summary(body: SummaryRequest):
    start = time.monotonic()
    service = SummaryService()
    data, fallback = await service.run(content=body.content)
    elapsed_ms = round((time.monotonic() - start) * 1000)
    return ApiResponse(
        code=0 if data else 5002,
        message="ok" if data else "fallback empty",
        data=data or SummaryResponse(),
        latencyMs=elapsed_ms,
        fallbackHit=fallback,
        model=settings.MODEL_NAME,
    )
