#!/bin/bash
# ============================================================
# 项目计划看板 - 本地一键启动脚本
# 使用方式：bash start-local.sh
# ============================================================

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_HOME_17="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
LOG_DIR="$PROJECT_DIR/.local-logs"

mkdir -p "$LOG_DIR"

echo "======================================"
echo " 项目计划看板 - 本地开发环境启动"
echo "======================================"

# ── Step 1：启动 MySQL ───────────────────
echo ""
echo "[1/3] 检查并启动 MySQL..."
if mysqladmin ping --silent 2>/dev/null; then
    echo "✅ MySQL 已在运行"
else
    echo "  启动 MySQL..."
    mysqld_safe --datadir=/opt/homebrew/var/mysql > "$LOG_DIR/mysql.log" 2>&1 &
    sleep 3
    if mysqladmin ping --silent 2>/dev/null; then
        echo "✅ MySQL 启动成功"
    else
        echo "❌ MySQL 启动失败，请手动检查"
        echo "   日志：$LOG_DIR/mysql.log"
        exit 1
    fi
fi

# ── Step 2：启动后端 ─────────────────────
echo ""
echo "[2/3] 启动后端服务（Java 17）..."

# 检查 Java 17
if [ ! -d "$JAVA_HOME_17" ]; then
    echo "❌ 找不到 Java 17：$JAVA_HOME_17"
    exit 1
fi

export JAVA_HOME="$JAVA_HOME_17"

# 停止已有后端进程，等待端口完全释放
EXISTING_PID=$(lsof -ti:8080 2>/dev/null)
if [ -n "$EXISTING_PID" ]; then
    echo "  停止占用 8080 端口的进程（PID=$EXISTING_PID）..."
    kill -9 $EXISTING_PID 2>/dev/null
    echo -n "  等待端口释放..."
    for i in $(seq 1 15); do
        sleep 1
        if ! lsof -ti:8080 &>/dev/null; then
            echo " 完成"
            break
        fi
        echo -n "."
    done
fi

cd "$PROJECT_DIR/backend"
mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "  后端启动中（PID=$BACKEND_PID），等待就绪..."

for i in $(seq 1 30); do
    sleep 2
    if grep -q "Started DashboardApplication" "$LOG_DIR/backend.log" 2>/dev/null; then
        echo "✅ 后端启动成功 → http://localhost:8080"
        break
    fi
    if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
        echo "❌ 后端进程异常退出"
        echo "   查看日志：cat $LOG_DIR/backend.log"
        exit 1
    fi
    echo "  等待中... ($i/30)"
done

# ── Step 3：启动前端 ─────────────────────
echo ""
echo "[3/3] 启动前端服务..."

# 停止已有前端进程
EXISTING_FE_PID=$(lsof -ti:5173 2>/dev/null)
if [ -n "$EXISTING_FE_PID" ]; then
    echo "  停止占用 5173 端口的进程（PID=$EXISTING_FE_PID）..."
    kill "$EXISTING_FE_PID" 2>/dev/null
    sleep 1
fi

cd "$PROJECT_DIR/frontend"
npm run dev > "$LOG_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
sleep 3

if kill -0 "$FRONTEND_PID" 2>/dev/null; then
    echo "✅ 前端启动成功 → http://localhost:5173"
else
    echo "❌ 前端进程异常退出"
    echo "   查看日志：cat $LOG_DIR/frontend.log"
    exit 1
fi

# ── 完成 ────────────────────────────────
echo ""
echo "======================================"
echo "✅ 本地环境启动完成！"
echo ""
echo "   前端地址：http://localhost:5173"
echo "   后端地址：http://localhost:8080"
echo ""
echo "   管理员账号：admin / admin123"
echo ""
echo "   日志目录：$LOG_DIR/"
echo "     后端日志：tail -f $LOG_DIR/backend.log"
echo "     前端日志：tail -f $LOG_DIR/frontend.log"
echo ""
echo "   停止所有服务：bash stop-local.sh"
echo "======================================"

# 保存 PID 供停止脚本使用
echo "$BACKEND_PID" > "$LOG_DIR/backend.pid"
echo "$FRONTEND_PID" > "$LOG_DIR/frontend.pid"

# 等待，捕获 Ctrl+C 时提示
trap 'echo ""; echo "脚本已退出，服务仍在后台运行。执行 bash stop-local.sh 停止所有服务。"' INT
wait
