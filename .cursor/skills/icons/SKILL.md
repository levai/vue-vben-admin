---
name: icons
description: 在 Vben Admin 中规范使用图标（Iconify、Lucide、SVG）。在用户要求加菜单图标、按钮图标、侧边栏图标时使用。
---

# 图标管理

**完整工作流**：[reference/icon-management.md](reference/icon-management.md)

## 何时使用

- 为菜单、按钮、页面配置图标；选择或注册新图标

## 步骤要点

1. 在 [Icones](https://icones.js.org/) 或 Lucide/Iconify 中确认图标 ID。
2. 组件：`icon="lucide:user"` 或 `ant-design:setting-outlined`；路由 `meta.icon` 直接配字符串。
3. 自定义 SVG 放 `frontend/packages/icons/src/svg/` 并参与构建。

**约定**：优先 Lucide/Iconify；同类功能图标风格一致。
