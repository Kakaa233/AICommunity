"""Service implementations for all 7 AI capabilities."""

from __future__ import annotations

import json

from ..models.schemas import (
    DraftResponse,
    ModerationResponse,
    PolishResponse,
    QualityResponse,
    RecommendResponse,
    SummaryResponse,
    TopicsResponse,
)
from .base import BaseAIService
from .prompts import (
    DRAFT_SYSTEM,
    MODERATION_SYSTEM,
    POLISH_SYSTEM,
    QUALITY_SYSTEM,
    RECOMMEND_SYSTEM,
    SUMMARY_SYSTEM,
    TOPICS_SYSTEM,
)


# ── 内容润色 ─────────────────────────────────────────────────


class PolishService(BaseAIService[PolishResponse]):
    system_prompt = POLISH_SYSTEM
    timeout_sec = 3.0
    max_tokens = 1024

    def _parse_response(self, raw: str) -> PolishResponse:
        data = json.loads(raw)
        return PolishResponse(
            polishedTitle=data.get("polishedTitle", ""),
            summary=data.get("summary", ""),
            tags=data.get("tags", []),
        )

    async def run(self, title: str, content: str) -> tuple[PolishResponse | None, bool]:
        user = f"## 标题\n{title}\n\n## 正文\n{content}"
        return await super().run(user)


# ── 问答草稿 ─────────────────────────────────────────────────


class DraftService(BaseAIService[DraftResponse]):
    system_prompt = DRAFT_SYSTEM
    timeout_sec = 3.0
    max_tokens = 1024

    def _parse_response(self, raw: str) -> DraftResponse:
        data = json.loads(raw)
        return DraftResponse(draftContent=data.get("draftContent", ""))

    async def run(self, title: str, description: str) -> tuple[DraftResponse | None, bool]:
        user = f"## 问题标题\n{title}\n\n## 问题描述\n{description}"
        return await super().run(user)


# ── 长文摘要与要点 ───────────────────────────────────────────


class SummaryService(BaseAIService[SummaryResponse]):
    system_prompt = SUMMARY_SYSTEM
    timeout_sec = 3.0
    max_tokens = 1024

    def _parse_response(self, raw: str) -> SummaryResponse:
        data = json.loads(raw)
        return SummaryResponse(
            summary=data.get("summary", ""),
            keyPoints=data.get("keyPoints", []),
        )

    async def run(self, content: str) -> tuple[SummaryResponse | None, bool]:
        return await super().run(content[:10000])  # truncate to avoid token limit


# ── 质量评分 ─────────────────────────────────────────────────


class QualityService(BaseAIService[QualityResponse]):
    system_prompt = QUALITY_SYSTEM
    timeout_sec = 2.0
    max_tokens = 1024

    def _parse_response(self, raw: str) -> QualityResponse:
        data = json.loads(raw)
        dims = data.get("dimensions", [])
        return QualityResponse(
            totalScore=float(data.get("totalScore", 0)),
            dimensions=[
                {
                    "name": d.get("name", ""),
                    "score": float(d.get("score", 0)),
                    "reason": d.get("reason", ""),
                }
                for d in dims
            ],
        )

    async def run(self, title: str, content: str) -> tuple[QualityResponse | None, bool]:
        user = f"## 标题\n{title}\n\n## 正文\n{content[:8000]}"
        return await super().run(user)


# ── 内容审核 ─────────────────────────────────────────────────


class ModerationService(BaseAIService[ModerationResponse]):
    system_prompt = MODERATION_SYSTEM
    timeout_sec = 3.0
    max_tokens = 1024
    temperature = 0.3  # lower temperature for more deterministic审核

    def _parse_response(self, raw: str) -> ModerationResponse:
        data = json.loads(raw)
        return ModerationResponse(
            suggestion=data.get("suggestion", "pass"),
            reason=data.get("reason", ""),
            confidence=float(data.get("confidence", 1.0)),
            violationTypes=data.get("violationTypes", []),
        )

    async def run(
        self, title: str, content: str
    ) -> tuple[ModerationResponse | None, bool]:
        user = f"## 标题\n{title}\n\n## 正文\n{content[:8000]}"
        return await super().run(user)


# ── 专题聚合 ─────────────────────────────────────────────────


class TopicsService(BaseAIService[TopicsResponse]):
    system_prompt = TOPICS_SYSTEM
    timeout_sec = 5.0
    max_tokens = 2048

    def _parse_response(self, raw: str) -> TopicsResponse:
        data = json.loads(raw)
        topics_data = data.get("topics", [])
        return TopicsResponse(
            topics=[
                {
                    "topicId": str(t.get("topicId", i)),
                    "title": t.get("title", ""),
                    "summary": t.get("summary", ""),
                    "articleIds": t.get("articleIds", []),
                    "score": float(t.get("score", 0)),
                }
                for i, t in enumerate(topics_data)
            ]
        )

    async def run(self, articles_json: str) -> tuple[TopicsResponse | None, bool]:
        """articles_json: a JSON array of {id, title, summary} objects."""
        user = f"以下是近期文章列表，请进行专题聚类：\n{articles_json}"
        return await super().run(user)


# ── 相关推荐 ─────────────────────────────────────────────────


class RecommendService(BaseAIService[RecommendResponse]):
    system_prompt = RECOMMEND_SYSTEM
    timeout_sec = 1.5
    max_tokens = 1024

    def _parse_response(self, raw: str) -> RecommendResponse:
        data = json.loads(raw)
        items_data = data.get("items", [])
        return RecommendResponse(
            items=[
                {
                    "articleId": str(item.get("articleId", "")),
                    "title": item.get("title", ""),
                    "reason": item.get("reason", ""),
                    "score": float(item.get("score", 0)),
                }
                for item in items_data
            ]
        )

    async def run(
        self, current_article: str, candidates_json: str
    ) -> tuple[RecommendResponse | None, bool]:
        user = f"## 当前文章\n{current_article}\n\n## 候选文章\n{candidates_json}"
        return await super().run(user)
