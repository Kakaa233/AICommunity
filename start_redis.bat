@echo off
chcp 65001 >nul
set "REDIS_EXE=F:\应用\软件\Redis-8.6.2-Windows-x64-cygwin-with-Service\Redis-8.6.2-Windows-x64-cygwin-with-Service\redis-server.exe"

if not exist "%REDIS_EXE%" (
    echo [错误] 未找到 Redis 可执行文件：
    echo %REDIS_EXE%
    exit /b 1
)

start "Redis Server" "%REDIS_EXE%"
echo [完成] Redis 服务已启动。