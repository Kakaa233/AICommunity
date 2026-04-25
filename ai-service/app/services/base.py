"""Base service with shared LLM invocation + JSON parsing + fallback logic."""

from __future__ import annotations

import json
import time
from abc import ABC, abstractmethod
from typing import Any, Generic, Optional, TypeVar

import structlog

from ..clients.llm_client import LLMClient, LLMClientError, LLMTimeoutError

logger = structlog.get_logger(__name__)

T = TypeVar("T")


class BaseAIService(ABC, Generic[T]):
    """Abstract base for all AI capability services.

    Subclasses define:
    - system_prompt:   the system-level instruction
    - parse_response:  how to convert LLM JSON string → Pydantic model
    - timeout_sec:     per-endpoint timeout
    """

    system_prompt: str = ""
    timeout_sec: float = 3.0
    max_tokens: int = 1024
    temperature: float = 0.7
    json_mode: bool = True

    def __init__(self, llm: Optional[LLMClient] = None) -> None:
        self._llm = llm or LLMClient()
        self._log = logger.bind(service=self.__class__.__name__)

    async def run(self, user_prompt: str) -> tuple[T | None, bool]:
        """Execute the AI service.

        Returns:
            (parsed_data, fallback_hit): if fallback is True the caller should
            return empty/default data rather than failing.
        """
        start = time.monotonic()
        try:
            raw = await self._llm.chat(
                system_prompt=self.system_prompt,
                user_prompt=user_prompt,
                max_tokens=self.max_tokens,
                temperature=self.temperature,
                json_mode=self.json_mode,
            )
            parsed = self._parse_response(raw)
            elapsed = round((time.monotonic() - start) * 1000)
            self._log.info("service_success", elapsed_ms=elapsed)
            return parsed, False
        except (LLMTimeoutError, LLMClientError, json.JSONDecodeError, KeyError) as exc:
            elapsed = round((time.monotonic() - start) * 1000)
            self._log.warning("service_fallback", error=str(exc), elapsed_ms=elapsed)
            return None, True
        except Exception as exc:
            elapsed = round((time.monotonic() - start) * 1000)
            self._log.exception("service_unexpected_error", elapsed_ms=elapsed)
            return None, True

    @abstractmethod
    def _parse_response(self, raw: str) -> T:
        """Parse the JSON string from LLM into the target response model."""
