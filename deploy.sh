#!/bin/bash
# ============================================================
# 项目计划看板系统 - 一键部署脚本
# 使用方式：bash deploy.sh
# 前置条件：本地已完成 npm run build 和 mvn package
# ============================================================

set -e

SERVER="root@47.103.56.254"
SERVER_DIR="/opt/dashboard"
JAR="backend/target/dashboard-backend-1.0.0.jar"
DIST="frontend/dist"
SQL="backend/src/main/resources/sql/init_prod.sql"

echo "======================================"
echo " 项目计划看板 - 部署到 47.103.56.254"
echo "======================================"

# ── Step 1：检查本地产物 ─────────────────
echo ""
echo "[1/6] 检查本地构建产物..."
[ -f "$JAR" ] || { echo "❌ 找不到 $JAR，请先执行 mvn package"; exit 1; }
[ -d "$DIST" ] || { echo "❌ 找不到 $DIST，请先执行 npm run build"; exit 1; }
echo "✅ 产物检查通过"

# ── Step 2：服务器环境初始化 ─────────────
echo ""
echo "[2/6] 初始化服务器环境..."
ssh "$SERVER" bash << 'REMOTE'
set -e

# 检测包管理器
if command -v apt &>/dev/null; then
    PKG="apt"
    apt update -qq
elif command -v yum &>/dev/null; then
    PKG="yum"
fi

# Java 17
if ! java -version 2>&1 | grep -q "17"; then
    echo "  安装 Java 17..."
    [ "$PKG" = "apt" ] && apt install -y openjdk-17-jdk -qq || yum install -y java-17-openjdk -q
fi
echo "  Java: $(java -version 2>&1 | head -1)"

# MySQL
if ! command -v mysql &>/dev/null; then
    echo "  安装 MySQL..."
    [ "$PKG" = "apt" ] && apt install -y mysql-server -qq || yum install -y mysql-server -q
    systemctl start mysql 2>/dev/null || systemctl start mysqld 2>/dev/null || true
    systemctl enable mysql 2>/dev/null || systemctl enable mysqld 2>/dev/null || true
fi
echo "  MySQL: $(mysql --version)"

# Nginx
if ! command -v nginx &>/dev/null; then
    echo "  安装 Nginx..."
    [ "$PKG" = "apt" ] && apt install -y nginx -qq || yum install -y nginx -q
    systemctl start nginx
    systemctl enable nginx
fi
echo "  Nginx: $(nginx -v 2>&1)"

# 创建目录
mkdir -p /opt/dashboard/uploads
echo "  目录 /opt/dashboard ✅"
REMOTE
echo "✅ 服务器环境就绪"

# ── Step 3：初始化数据库 ─────────────────
echo ""
echo "[3/6] 初始化数据库..."
scp "$SQL" "$SERVER:/tmp/init_prod.sql"
ssh "$SERVER" bash << 'REMOTE'
# 尝试执行 SQL（root 无密码）
if mysql -u root -e "SHOW DATABASES;" &>/dev/null 2>&1; then
    mysql -u root < /tmp/init_prod.sql
    echo "  数据库初始化完成（无密码）"
else
    echo "  ⚠️  MySQL root 有密码，请手动执行："
    echo "     mysql -u root -p < /tmp/init_prod.sql"
fi
REMOTE
echo "✅ 数据库步骤完成"

# ── Step 4：上传并启动后端 ───────────────
echo ""
echo "[4/6] 上传后端 jar（25MB，稍等...）"
scp "$JAR" "$SERVER:$SERVER_DIR/dashboard-backend-1.0.0.jar"
echo "✅ jar 上传完成"

echo ""
echo "[5/6] 启动后端服务..."
ssh "$SERVER" bash << 'REMOTE'
set -e
# 停止旧进程
if [ -f /opt/dashboard/app.pid ]; then
    OLD_PID=$(cat /opt/dashboard/app.pid)
    kill "$OLD_PID" 2>/dev/null && echo "  已停止旧进程 PID=$OLD_PID" || true
    sleep 2
fi

# 启动新进程
cd /opt/dashboard
nohup java -jar dashboard-backend-1.0.0.jar \
    --server.port=8080 \
    > /opt/dashboard/app.log 2>&1 &
echo $! > /opt/dashboard/app.pid
echo "  后端启动中，PID=$(cat app.pid)..."

# 等待启动
for i in $(seq 1 20); do
    sleep 2
    if grep -q "Started DashboardApplication" /opt/dashboard/app.log 2>/dev/null; then
        echo "  ✅ 后端启动成功"
        exit 0
    fi
    echo "  等待中... ($i/20)"
done
echo "  ⚠️  超时，请查看日志：tail -f /opt/dashboard/app.log"
REMOTE

# ── Step 5：上传前端 + 配置 Nginx ────────
echo ""
echo "[6/6] 上传前端并配置 Nginx..."
scp -r "$DIST/." "$SERVER:$SERVER_DIR/frontend"

ssh "$SERVER" bash << 'REMOTE'
# 写 Nginx 配置
cat > /etc/nginx/conf.d/dashboard.conf << 'NGINX'
server {
    listen 80;
    server_name _;

    root /opt/dashboard/frontend;
    index index.html;

    client_max_body_size 10m;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 60s;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080;
    }
}
NGINX

nginx -t && systemctl reload nginx
echo "  ✅ Nginx 配置完成并重载"
REMOTE

# ── 完成 ───────────────────────────────
echo ""
echo "======================================"
echo "✅ 部署完成！"
echo "   访问地址：http://47.103.56.254"
echo "   管理员账号：admin / admin123"
echo "   查看日志：ssh root@10.10.0.207 'tail -f /opt/dashboard/app.log'"
echo "======================================"
