# Scripts 目录说明

本目录包含项目的启动和安装脚本，所有脚本均使用 Node.js 编写，支持跨平台（Windows/macOS/Linux）。

## 📁 目录结构

```
scripts/
├── README.md              # 本文件
├── cli.js                 # 统一命令行入口（推荐使用）
├── install.js             # 依赖安装脚本
├── start.js               # 启动脚本（命令行模式）
├── start-interactive.js   # 启动脚本（交互式模式）
└── utils/                 # 工具函数
    ├── logger.js          # 日志工具
    ├── paths.js           # 路径工具
    ├── process-manager.js # 进程管理工具
    └── validator.js       # 验证工具
```

## 🎯 统一入口（推荐）

所有命令都通过 `cli.js` 统一入口：

```bash
# 启动开发服务器
node scripts/cli.js dev              # 交互式模式
node scripts/cli.js dev all          # 启动全部
node scripts/cli.js dev frontend     # 仅前端
node scripts/cli.js dev backend      # 仅后端
node scripts/cli.js dev ele          # Element Plus 版本
node scripts/cli.js dev docs         # 文档站点

# 安装依赖
node scripts/cli.js install          # 安装所有依赖
node scripts/cli.js install --frontend-only  # 仅前端

# 帮助信息
node scripts/cli.js help
```

## 🚀 脚本说明

### cli.js - 统一命令行入口 ⭐

统一入口脚本，支持所有项目命令。

**使用方式：**

```bash
node scripts/cli.js <command> [options]
```

**支持的命令：**

- `dev [mode]` - 启动开发服务器
- `install [options]` - 安装依赖
- `help` - 显示帮助信息

**优势：**

- ✅ 统一入口，易于记忆
- ✅ 自动判断交互式或命令行模式
- ✅ 支持所有启动模式
- ✅ 更好的扩展性

### install.js - 依赖安装脚本

安装项目的前后端依赖。

**使用方式：**

```bash
# 安装所有依赖
node scripts/install.js

# 仅安装前端依赖
node scripts/install.js --frontend-only
```

**功能：**

- ✅ 自动检测 Node.js 和 pnpm
- ✅ 安装前端依赖（pnpm）
- ✅ 安装后端依赖（Maven，如果已安装）
- ✅ 跨平台支持

### start.js - 启动脚本（命令行模式）

通过命令行参数指定启动模式。

**使用方式：**

```bash
# 启动全部（前端 + 后端）
node scripts/start.js all

# 仅启动前端
node scripts/start.js frontend

# 仅启动后端
node scripts/start.js backend
```

**功能：**

- ✅ 支持三种模式：all、frontend、backend
- ✅ 自动检查目录是否存在
- ✅ 优雅的进程清理（Ctrl+C）
- ✅ 跨平台支持

### start-interactive.js - 启动脚本（交互式模式）

显示交互式菜单，让用户选择启动模式。

**使用方式：**

```bash
# 交互式选择（推荐）
node scripts/start-interactive.js

# 或直接指定模式（兼容命令行模式）
node scripts/start-interactive.js all
node scripts/start-interactive.js frontend
node scripts/start-interactive.js backend
node scripts/start-interactive.js ele      # Element Plus 版本
node scripts/start-interactive.js docs     # 文档站点
```

**功能：**

- ✅ 交互式菜单选择
- ✅ 支持 5 种启动模式
- ✅ 兼容命令行参数
- ✅ 跨平台支持

## 🛠️ 工具函数（utils/）

### logger.js - 日志工具

提供统一的颜色输出功能。

```javascript
import { log } from './utils/logger.js'

log('成功消息', 'green')
log('错误消息', 'red')
log('警告消息', 'yellow')
```

### paths.js - 路径工具

提供统一的路径管理。

```javascript
import { rootDir, backendDir, frontendDir } from './utils/paths.js'
```

### ports.js - 端口配置工具

提供端口配置管理，前端端口从 `.env.development` 文件动态读取。

```javascript
import { PORTS, getFrontendPort, getAntdPort, getElePort, getDocsPort } from './utils/ports.js'

// 后端端口（固定）
PORTS.BACKEND // 8080

// 前端端口（从环境文件读取）
getAntdPort() // 从 frontend/apps/web-antd/.env.development 读取
getElePort()  // 从 frontend/apps/web-ele/.env.development 读取
getDocsPort() // 从 frontend/apps/docs/.env.development 读取（如果存在）

// 根据类型获取端口
getFrontendPort('antd') // 等同于 getAntdPort()
```

**端口配置来源**：
- 后端：固定为 8080
- 前端：从各应用的 `.env.development` 文件中的 `VITE_PORT` 读取
  - `web-antd`: `frontend/apps/web-antd/.env.development`
  - `web-ele`: `frontend/apps/web-ele/.env.development`
  - `docs`: `frontend/apps/docs/.env.development`（如果存在）

### env-reader.js - 环境变量读取工具

从 `.env.development` 文件中读取端口配置。

```javascript
import { getPortFromEnv } from './utils/env-reader.js'

const port = getPortFromEnv('web-antd') // 读取 web-antd 的端口配置
```

### process-manager.js - 进程管理工具

提供进程启动和清理功能。

```javascript
import { startService, registerCleanupHandlers, waitForAllProcesses } from './utils/process-manager.js'

// 注册清理函数
registerCleanupHandlers()

// 启动服务
await startService('服务名', '命令', '工作目录')

// 等待所有进程退出
await waitForAllProcesses()
```

### validator.js - 验证工具

提供目录和命令验证功能。

```javascript
import { checkDir, checkCommand } from './utils/validator.js'

if (checkDir('/path/to/dir', '目录名')) {
  // 目录存在
}

if (checkCommand('node')) {
  // 命令存在
}
```

## 📝 快速开始

### 1. 安装依赖

```bash
node scripts/install.js
```

### 2. 启动项目

```bash
# 交互式启动（推荐）
node scripts/start-interactive.js

# 或命令行启动
node scripts/start.js all
```

## 🔧 开发说明

### 添加新脚本

1. 在 `scripts/` 目录创建新的脚本文件
2. 使用 `utils/` 中的工具函数
3. 添加文件头注释说明用途和使用方式
4. 更新本 README

### 代码规范

- 使用 ES Modules（`import/export`）
- 使用统一的日志工具（`utils/logger.js`）
- 添加详细的注释
- 支持跨平台（Windows/macOS/Linux）

## 📌 注意事项

1. **Node.js 版本要求**: >= 20.12.0
2. **pnpm 版本要求**: >= 10.0.0
3. **脚本权限**: Unix/macOS/Linux 系统需要执行权限（`chmod +x scripts/*.js`）
4. **进程清理**: 使用 `Ctrl+C` 可以优雅地停止所有服务
