---
name: routes
description: 在 Vben Admin 中添加新路由和菜单。在用户要求加菜单、加路由、配置 meta、新页面入口时使用。
---

# 路由配置

**完整工作流**：[reference/route-configuration.md](reference/route-configuration.md)

## 何时使用

- 新增页面入口、菜单项；配置 path、component、meta

## 步骤要点

1. 在 **`src/router/routes/modules/`**（即 `frontend/apps/web-antd/src/router/routes/modules/`）下新增模块（如 `product.ts`），定义 path、name、component、meta。
2. meta：`title`（i18n 键）、`icon`（Lucide/Iconify 字符串）、`order`、`hideInMenu`。
3. 在 **`src/views/`** 下创建对应 `.vue`；可选 `meta.authority`（当前多为后端菜单）。
4. 启动项目验证菜单与跳转。

**约定**：菜单标题用 i18n；路由命名一致（如 SystemUser）。
