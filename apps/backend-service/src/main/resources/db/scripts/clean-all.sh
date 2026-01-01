#!/bin/bash

# =============================================
# Vben Admin 数据库清理脚本
# 用于清理所有表和数据，便于重新初始化
# =============================================

set -e

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# 默认配置
MYSQL_CONTAINER="${MYSQL_CONTAINER:-mysql-vben}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"
MYSQL_DATABASE="${MYSQL_DATABASE:-vben_admin}"
MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
USE_DOCKER="${USE_DOCKER:-auto}"
SKIP_CONFIRM="${SKIP_CONFIRM:-false}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检测 MySQL 连接方式
detect_mysql() {
    if [ "$USE_DOCKER" = "auto" ]; then
        # 自动检测 Docker 容器
        if docker ps --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
            USE_DOCKER="true"
            echo -e "${GREEN}检测到 Docker MySQL 容器: ${MYSQL_CONTAINER}${NC}"
        elif docker ps --format '{{.Names}}' | grep -qi mysql; then
            # 尝试查找其他 MySQL 容器
            FOUND_CONTAINER=$(docker ps --format '{{.Names}}' | grep -i mysql | head -n1)
            echo -e "${YELLOW}未找到容器 '${MYSQL_CONTAINER}'，但找到其他 MySQL 容器: ${FOUND_CONTAINER}${NC}"
            read -p "是否使用此容器? (y/n): " use_found
            if [ "$use_found" = "y" ] || [ "$use_found" = "Y" ]; then
                MYSQL_CONTAINER="$FOUND_CONTAINER"
                USE_DOCKER="true"
            else
                USE_DOCKER="false"
            fi
        else
            USE_DOCKER="false"
            echo -e "${YELLOW}未找到 Docker MySQL 容器，将使用本地 MySQL${NC}"
        fi
    fi
}

# 执行清理 SQL
execute_clean() {
    if [ "$USE_DOCKER" = "true" ]; then
        echo -e "${GREEN}使用 Docker 执行清理...${NC}"
        docker exec -i "${MYSQL_CONTAINER}" mysql -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" "${MYSQL_DATABASE}" < "${DB_DIR}/clean-all.sql"
    else
        echo -e "${GREEN}使用本地 MySQL 执行清理...${NC}"
        mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" "${MYSQL_DATABASE}" < "${DB_DIR}/clean-all.sql"
    fi
}

# 主函数
main() {
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}Vben Admin 数据库清理脚本${NC}"
    echo -e "${YELLOW}========================================${NC}"
    echo ""

    # 检测 MySQL 连接方式
    detect_mysql
    echo ""

    # 确认操作
    if [ "$SKIP_CONFIRM" != "true" ]; then
        echo -e "${RED}警告: 此操作将删除数据库 '${MYSQL_DATABASE}' 中的所有表和数据！${NC}"
        read -p "确认继续? (yes/no): " confirm

        if [ "$confirm" != "yes" ]; then
            echo "操作已取消"
            exit 0
        fi
    fi

    echo ""
    echo -e "${GREEN}开始清理数据库...${NC}"

    # 执行清理脚本
    if execute_clean; then
        echo ""
        echo -e "${GREEN}✓ 数据库清理完成${NC}"
        echo ""
        echo -e "${GREEN}可以运行以下命令重新初始化数据库:${NC}"
        echo "  ${DB_DIR}/scripts/init-all.sh"
    else
        echo ""
        echo -e "${RED}✗ 数据库清理失败${NC}"
        exit 1
    fi
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
    --no-docker                  强制使用本地 MySQL
    --docker                     强制使用 Docker MySQL
    -y, --yes                    跳过确认提示
    --help                       显示此帮助信息

环境变量:
    MYSQL_CONTAINER               Docker 容器名称
    MYSQL_USER                    MySQL 用户名
    MYSQL_PASSWORD                MySQL 密码
    MYSQL_DATABASE                数据库名称（默认: vben_admin）
    MYSQL_HOST                    MySQL 主机（仅本地模式）
    MYSQL_PORT                    MySQL 端口（仅本地模式）
    USE_DOCKER                    是否使用 Docker（auto/true/false）
    SKIP_CONFIRM                  跳过确认（true/false）

示例:
    # 自动检测并使用 Docker 或本地 MySQL
    $0

    # 使用 Docker（跳过确认）
    $0 -y

    # 使用本地 MySQL
    $0 --no-docker -u root -p mypassword

    # 使用环境变量
    MYSQL_CONTAINER=my-mysql SKIP_CONFIRM=true $0
EOF
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--container)
            MYSQL_CONTAINER="$2"
            USE_DOCKER="true"
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
            USE_DOCKER="false"
            shift 2
            ;;
        -P|--port)
            MYSQL_PORT="$2"
            USE_DOCKER="false"
            shift 2
            ;;
        --no-docker)
            USE_DOCKER="false"
            shift
            ;;
        --docker)
            USE_DOCKER="true"
            shift
            ;;
        -y|--yes)
            SKIP_CONFIRM="true"
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
