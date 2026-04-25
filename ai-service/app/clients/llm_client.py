"""Generic LLM API client with retry, timeout, and streaming support.

Currently implements the OpenAI-compatible Chat Completions interface so it
works out-of-the-box with OpenAI, Azure OpenAI, DeepSeek, and many other
providers.
"""

from __future__ import annotations

import time
from typing import Any, Optional

import httpx
import structlog
from tenacity import (
    before_sleep_log,
    retry,
    retry_if_exception_type,
    stop_after_attempt,
    wait_exponential,
)

from ..core.config import settings

logger = structlog.get_logger(__name__)


class LLMClientError(Exception):
    """Base exception for LLM API calls."""


class LLMTimeoutError(LLMClientError):
    """Raised when the upstream model API times out."""


class LLMStatusError(LLMClientError):
    """Raised on non-2xx response from the upstream."""


class LLMClient:
    """Thin wrapper around an OpenAI-compatible chat completions endpoint."""

    def __init__(self) -> None:
        self._base_url = settings.MODEL_API_BASE.rstrip("/")
        self._api_key = settings.MODEL_API_KEY
        self._model = settings.MODEL_NAME
        self._timeout = settings.MODEL_TIMEOUT_SEC
        self._client = httpx.AsyncClient(
            base_url=self._base_url,
            timeout=self._timeout,
            headers={"Authorization": f"Bearer {self._api_key}"},
        )
        self._log = logger.bind(model=self._model, base_url=self._base_url)

    # ── Retry decorator ──────────────────────────────────────

    def _retry_decorator(self):
        return retry(
            stop=stop_after_attempt(settings.RETRY_MAX_ATTEMPTS),
            wait=wait_exponential(
                min=settings.RETRY_MIN_WAIT_SEC,
                max=settings.RETRY_MAX_WAIT_SEC,
            ),
            retry=retry_if_exception_type(
                (httpx.TimeoutException, httpx.NetworkError, httpx.RemoteProtocolError)
            ),
            before_sleep=before_sleep_log(self._log, "warning"),
            reraise=True,
        )

    # ── Public helpers ───────────────────────────────────────

    def _build_messages(self, system: str, user: str) -> list[dict]:
        return [
            {"role": "system", "content": system},
            {"role": "user", "content": user},
        ]

    async def chat(
        self,
        system_prompt: str,
        user_prompt: str,
        *,
        max_tokens: Optional[int] = None,
        temperature: Optional[float] = None,
        json_mode: bool = False,
    ) -> str:
        """Call the chat completions endpoint and return the content string."""
        payload: dict[str, Any] = {
            "model": self._model,
            "messages": self._build_messages(system_prompt, user_prompt),
            "max_tokens": max_tokens or settings.MODEL_MAX_TOKENS,
            "temperature": temperature if temperature is not None else settings.MODEL_TEMPERATURE,
        }
        if json_mode:
            payload["response_format"] = {"type": "json_object"}

        self._log.info("llm_request", max_tokens=payload["max_tokens"], json_mode=json_mode)
        start = time.monotonic()

        try:
            resp = await self._retry_decorator()(self._client.post)(
                "/chat/completions",
                json=payload,
            )
        except httpx.TimeoutException:
            elapsed = time.monotonic() - start
            self._log.warning("llm_timeout", elapsed_sec=round(elapsed, 2))
            raise LLMTimeoutError(f"Model API timed out after {elapsed:.1f}s")
        except httpx.HTTPStatusError as exc:
            self._log.error("llm_http_error", status=exc.response.status_code, body=exc.response.text)
            raise LLMStatusError(f"Model API returned {exc.response.status_code}")
        except Exception:
            self._log.exception("llm_unexpected_error")
            raise

        elapsed = time.monotonic() - start

        if resp.status_code != 200:
            self._log.error("llm_non_ok", status=resp.status_code, body=resp.text)
            raise LLMStatusError(f"Model API returned {resp.status_code}")

        data = resp.json()
        content: str = data["choices"][0]["message"]["content"]
        usage = data.get("usage", {})
        self._log.info(
            "llm_success",
            elapsed_ms=round(elapsed * 1000),
            prompt_tokens=usage.get("prompt_tokens"),
            completion_tokens=usage.get("completion_tokens"),
        )
        return content

    async def close(self) -> None:
        await self._client.aclose()
