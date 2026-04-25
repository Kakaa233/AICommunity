"""Request authentication middleware.

Java backend and AI service share a pre-configured SERVICE_KEY.
Each request from Java includes:
  - X-Service-Key:   HMAC-SHA256(timestamp + ":" + SERVICE_KEY, SERVICE_KEY)
  - X-Timestamp:     unix epoch millis

The middleware verifies the signature and rejects requests outside a
5-minute replay window.
"""

from __future__ import annotations

import hashlib
import hmac
import time

from fastapi import Request, HTTPException
from starlette.middleware.base import BaseHTTPMiddleware

from .config import settings

REPLAY_WINDOW_SEC = 300  # 5 minutes


def _compute_signature(timestamp: str, secret: str) -> str:
    msg = f"{timestamp}:{secret}".encode("utf-8")
    return hmac.new(secret.encode("utf-8"), msg, hashlib.sha256).hexdigest()


def _verify(timestamp: str, signature: str, secret: str) -> bool:
    expected = _compute_signature(timestamp, secret)
    return hmac.compare_digest(expected, signature)


class ServiceAuthMiddleware(BaseHTTPMiddleware):
    """Validates X-Service-Key + X-Timestamp on every request."""

    async def dispatch(self, request: Request, call_next):
        # Health-check endpoints may skip auth
        if request.url.path in ("/health", "/ready", "/version"):
            return await call_next(request)

        sig = request.headers.get("X-Service-Key", "")
        ts_str = request.headers.get("X-Timestamp", "")

        if not sig or not ts_str:
            raise HTTPException(status_code=401, detail="Missing authentication headers")

        try:
            ts = int(ts_str)
        except ValueError:
            raise HTTPException(status_code=401, detail="Invalid X-Timestamp format")

        now = time.time()
        if abs(now - ts / 1000.0) > REPLAY_WINDOW_SEC:
            raise HTTPException(status_code=401, detail="Request expired")

        if not _verify(ts_str, sig, settings.SERVICE_KEY):
            raise HTTPException(status_code=403, detail="Invalid signature")

        return await call_next(request)
