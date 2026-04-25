"""Request authentication middleware.

Java backend and AI service share a pre-configured SERVICE_KEY.
Each request from Java includes:
    - X-Service-Key: shared SERVICE_KEY plain text
    - X-Timestamp: unix epoch seconds
    - X-Signature: HMAC-SHA256(timestamp, SERVICE_KEY)

The middleware verifies key/signature and rejects requests outside a
5-minute replay window.
"""

from __future__ import annotations

import hmac
import hashlib
import time

from fastapi import Request
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware

from .config import settings

REPLAY_WINDOW_SEC = 300  # 5 minutes


def _compute_signature(timestamp: str, secret: str) -> str:
    msg = timestamp.encode("utf-8")
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

        svc_key = request.headers.get("X-Service-Key", "")
        ts_str = request.headers.get("X-Timestamp", "")
        signature = request.headers.get("X-Signature", "")

        if not svc_key or not ts_str or not signature:
            return JSONResponse(
                status_code=401,
                content={"detail": "Missing authentication headers"},
            )

        if svc_key != settings.SERVICE_KEY:
            return JSONResponse(
                status_code=403,
                content={"detail": "Invalid service key"},
            )

        try:
            ts = int(ts_str)
        except ValueError:
            return JSONResponse(
                status_code=401,
                content={"detail": "Invalid X-Timestamp format"},
            )

        # Support both seconds and milliseconds to be backward compatible.
        ts_sec = ts / 1000.0 if ts > 10_000_000_000 else float(ts)

        now = time.time()
        if abs(now - ts_sec) > REPLAY_WINDOW_SEC:
            return JSONResponse(
                status_code=401,
                content={"detail": "Request expired"},
            )

        if not _verify(ts_str, signature, settings.SERVICE_KEY):
            return JSONResponse(
                status_code=403,
                content={"detail": "Invalid signature"},
            )

        return await call_next(request)
