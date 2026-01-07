# Vue Vben Admin 全栈项目

> 基于 Vue 3 + Spring Boot 的全栈管理系统

## 📋 项目结构

```
vue-vben-admin/
├── backend/              # 后端服务（Spring Boot 3.2.0 + Java 17）
│   ├── src/
│   ├── pom.xml
│   └── README.md
└── frontend/             # 前端 Monorepo（Vue 3 + Vite + TypeScript）
    ├── apps/
    │   ├── web-antd/     # Ant Design Vue 版本
    │   ├── web-ele/      # Element Plus 版本
    │   └── backend-mock/ # Mock API 服务
    ├── packages/         # 共享包
    └── internal/         # 内部工具
```

## 🚀 快速开始

### 环境要求

- **Node.js**: >= 20.12.0
- **pnpm**: >= 10.0.0
- **Java**: 17+ (推荐使用 jenv 管理)
- **Maven**: 3.6+
- **MySQL**: 8.0+

### 1. 安装依赖

```bash
# 方式一：使用 npm 脚本（推荐）
npm run install

# 方式二：直接使用 bash 脚本
bash scripts/install.sh

# 仅安装前端依赖
npm run install:frontend
# 或
bash scripts/install.sh --frontend-only
```

**注意**：
- 根目录不使用 pnpm 管理依赖，所有依赖管理都在 `frontend` 目录进行
- 后端依赖由 Maven 管理，无需单独安装
- 安装脚本会自动检测环境并安装相应依赖

### 2. 数据库初始化

```bash
cd backend
./src/main/resources/db/init.sh
```

详细说明请参考：[后端数据库文档](./backend/README.md#数据库初始化)

### 3. 启动项目

#### 一键启动（推荐）

```bash
# 同时启动前后端（跨平台支持 Windows/macOS/Linux）
pnpm dev
```

#### 分别启动

```bash
# 仅启动前端
pnpm run dev:frontend

# 仅启动后端
pnpm run dev:backend

# 启动前端（Ant Design Vue 版本，直接进入前端目录）
pnpm run dev:antd
```

### 4. 访问应用

- **前端**: http://localhost:5173
- **后端 API**: http://localhost:8080
- **API 文档**: http://localhost:8080/doc.html

### 5. 默认账号

- **用户名**: `admin`
- **密码**: `admin123`

## 📚 项目文档

### 前端文档

- [前端开发规范](./frontend/.cursor/rules/frontend-development.mdc)
- [权限系统设计](./frontend/.cursor/rules/access-system.mdc)
- [前端 README](./frontend/README.md)

### 后端文档

- [后端开发规范](./backend/.cursor/rules/backend-development.mdc)
- [后端 README](./backend/README.md)
- [技术栈分析](./backend/docs/技术栈与开发规范分析.md)
- [Token 最佳实践](./backend/docs/TOKEN_BEST_PRACTICES.md)

## 🛠️ 常用命令

### 开发命令

```bash
# 启动开发服务器
pnpm dev                    # 一键启动前后端（推荐）
pnpm run dev:frontend       # 仅启动前端
pnpm run dev:backend        # 仅启动后端
pnpm run dev:antd          # 启动 Ant Design Vue 版本
```

### 构建命令

```bash
# 构建项目
pnpm build                  # 构建前端（Ant Design Vue）
pnpm run build:frontend     # 构建所有前端应用
```

### 代码质量

```bash
# 代码检查
pnpm lint                   # ESLint 检查
pnpm format                 # 格式化代码

# 清理
pnpm clean                  # 清理构建产物
```

## 🏗️ 技术栈

### 前端

- **框架**: Vue 3.5+ (Composition API)
- **构建工具**: Vite 6+
- **语言**: TypeScript 5+
- **UI 框架**: Ant Design Vue 4.x
- **状态管理**: Pinia
- **路由**: Vue Router
- **样式**: TailwindCSS
- **包管理**: pnpm (Monorepo)

### 后端

- **框架**: Spring Boot 3.2.0
- **语言**: Java 17 (LTS)
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis Plus 3.5.7
- **安全**: Spring Security + JWT
- **API 文档**: Knife4j (SpringDoc OpenAPI 3)
- **构建工具**: Maven

## 📖 开发指南

### 前端开发

1. 进入前端目录：`cd frontend`
2. 查看前端文档：`frontend/.cursor/rules/frontend-development.mdc`
3. 遵循 Vue 3 Composition API 规范
4. 使用 TypeScript 类型定义
5. 遵循项目代码规范

### 后端开发

1. 进入后端目录：`cd backend`
2. 查看后端文档：`backend/.cursor/rules/backend-development.mdc`
3. 遵循 Spring Boot 3 规范
4. 使用 MyBatis Plus 进行数据库操作
5. 遵循 RESTful API 设计规范

## 🔧 配置说明

### 前端配置

- 路径别名：`#/*` 指向 `./src/*`
- 环境变量：`.env.local`、`.env.development`、`.env.production`
- Vite 配置：`frontend/apps/web-antd/vite.config.mts`

### 后端配置

- 数据库配置：`backend/src/main/resources/application.yml`
- Java 版本：使用 `.java-version` 文件配合 jenv
- Maven 配置：`backend/pom.xml`

## 🐛 常见问题

### 端口占用

```bash
# 清理 8080 端口（后端）
lsof -ti:8080 | xargs kill -9 2>/dev/null

# 清理 5173 端口（前端）
lsof -ti:5173 | xargs kill -9 2>/dev/null
```

### 依赖问题

```bash
# 重新安装前端依赖
cd frontend && pnpm install

# 清理 Maven 缓存（后端）
cd backend && mvn clean
```

### 数据库连接失败

1. 检查 MySQL 是否启动
2. 检查数据库配置是否正确
3. 确认数据库已初始化

## 📝 开发规范

- **Git 提交**: 遵循 `<type>: <subject>` 格式
- **代码风格**: 遵循项目 ESLint/Prettier 配置
- **类型安全**: 禁止使用 `any`，确保类型完整
- **组件规范**: 使用 Composition API，禁止 Options API

## 📄 License

MIT

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**提示**：更多详细文档请查看各子目录的 README 文件。
