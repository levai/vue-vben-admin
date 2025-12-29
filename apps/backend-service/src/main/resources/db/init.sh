#!/bin/bash

# =============================================
# Vben Admin 数据库一键初始化脚本
# 适用于 Docker MySQL 环境
# =============================================
# 功能：
#   1. 首次初始化：创建数据库和表结构，插入初始数据
#   2. 重新初始化：如果数据库已存在，会先删除所有表再重新创建
# =============================================

set -e

# 配置参数（可根据实际情况修改）
CONTAINER_NAME="${MYSQL_CONTAINER:-mysql-vben}"
DB_NAME="vben_admin"
DB_USER="root"
DB_PASSWORD="root"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 是否重新初始化（删除现有表）
REINIT="${REINIT:-false}"

echo "=========================================="
echo "Vben Admin 数据库初始化"
echo "=========================================="
echo "容器名称: $CONTAINER_NAME"
echo "数据库名: $DB_NAME"
echo "脚本目录: $SCRIPT_DIR"
echo ""

# 检查容器是否运行
if ! docker ps --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    echo "❌ 错误: 容器 '$CONTAINER_NAME' 未运行"
    echo ""
    echo "请先启动容器:"
    echo "  docker start $CONTAINER_NAME"
    echo ""
    echo "或者设置正确的容器名称:"
    echo "  export MYSQL_CONTAINER=your-container-name"
    echo "  $0"
    exit 1
fi

echo "✅ 容器运行正常"
echo ""

# 如果指定重新初始化，先删除所有表
if [ "$REINIT" = "true" ]; then
    echo "⚠️  重新初始化模式：将删除所有现有表"
    echo "步骤 0/3: 删除所有表..."
    docker exec -i $CONTAINER_NAME mysql -u$DB_USER -p$DB_PASSWORD $DB_NAME <<EOF
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS \`sys_user_permission\`;
DROP TABLE IF EXISTS \`sys_permission\`;
DROP TABLE IF EXISTS \`sys_role_menu\`;
DROP TABLE IF EXISTS \`sys_user_role\`;
DROP TABLE IF EXISTS \`sys_menu\`;
DROP TABLE IF EXISTS \`sys_dept\`;
DROP TABLE IF EXISTS \`sys_role\`;
DROP TABLE IF EXISTS \`sys_user\`;
SET FOREIGN_KEY_CHECKS = 1;
EOF
    if [ $? -eq 0 ]; then
        echo "✅ 表删除完成"
    else
        echo "⚠️  表删除可能失败（如果表不存在是正常的）"
    fi
    echo ""
fi

# 步骤 1: 创建数据库和表结构
STEP_COUNT=$([ "$REINIT" = "true" ] && echo "3" || echo "2")
STEP_NUM=$([ "$REINIT" = "true" ] && echo "1" || echo "1")
echo "步骤 ${STEP_NUM}/${STEP_COUNT}: 创建数据库和表结构..."
docker exec -i $CONTAINER_NAME mysql -u$DB_USER -p$DB_PASSWORD < "$SCRIPT_DIR/schema.sql"
if [ $? -eq 0 ]; then
    echo "✅ 数据库和表结构创建完成"
else
    echo "❌ 数据库和表结构创建失败"
    exit 1
fi
echo ""

# 步骤 2: 初始化数据（使用 UTF-8 编码）
STEP_NUM=$([ "$REINIT" = "true" ] && echo "2" || echo "2")
echo "步骤 ${STEP_NUM}/${STEP_COUNT}: 初始化数据（UTF-8 编码）..."
docker exec -i $CONTAINER_NAME mysql -u$DB_USER -p$DB_PASSWORD --default-character-set=utf8mb4 $DB_NAME < "$SCRIPT_DIR/data.sql"
if [ $? -eq 0 ]; then
    echo "✅ 数据初始化完成"
else
    echo "❌ 数据初始化失败"
    exit 1
fi
echo ""

# 验证数据
echo "=========================================="
echo "验证数据..."
echo "=========================================="
docker exec -i $CONTAINER_NAME mysql -u$DB_USER -p$DB_PASSWORD --default-character-set=utf8mb4 $DB_NAME <<EOF
SELECT '用户表' as table_name, COUNT(*) as count FROM sys_user
UNION ALL
SELECT '角色表', COUNT(*) FROM sys_role
UNION ALL
SELECT '菜单表', COUNT(*) FROM sys_menu
UNION ALL
SELECT '权限表', COUNT(*) FROM sys_permission;
EOF

echo ""
echo "验证中文数据..."
docker exec -i $CONTAINER_NAME mysql -u$DB_USER -p$DB_PASSWORD --default-character-set=utf8mb4 $DB_NAME -e "SELECT id, name FROM sys_menu WHERE name LIKE '%管理%' ORDER BY sort_order;"

echo ""
echo "=========================================="
echo "✅ 数据库初始化完成！"
echo "=========================================="
echo ""
echo "默认管理员账号: admin / admin123"
echo ""
echo "验证 sort_order 字段:"
docker exec -i $CONTAINER_NAME mysql -u$DB_USER -p$DB_PASSWORD $DB_NAME -e "SHOW COLUMNS FROM sys_menu WHERE Field = 'sort_order';"
echo ""
echo "现在可以启动应用了！"
echo ""
echo "提示：如果需要重新初始化数据库，可以设置环境变量："
echo "  REINIT=true ./src/main/resources/db/init.sh"
