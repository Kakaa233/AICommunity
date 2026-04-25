"""API v1: 内容润色 (Content Polish)

Input:  title + content
Output: polishedTitle + summary + tags
"""

from __future__ import annotations

import time

import structlog
from fastapi import APIRouter

from ...core.config import settings
from ...models.schemas import ApiResponse, PolishRequest, PolishResponse
from ...services.impl import PolishService

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/polish", tags=["polish"])


@router.post("", response_model=ApiResponse[PolishResponse])
async def polish(body: PolishRequest):
    start = time.monotonic()
    service = PolishService()
    data, fallback = await service.run(title=body.title, content=body.content)
    elapsed_ms = round((time.monotonic() - start) * 1000)
    return ApiResponse(
        code=0 if data else 5002,
        message="ok" if data else "fallback empty",
        data=data or PolishResponse(),
        latencyMs=elapsed_ms,
        fallbackHit=fallback,
        model=settings.MODEL_NAME,
    )
