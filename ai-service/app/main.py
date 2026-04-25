"""FastAPI application entry point for Wego AI Service."""

from __future__ import annotations

import structlog
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.v1 import (
    polish,
    draft,
    summary,
    quality,
    moderation,
    topics,
    recommend,
)
from app.core.config import settings
from app.core.logging import setup_logging
from app.core.security import ServiceAuthMiddleware
from app.models.schemas import ApiResponse, HealthResponse

# ── Initialise logging ──────────────────────────────────────
setup_logging()
logger = structlog.get_logger(__name__)

# ── Application factory ─────────────────────────────────────

app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="AI-powered assistant for Wego Campus Community",
)

# ── Middleware ───────────────────────────────────────────────

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
app.add_middleware(ServiceAuthMiddleware)

# ── Routers ─────────────────────────────────────────────────

app.include_router(polish.router, prefix="/api/v1")
app.include_router(draft.router, prefix="/api/v1")
app.include_router(summary.router, prefix="/api/v1")
app.include_router(quality.router, prefix="/api/v1")
app.include_router(moderation.router, prefix="/api/v1")
app.include_router(topics.router, prefix="/api/v1")
app.include_router(recommend.router, prefix="/api/v1")


# ── Health & info endpoints (skip auth) ─────────────────────


@app.get("/health", response_model=HealthResponse, tags=["system"])
async def health():
    return HealthResponse(status="ok", version=settings.APP_VERSION)


@app.get("/ready", response_model=HealthResponse, tags=["system"])
async def ready():
    return HealthResponse(status="ok", version=settings.APP_VERSION)


@app.get("/version", response_model=ApiResponse, tags=["system"])
async def version():
    return ApiResponse(data={"version": settings.APP_VERSION, "appName": settings.APP_NAME})


# ── Startup / shutdown ──────────────────────────────────────


@app.on_event("startup")
async def startup():
    logger.info("ai_service_startup", host=settings.HOST, port=settings.PORT)


@app.on_event("shutdown")
async def shutdown():
    logger.info("ai_service_shutdown")
