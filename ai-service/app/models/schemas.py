"""Unified Pydantic models for request / response."""

from __future__ import annotations

from datetime import datetime
from typing import Any, Generic, Optional, TypeVar

from pydantic import BaseModel, Field

# ── Error code constants ────────────────────────────────────


class ErrorCode:
    SUCCESS = 0
    BAD_REQUEST = 400
    UNAUTHORIZED = 401
    FORBIDDEN = 403
    NOT_FOUND = 404
    TIMEOUT = 408
    RATE_LIMITED = 429
    MODEL_ERROR = 5001
    FALLBACK_EMPTY = 5002
    SERVICE_UNAVAILABLE = 5003


# ── Generic response wrapper ────────────────────────────────

T = TypeVar("T")


class ApiResponse(BaseModel, Generic[T]):
    """Unified API response contract."""

    code: int = ErrorCode.SUCCESS
    message: str = "ok"
    data: Optional[T] = None
    latencyMs: Optional[float] = None
    fallbackHit: bool = False
    model: Optional[str] = None


# ── Request models for each capability ──────────────────────


class PolishRequest(BaseModel):
    title: str = Field(..., max_length=200, description="Article title")
    content: str = Field(..., max_length=50000, description="Article body markdown")


class PolishResponse(BaseModel):
    polishedTitle: str = ""
    summary: str = ""
    tags: list[str] = []


class DraftRequest(BaseModel):
    title: str = Field(..., max_length=200, description="Question title")
    description: str = Field(..., max_length=10000, description="Question description")


class DraftResponse(BaseModel):
    draftContent: str = ""


class SummaryRequest(BaseModel):
    content: str = Field(..., max_length=50000, description="Full article body")


class SummaryResponse(BaseModel):
    summary: str = ""
    keyPoints: list[str] = []


class QualityRequest(BaseModel):
    title: str = Field(..., max_length=200)
    content: str = Field(..., max_length=50000)


class QualityDimension(BaseModel):
    name: str
    score: float  # 0-100
    reason: str = ""


class QualityResponse(BaseModel):
    totalScore: float = 0.0
    dimensions: list[QualityDimension] = []


class ModerationRequest(BaseModel):
    contentId: str = Field(..., description="Business primary key")
    title: str = Field(..., max_length=200)
    content: str = Field(..., max_length=50000)
    authorId: str = Field("", max_length=64)


class ModerationResponse(BaseModel):
    suggestion: str = "pass"  # pass / flag / reject
    reason: str = ""
    confidence: float = 1.0
    violationTypes: list[str] = []


class TopicsRequest(BaseModel):
    timeRangeDays: int = 7
    maxTopics: int = 10


class TopicItem(BaseModel):
    topicId: str = ""
    title: str = ""
    summary: str = ""
    articleIds: list[str] = []
    score: float = 0.0


class TopicsResponse(BaseModel):
    topics: list[TopicItem] = []


class RecommendRequest(BaseModel):
    articleId: str = Field(..., description="Current article ID")
    limit: int = 6


class RecommendItem(BaseModel):
    articleId: str = ""
    title: str = ""
    reason: str = ""
    score: float = 0.0


class RecommendResponse(BaseModel):
    items: list[RecommendItem] = []


# ── Health ──────────────────────────────────────────────────


class HealthResponse(BaseModel):
    status: str = "ok"
    version: str = ""
    timestamp: datetime = Field(default_factory=datetime.now)
