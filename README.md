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
# 方式一：使用包装脚本（推荐）
./install                    # 安装所有依赖
./install --frontend-only    # 仅安装前端依赖
# Windows: install.bat [options]

# 方式二：使用统一入口
node scripts/cli.js install              # 安装所有依赖
node scripts/cli.js install --frontend-only  # 仅前端

# 方式三：直接运行脚本
node scripts/install.js
node scripts/install.js --frontend-only
```

**注意**：
- 安装脚本使用 Node.js 编写，支持 Windows/macOS/Linux
- 根目录不使用 pnpm 管理依赖，所有依赖管理都在 `frontend` 目录进行
- 后端依赖由 Maven 管理，会自动检测并安装（如果已安装 Maven）
- 安装脚本会自动检测环境并安装相应依赖

### 2. 数据库初始化

```bash
cd backend
./src/main/resources/db/init.sh
```

详细说明请参考：[后端数据库文档](./backend/README.md#数据库初始化)

### 3. 启动项目

#### 统一入口（推荐）✨

```bash
# 方式一：使用包装脚本（推荐）
./dev                    # 交互式启动
./dev all                # 启动全部（前端 + 后端）
./dev frontend           # 仅启动前端
./dev backend            # 仅启动后端
./dev ele                # Element Plus 版本
./dev docs               # 文档站点

# Windows: dev.bat [mode]

# 方式二：使用统一入口
node scripts/cli.js dev              # 交互式启动
node scripts/cli.js dev all          # 启动全部
node scripts/cli.js dev frontend     # 仅前端
node scripts/cli.js dev backend      # 仅后端
node scripts/cli.js dev ele          # Element Plus
node scripts/cli.js dev docs         # 文档站点
```

**交互式菜单选项**（无参数时）：
- `1` - 启动全部（前端 + 后端）
- `2` - 仅启动前端
- `3` - 仅启动后端
- `4` - 启动前端（Element Plus 版本）
- `5` - 启动文档站点
- `0` - 退出

**提示**：
- 所有启动脚本都支持跨平台（Windows/macOS/Linux）
- 使用 `Ctrl+C` 停止所有服务
- Unix/macOS/Linux 系统可以直接使用 `./dev` 和 `./install`
- Windows 系统可以使用 `dev.bat` 和 `install.bat`

### 4. 访问应用

- **前端 (Ant Design Vue)**: http://localhost:5666
- **前端 (Element Plus)**: http://localhost:5777
- **后端 API**: http://localhost:8080
- **API 文档**: http://localhost:8080/doc.html

### 5. 默认账号

- **用户名**: `admin`
- **密码**: `admin123`

## 📚 项目文档

### 前端文档

- [前端开发规范](./.cursor/rules/frontend-development.mdc)
- [权限系统设计](./.cursor/rules/access-system.mdc)
- [前端 README](./frontend/README.md)

### 后端文档

- [后端开发规范](./.cursor/rules/backend-development.mdc)
- [后端 README](./backend/README.md)
- [技术栈分析](./backend/docs/技术栈与开发规范分析.md)
- [Token 最佳实践](./backend/docs/TOKEN_BEST_PRACTICES.md)

## 🛠️ 常用命令

### 开发命令

```bash
# 启动开发服务器
./dev [mode]                # 统一入口（推荐）
# Windows: dev.bat [mode]

node scripts/cli.js dev [mode]     # 统一入口
# 模式: (无参数=交互式), all, frontend, backend, ele, docs
```

### 构建命令

```bash
# 构建项目（需要进入 frontend 目录）
cd frontend
pnpm build:antd             # 构建前端（Ant Design Vue）
pnpm build                  # 构建所有前端应用
```

### 代码质量

```bash
# 代码检查（需要进入 frontend 目录）
cd frontend
pnpm lint                   # ESLint 检查
pnpm format                 # 格式化代码

# 清理
cd frontend && pnpm clean && cd ../backend && mvn clean
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
2. 查看前端文档：`.cursor/rules/frontend-development.mdc`
3. 遵循 Vue 3 Composition API 规范
4. 使用 TypeScript 类型定义
5. 遵循项目代码规范

### 后端开发

1. 进入后端目录：`cd backend`
2. 查看后端文档：`.cursor/rules/backend-development.mdc`
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
