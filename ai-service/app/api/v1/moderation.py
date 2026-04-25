"""API v1: 内容审核 (Content Moderation)

Input:  contentId + title + content + authorId
Output: suggestion (pass/flag/reject) + reason + confidence + violationTypes
"""

from __future__ import annotations

import time

import structlog
from fastapi import APIRouter

from ...core.config import settings
from ...models.schemas import ApiResponse, ModerationRequest, ModerationResponse
from ...services.impl import ModerationService

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/moderation", tags=["moderation"])


@router.post("", response_model=ApiResponse[ModerationResponse])
async def moderation(body: ModerationRequest):
    start = time.monotonic()
    service = ModerationService()
    title = body.title or ""
    content = body.content or ""
    data, fallback = await service.run(title=title, content=content)
    elapsed_ms = round((time.monotonic() - start) * 1000)
    return ApiResponse(
        code=0 if data else 5002,
        message="ok" if data else "fallback empty",
        data=data or ModerationResponse(),
        latencyMs=elapsed_ms,
        fallbackHit=fallback,
        model=settings.MODEL_NAME,
    )
