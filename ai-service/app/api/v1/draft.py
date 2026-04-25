"""API v1: 问答草稿 (Question Draft)

Input:  title + description
Output: draftContent (<=500 chars structured draft)
"""

from __future__ import annotations

import time

import structlog
from fastapi import APIRouter

from ...core.config import settings
from ...models.schemas import ApiResponse, DraftRequest, DraftResponse
from ...services.impl import DraftService

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/draft", tags=["draft"])


@router.post("", response_model=ApiResponse[DraftResponse])
async def draft(body: DraftRequest):
    start = time.monotonic()
    service = DraftService()
    data, fallback = await service.run(title=body.title, description=body.description)
    elapsed_ms = round((time.monotonic() - start) * 1000)
    return ApiResponse(
        code=0 if data else 5002,
        message="ok" if data else "fallback empty",
        data=data or DraftResponse(),
        latencyMs=elapsed_ms,
        fallbackHit=fallback,
        model=settings.MODEL_NAME,
    )
