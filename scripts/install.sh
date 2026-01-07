#!/bin/bash

# Vue Vben Admin 项目安装脚本
# 用于管理前后端依赖安装

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
BACKEND_DIR="$ROOT_DIR/backend"

log() {
    echo -e "${CYAN}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查命令是否存在
check_command() {
    if ! command -v "$1" &> /dev/null; then
        error "$1 未安装，请先安装 $1"
        exit 1
    fi
}

# 检查 Node.js 和 pnpm
check_command node
check_command pnpm

# 检查 Java 和 Maven（可选）
if [ "$1" != "--frontend-only" ]; then
    if command -v java &> /dev/null && command -v mvn &> /dev/null; then
        log "检测到 Java 和 Maven，将同时安装后端依赖"
        INSTALL_BACKEND=true
    else
        warning "未检测到 Java 或 Maven，跳过后端依赖安装"
        INSTALL_BACKEND=false
    fi
fi

# 安装前端依赖
install_frontend() {
    log "开始安装前端依赖..."
    cd "$FRONTEND_DIR"
    
    if [ ! -f "package.json" ]; then
        error "frontend/package.json 不存在"
        exit 1
    fi
    
    pnpm install
    
    if [ $? -eq 0 ]; then
        success "前端依赖安装完成"
    else
        error "前端依赖安装失败"
        exit 1
    fi
}

# 安装后端依赖
install_backend() {
    if [ "$INSTALL_BACKEND" != "true" ]; then
        return
    fi
    
    log "开始安装后端依赖..."
    cd "$BACKEND_DIR"
    
    if [ ! -f "pom.xml" ]; then
        warning "backend/pom.xml 不存在，跳过后端依赖安装"
        return
    fi
    
    mvn dependency:resolve -q
    
    if [ $? -eq 0 ]; then
        success "后端依赖安装完成"
    else
        warning "后端依赖安装失败（可能不影响开发）"
    fi
}

# 主函数
main() {
    log "========================================="
    log "Vue Vben Admin 项目依赖安装"
    log "========================================="
    echo ""
    
    # 检查目录
    if [ ! -d "$FRONTEND_DIR" ]; then
        error "frontend 目录不存在"
        exit 1
    fi
    
    # 安装前端依赖
    install_frontend
    
    # 安装后端依赖（如果指定了 --frontend-only，则跳过）
    if [ "$1" != "--frontend-only" ]; then
        install_backend
    fi
    
    echo ""
    success "========================================="
    success "所有依赖安装完成！"
    success "========================================="
    echo ""
    log "使用以下命令启动项目："
    log "  前端开发: cd frontend && pnpm dev:antd"
    log "  后端开发: cd backend && mvn spring-boot:run"
    log "  一键启动: node scripts/start.js"
    echo ""
}

# 运行主函数
main "$@"
