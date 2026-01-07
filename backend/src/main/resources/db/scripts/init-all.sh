#!/bin/bash

# =============================================
# Vben Admin 数据库模块化初始化脚本
# =============================================
# 说明：
# 1. 按依赖顺序执行所有模块的 schema 和 data 文件
# 2. 支持 Docker MySQL 和本地 MySQL
# 3. 使用 UTF-8 编码执行所有 SQL 文件
# =============================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
MODULES_DIR="$DB_DIR/modules"

# 默认配置
MYSQL_CONTAINER="${MYSQL_CONTAINER:-mysql-vben}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"
MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
USE_DOCKER="${USE_DOCKER:-true}"

# 检查 MySQL 连接
check_mysql() {
    if [ "$USE_DOCKER" = "true" ]; then
        if ! docker ps | grep -q "$MYSQL_CONTAINER"; then
            echo -e "${RED}错误: Docker 容器 '$MYSQL_CONTAINER' 未运行${NC}"
            exit 1
        fi
    else
        if ! command -v mysql &> /dev/null; then
            echo -e "${RED}错误: mysql 命令未找到${NC}"
            exit 1
        fi
    fi
}

# 执行 SQL 文件
execute_sql() {
    local sql_file=$1
    local description=$2

    if [ ! -f "$sql_file" ]; then
        echo -e "${YELLOW}警告: 文件不存在，跳过: $sql_file${NC}"
        return
    fi

    echo -e "${GREEN}执行: $description${NC}"
    echo -e "  文件: $sql_file"

    if [ "$USE_DOCKER" = "true" ]; then
        docker exec -i "$MYSQL_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --default-character-set=utf8mb4 vben_admin < "$sql_file"
    else
        mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --default-character-set=utf8mb4 vben_admin < "$sql_file"
    fi

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}  ✓ 成功${NC}"
    else
        echo -e "${RED}  ✗ 失败${NC}"
        exit 1
    fi
}

# 主函数
main() {
    echo -e "${GREEN}=============================================${NC}"
    echo -e "${GREEN}Vben Admin 数据库模块化初始化${NC}"
    echo -e "${GREEN}=============================================${NC}"
    echo ""

    # 检查 MySQL 连接
    check_mysql

    # 1. 初始化数据库和字符集
    echo -e "${YELLOW}步骤 1: 初始化数据库${NC}"
    execute_sql "$DB_DIR/init.sql" "创建数据库和设置字符集"
    echo ""

    # 2. 基础模块（无依赖）
    echo -e "${YELLOW}步骤 2: 初始化基础模块${NC}"
    
    # 2.1 部门模块（用户需要关联部门）
    echo -e "${GREEN}  → 部门模块${NC}"
    execute_sql "$MODULES_DIR/dept/dept-schema.sql" "创建部门表"
    execute_sql "$MODULES_DIR/dept/dept-data.sql" "初始化部门数据"
    echo ""

    # 2.2 角色模块
    echo -e "${GREEN}  → 角色模块${NC}"
    execute_sql "$MODULES_DIR/role/role-schema.sql" "创建角色表"
    execute_sql "$MODULES_DIR/role/role-data.sql" "初始化角色数据"
    echo ""

    # 2.3 菜单模块
    echo -e "${GREEN}  → 菜单模块${NC}"
    execute_sql "$MODULES_DIR/menu/menu-schema.sql" "创建菜单表"
    execute_sql "$MODULES_DIR/menu/menu-data.sql" "初始化菜单数据"
    echo ""

    # 2.4 权限模块
    echo -e "${GREEN}  → 权限模块${NC}"
    execute_sql "$MODULES_DIR/permission/permission-schema.sql" "创建权限表"
    execute_sql "$MODULES_DIR/permission/permission-data.sql" "初始化权限数据"
    echo ""

    # 3. 业务模块（依赖基础模块）
    echo -e "${YELLOW}步骤 3: 初始化业务模块${NC}"
    
    # 3.1 用户模块（依赖部门）
    echo -e "${GREEN}  → 用户模块${NC}"
    execute_sql "$MODULES_DIR/user/user-schema.sql" "创建用户表"
    execute_sql "$MODULES_DIR/user/user-data.sql" "初始化用户数据"
    echo ""

    # 4. 关联模块（依赖所有基础模块）
    echo -e "${YELLOW}步骤 4: 初始化关联关系模块${NC}"
    execute_sql "$MODULES_DIR/relation/relation-schema.sql" "创建关联表"
    execute_sql "$MODULES_DIR/relation/relation-data.sql" "初始化关联关系数据"
    echo ""

    # 5. 日志模块（独立，依赖用户）
    echo -e "${YELLOW}步骤 5: 初始化操作日志模块${NC}"
    execute_sql "$MODULES_DIR/operation-log/operation-log-schema.sql" "创建操作日志表"
    execute_sql "$MODULES_DIR/operation-log/operation-log-data.sql" "初始化操作日志数据（如需要）"
    echo ""

    echo -e "${GREEN}=============================================${NC}"
    echo -e "${GREEN}数据库初始化完成！${NC}"
    echo -e "${GREEN}=============================================${NC}"
}

# 显示帮助信息
show_help() {
    cat << EOF
用法: $0 [选项]

选项:
    -c, --container CONTAINER    Docker 容器名称（默认: mysql-vben）
    -u, --user USER              MySQL 用户名（默认: root）
    -p, --password PASSWORD      MySQL 密码（默认: root）
    -h, --host HOST              MySQL 主机（默认: localhost，仅本地模式）
    -P, --port PORT              MySQL 端口（默认: 3306，仅本地模式）
    --no-docker                  使用本地 MySQL 而不是 Docker
    --help                       显示此帮助信息

环境变量:
    MYSQL_CONTAINER               Docker 容器名称
    MYSQL_USER                    MySQL 用户名
    MYSQL_PASSWORD                MySQL 密码
    MYSQL_HOST                    MySQL 主机（仅本地模式）
    MYSQL_PORT                    MySQL 端口（仅本地模式）
    USE_DOCKER                    是否使用 Docker（true/false）

示例:
    # 使用默认配置（Docker）
    $0

    # 使用自定义 Docker 容器
    $0 -c my-mysql-container

    # 使用本地 MySQL
    $0 --no-docker -u root -p mypassword

    # 使用环境变量
    MYSQL_CONTAINER=my-mysql $0
EOF
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--container)
            MYSQL_CONTAINER="$2"
            shift 2
            ;;
        -u|--user)
            MYSQL_USER="$2"
            shift 2
            ;;
        -p|--password)
            MYSQL_PASSWORD="$2"
            shift 2
            ;;
        -h|--host)
            MYSQL_HOST="$2"
            shift 2
            ;;
        -P|--port)
            MYSQL_PORT="$2"
            shift 2
            ;;
        --no-docker)
            USE_DOCKER="false"
            shift
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}未知选项: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# 执行主函数
main
