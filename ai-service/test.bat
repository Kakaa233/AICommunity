@echo off
REM AI 服务一键测试脚本
cd /d "%~dp0"

echo 正在测试 AI 服务 (http://127.0.0.1:8000)
echo 确保服务已经在另一个终端中运行！
echo.

.venv\Scripts\python.exe test_all.py

pause
