#!/bin/bash

# =============================================
# 执行 SQL 文件的辅助脚本
# 用于在 MySQL 客户端中执行 SQL 文件
# =============================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================="
echo "SQL 执行说明"
echo "=========================================="
echo ""
echo "由于系统未找到 mysql 命令行工具，请使用以下方式之一执行："
echo ""
echo "方式 1: 在 MySQL 客户端中执行"
echo "  1. 打开 MySQL 客户端（如 MySQL Workbench、Navicat、DBeaver 等）"
echo "  2. 连接到数据库"
echo "  3. 执行以下 SQL 文件："
echo ""
echo "清理数据库："
echo "  source ${DB_DIR}/clean-all.sql;"
echo ""
echo "初始化数据库："
echo "  source ${DB_DIR}/init.sql;"
echo ""
echo "方式 2: 使用 MySQL 命令行（如果已安装）"
echo "  mysql -uroot -proot < ${DB_DIR}/clean-all.sql"
echo "  mysql -uroot -proot < ${DB_DIR}/init.sql"
echo ""
echo "方式 3: 复制 SQL 内容手动执行"
echo "  打开 SQL 文件，复制内容到 MySQL 客户端执行"
echo ""
