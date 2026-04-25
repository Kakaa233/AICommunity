# Wego AI Service

AI 辅助模块独立服务，为 [Wego 校园社区](../../) 提供 7 项 AI 能力。

## 架构概览

```
┌──────────────┐   HTTP (X-Service-Key)   ┌──────────────┐
│  Java 后端    │ ────────────────────────> │  AI Service  │
│  (SpringBoot) │ <──────────────────────── │  (FastAPI)   │
└──────────────┘       JSON Response       └──────┬───────┘
                                                   │
                                          ┌────────v────────┐
                                          │  Third-party LLM │
                                          │  (OpenAI-compat) │
                                          └─────────────────┘
```

## 快速开始

### 1. 环境准备

```bash
cd ai-service
python -m venv .venv
.venv\Scripts\activate    # Windows
# source .venv/bin/activate  # Linux / macOS
```

### 2. 安装依赖

```bash
pip install -r requirements.txt
```

### 3. 配置

```bash
cp .env.example .env
# 编辑 .env，填入 MODEL_API_KEY 等必要配置
```

### 4. 启动

```bash
uvicorn app.main:app --reload --port 8000
```

服务将在 http://localhost:8000 启动，自动加载的 API 文档在 http://localhost:8000/docs。

## API 清单

| 端点 | 能力 | 超时 | 模式 |
|------|------|------|------|
| `POST /api/v1/polish` | 内容润色 | 3s | 同步 |
| `POST /api/v1/draft` | 问答草稿 | 3s | 同步 |
| `POST /api/v1/summary` | 长文摘要与要点 | 3s | 同步 |
| `POST /api/v1/quality` | 质量评分 | 2s | 同步 |
| `POST /api/v1/moderation` | 内容审核 | 3s | 异步 |
| `POST /api/v1/topics` | 专题聚合 | 5s | 异步 |
| `POST /api/v1/recommend` | 相关推荐 | 1.5s | 异步+缓存 |

## 鉴权

所有请求（除 `/health`、`/ready`、`/version`）需携带：

- `X-Service-Key`: `HMAC-SHA256(timestamp + ":" + SERVICE_KEY, SERVICE_KEY)`
- `X-Timestamp`: Unix epoch 毫秒

## 项目结构

```
ai-service/
├── app/
│   ├── main.py              # FastAPI 入口
│   ├── api/v1/              # API 路由（7 个模块）
│   ├── clients/             # 外部客户端（LLM）
│   ├── core/                # 配置、日志、鉴权
│   ├── models/              # Pydantic 请求/响应
│   └── services/            # 业务逻辑（待实现）
├── tests/                   # 测试
├── requirements.txt
├── .env.example
└── README.md
```
