---
description: 在 Vben Admin 中添加新路由和菜单项的标准流程。
---

# 路由配置工作流

此工作流指导 Agent 如何在 Vben Admin 的 Monorepo 架构中正确添加新的路由和菜单。

## 必备条件

- 了解项目的路由配置位置（通常在 `frontend/apps/web-antd/src/router/routes/modules/`）。
- 熟悉 Vue Router 和 Vben 的路由元信息 (meta) 配置。

## 工作流步骤

1.  **创建路由配置文件**:
    - 在 **`src/router/routes/modules/`**（即 `frontend/apps/web-antd/src/router/routes/modules/`）下创建新的路由模块文件（如 `product.ts`）。
    - 定义路由的 `path`, `name`, `component`, 和 `meta` 信息。
2.  **配置菜单元信息 (meta)**:
    - `title`: 菜单标题（支持 i18n 键名）。
    - `icon`: 菜单图标（使用 Lucide 或 Iconify）。
    - `order`: 菜单排序。
    - `hideInMenu`: 是否在菜单中隐藏。
3.  **创建对应的视图组件**:
    - 在 `views/` 目录下创建对应的 `.vue` 文件。
4.  **权限控制** (可选):
    - 在 `meta` 中配置 `authority` 或权限码。
5.  **验证**:
    - 启动开发服务器，检查菜单是否正确显示。
    - 测试路由跳转和页面渲染。

## 最佳实践

- **应当 (DO)**: 使用 i18n 键名作为菜单标题，便于国际化。
- **应当 (DO)**: 保持路由命名的一致性（如 `SystemUser`, `SystemRole`）。
- **避免 (DON'T)**: 在路由配置中编写复杂的业务逻辑。

## 示例

**提示词:**

> 为"产品管理"模块添加路由和菜单。

**路由配置示例:**

```typescript
// router/routes/modules/product.ts
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/product',
    name: 'Product',
    component: () => import('#/views/product/index.vue'),
    meta: {
      title: 'product.title', // i18n key
      icon: 'lucide:package',
      order: 3,
    },
  },
]

export default routes
```
