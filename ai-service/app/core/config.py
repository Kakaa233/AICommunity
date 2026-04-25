"""Centralized configuration via environment variables with pydantic-settings."""

from __future__ import annotations

from pathlib import Path
from typing import ClassVar

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # ── Service ──────────────────────────────────────────────
    APP_NAME: str = "wego-ai-service"
    APP_VERSION: str = "0.1.0"
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    LOG_LEVEL: str = "INFO"
    DEBUG: bool = False

    # ── Auth ─────────────────────────────────────────────────
    SERVICE_KEY: str = "change-me-in-production"
    """Shared secret used to verify X-Service-Key HMAC signature from Java backend."""

    # ── Model API (third-party, DeepSeek as default) ─────────
    MODEL_API_BASE: str = "https://api.deepseek.com/v1"
    MODEL_API_KEY: str = ""
    MODEL_NAME: str = "deepseek-chat"
    MODEL_MAX_TOKENS: int = 2048
    MODEL_TEMPERATURE: float = 0.7
    MODEL_TIMEOUT_SEC: int = 30

    # ── Per-endpoint timeouts (seconds) ──────────────────────
    TIMEOUT_POLISH: float = 3.0
    TIMEOUT_DRAFT: float = 3.0
    TIMEOUT_SUMMARY: float = 3.0
    TIMEOUT_QUALITY: float = 2.0
    TIMEOUT_MODERATION: float = 3.0
    TIMEOUT_TOPICS: float = 5.0
    TIMEOUT_RECOMMEND: float = 1.5

    # ── Retry ────────────────────────────────────────────────
    RETRY_MAX_ATTEMPTS: int = 2
    RETRY_MIN_WAIT_SEC: float = 0.5
    RETRY_MAX_WAIT_SEC: float = 2.0

    # ── Rate-limit ───────────────────────────────────────────
    RATE_LIMIT_PER_MIN: int = 60

    # ── Paths ────────────────────────────────────────────────
    BASE_DIR: ClassVar[Path] = Path(__file__).resolve().parent.parent.parent

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
    )


settings = Settings()
