#!/bin/bash
# ============================================================
# 项目计划看板 - 本地服务停止脚本
# 使用方式：bash stop-local.sh
# ============================================================

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$PROJECT_DIR/.local-logs"

echo "======================================"
echo " 项目计划看板 - 停止本地服务"
echo "======================================"

# 停止后端
echo ""
echo "停止后端（端口 8080）..."
PID_FILE="$LOG_DIR/backend.pid"
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        kill "$PID" && echo "✅ 后端已停止（PID=$PID）"
    else
        echo "  后端进程不存在，尝试按端口查找..."
    fi
    rm -f "$PID_FILE"
fi
LEFTOVERS=$(lsof -ti:8080 2>/dev/null)
if [ -n "$LEFTOVERS" ]; then
    kill $LEFTOVERS 2>/dev/null && echo "✅ 已终止占用 8080 端口的进程"
fi

# 停止前端
echo ""
echo "停止前端（端口 5173）..."
PID_FILE="$LOG_DIR/frontend.pid"
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        kill "$PID" && echo "✅ 前端已停止（PID=$PID）"
    else
        echo "  前端进程不存在，尝试按端口查找..."
    fi
    rm -f "$PID_FILE"
fi
LEFTOVERS=$(lsof -ti:5173 2>/dev/null)
if [ -n "$LEFTOVERS" ]; then
    kill $LEFTOVERS 2>/dev/null && echo "✅ 已终止占用 5173 端口的进程"
fi

echo ""
echo "======================================"
echo "✅ 所有服务已停止"
echo "   MySQL 未停止（保持运行，下次启动更快）"
echo "======================================"
