@echo off
REM 启动 AI 服务前的设置
cd /d "%~dp0"

REM 检查 .env 文件
if not exist .env (
    echo [提示] 未发现 .env 文件，正在从 .env.example 复制...
    copy .env.example .env >nul
    echo [提示] 请编辑 .env 填入你的 MODEL_API_KEY，然后重新运行本脚本
    echo.
    echo 临时测试模式：不设 API Key 也可以启动，AI 接口将返回降级空数据
)

echo 启动 Wego AI Service...
echo 地址: http://127.0.0.1:8000
echo 文档: http://127.0.0.1:8000/docs
echo.

.venv\Scripts\python.exe -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000

pause
