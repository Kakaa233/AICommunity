"""Quick integration test script for Wego AI Service.

Usage:
    # 1. 确保服务已启动 (另一个终端)
    cd ai-service
    $env:MODEL_API_KEY="sk-xxx"; .\.venv\Scripts\python.exe -m uvicorn app.main:app --reload --port 8000

    # 2. 运行测试
    cd ai-service
    .\.venv\Scripts\python.exe test_all.py

    # 或者指定自定义参数
    .\.venv\Scripts\python.exe test_all.py --base-url http://127.0.0.1:8000 --service-key test-key-123
"""

from __future__ import annotations

import argparse
import hashlib
import hmac
import json
import sys
import time
from datetime import datetime
from typing import Any

import httpx

BASE_URL = "http://127.0.0.1:8000"
SERVICE_KEY = "test-key-123"


def make_headers(secret: str) -> dict[str, str]:
    """Compute HMAC-SHA256 signature header."""
    ts = str(int(time.time() * 1000))
    msg = f"{ts}:{secret}"
    sig = hmac.new(secret.encode(), msg.encode(), hashlib.sha256).hexdigest()
    return {
        "X-Service-Key": sig,
        "X-Timestamp": ts,
        "Content-Type": "application/json",
    }


def ok(label: str, resp: httpx.Response) -> bool:
    elapsed = resp.elapsed.total_seconds() * 1000
    try:
        data = resp.json()
    except Exception:
        print(f"  ✗ {label}: {resp.status_code} (not JSON, {elapsed:.0f}ms)")
        return False

    code = data.get("code", -1)
    fallback = data.get("fallbackHit", False)
    model = data.get("model", "?")
    latency = data.get("latencyMs", elapsed)
    status = "✓" if resp.status_code == 200 else "✗"
    fb = " [FALLBACK]" if fallback else ""
    print(f"  {status} {label}: code={code} {latency:.0f}ms model={model}{fb}")
    if data.get("data"):
        preview = json.dumps(data["data"], ensure_ascii=False, default=str)
        if len(preview) > 120:
            preview = preview[:120] + "..."
        print(f"       data: {preview}")
    return resp.status_code == 200


def run_tests(base_url: str, secret: str):
    headers = make_headers(secret)
    client = httpx.Client(base_url=base_url, headers=headers, timeout=30)
    passed = failed = 0

    # ── 1. Health (no auth) ──────────────────────────────────
    print("\n── 系统 ──")
    r = client.get("/health", headers={"Content-Type": "application/json"})
    if ok("GET /health", r):
        passed += 1
    else:
        failed += 1

    # ── 2. Polish (内容润色) ─────────────────────────────────
    print("\n── 1. 内容润色 /api/v1/polish ──")
    body = {
        "title": "如何学好Java后端开发",
        "content": "我是一名大二计算机专业的学生，目前学了Java基础语法和MySQL，想请教一下接下来该怎么系统学习Java后端开发，需要学哪些框架和技术栈？",
    }
    r = client.post("/api/v1/polish", json=body)
    if ok("POST /api/v1/polish", r):
        passed += 1
    else:
        failed += 1

    # ── 3. Draft (问答草稿) ──────────────────────────────────
    print("\n── 2. 问答草稿 /api/v1/draft ──")
    body = {
        "title": "考研和就业如何选择",
        "description": "现在大二下学期了，很纠结是考研还是直接就业。计算机专业，成绩中等，有没有过来人给点建议？",
    }
    r = client.post("/api/v1/draft", json=body)
    if ok("POST /api/v1/draft", r):
        passed += 1
    else:
        failed += 1

    # ── 4. Summary (长文摘要) ────────────────────────────────
    print("\n── 3. 长文摘要 /api/v1/summary ──")
    long_text = (
        "在当今数字化时代，编程已经成为一项核心技能。"
        "越来越多的非计算机专业学生也开始学习编程。"
        "Python因其简洁的语法和丰富的库生态，成为入门首选。"
        "据统计，2024年全球有超过3000万Python开发者。"
        "学习编程不仅能提升逻辑思维能力，还能为解决实际问题提供新思路。"
        "本文将从编程语言选择、学习路径规划、项目实战三个方面展开讨论。"
        "在语言选择上，建议根据目标领域决定：Web开发选JavaScript，"
        "数据分析选Python，移动开发选Kotlin或Swift。"
        "学习路径方面，建议先掌握基础语法，再做2-3个小项目巩固，"
        "最后参与开源或实习积累经验。"
        * 5
    )
    r = client.post("/api/v1/summary", json={"content": long_text})
    if ok("POST /api/v1/summary", r):
        passed += 1
    else:
        failed += 1

    # ── 5. Quality (质量评分) ────────────────────────────────
    print("\n── 4. 质量评分 /api/v1/quality ──")
    body = {
        "title": "Java多线程编程入门指南",
        "content": "本文介绍了Java多线程的基本概念，包括Thread和Runnable的使用方式，"
        "以及synchronized关键字的作用。文章结构清晰，但缺少实际案例。",
    }
    r = client.post("/api/v1/quality", json=body)
    if ok("POST /api/v1/quality", r):
        passed += 1
    else:
        failed += 1

    # ── 6. Moderation (内容审核) ─────────────────────────────
    print("\n── 5. 内容审核 /api/v1/moderation ──")
    body = {
        "contentId": "12345",
        "title": "今天天气真好，去图书馆学习",
        "content": "阳光明媚，适合去图书馆看书复习。有一起组队的吗？",
        "authorId": "user_001",
    }
    r = client.post("/api/v1/moderation", json=body)
    if ok("POST /api/v1/moderation", r):
        passed += 1
    else:
        failed += 1

    # ── 7. Topics (专题聚合) ────────────────────────────────
    print("\n── 6. 专题聚合 /api/v1/topics ──")
    r = client.post("/api/v1/topics", json={"timeRangeDays": 7, "maxTopics": 5})
    if ok("POST /api/v1/topics", r):
        passed += 1
    else:
        failed += 1

    # ── 8. Recommend (相关推荐) ──────────────────────────────
    print("\n── 7. 相关推荐 /api/v1/recommend ──")
    r = client.post("/api/v1/recommend", json={"articleId": "10086", "limit": 6})
    if ok("POST /api/v1/recommend", r):
        passed += 1
    else:
        failed += 1

    # ── Summary ──────────────────────────────────────────────
    print(f"\n{'='*50}")
    total = passed + failed
    print(f"结果: {passed}/{total} 通过, {failed} 失败")
    return failed == 0


def main():
    parser = argparse.ArgumentParser(description="Test Wego AI Service endpoints")
    parser.add_argument("--base-url", default=BASE_URL, help="Service base URL")
    parser.add_argument("--service-key", default=SERVICE_KEY, help="Service secret key")
    args = parser.parse_args()

    print(f"测试 AI 服务: {args.base_url}")
    print(f"Service Key: {args.service_key}")
    print(f"时间戳: {datetime.now().isoformat()}")

    success = run_tests(args.base_url, args.service_key)
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
