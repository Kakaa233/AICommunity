"""API v1: 质量评分 (Quality Score)

Input:  title + content
Output: totalScore (0-100) + dimensions (completeness/clarity/value)
"""

from __future__ import annotations

import time

import structlog
from fastapi import APIRouter

from ...core.config import settings
from ...models.schemas import ApiResponse, QualityRequest, QualityResponse
from ...services.impl import QualityService

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/quality", tags=["quality"])


@router.post("", response_model=ApiResponse[QualityResponse])
async def quality(body: QualityRequest):
    start = time.monotonic()
    service = QualityService()
    data, fallback = await service.run(title=body.title, content=body.content)
    elapsed_ms = round((time.monotonic() - start) * 1000)
    return ApiResponse(
        code=0 if data else 5002,
        message="ok" if data else "fallback empty",
        data=data or QualityResponse(),
        latencyMs=elapsed_ms,
        fallbackHit=fallback,
        model=settings.MODEL_NAME,
    )
