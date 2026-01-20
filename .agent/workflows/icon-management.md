---
description: 规范化使用 Vben Admin 中的图标资源（Iconify, Lucide, SVG）。
---

# 图标管理工作流

此工作流用于指导如何在 Vben Admin 中正确选择、配置和使用图标。

## 必备条件

- 熟悉 Lucide 或 Iconify 图标库前缀（如 `lucide:`, `ant-design:`）。
- 了解 `frontend/packages/icons` 的图标注册机制。

## 工作流步骤

1.  **查找图标**: 在 [Icones](https://icones.js.org/) 或相关库中找到所需图标 ID。
2.  **组件引用**:
    - 使用通用图标组件：`import { VbenIcon } from '@vben/icons';`。
    - 传入 `icon` 属性：`<VbenIcon icon="ant-design:setting-outlined" />`。
3.  **菜单图标**: 在路由 `meta.icon` 中直接配置图标字符串或 Lucide 图标对象。
4.  **自定义 SVG**: (如果需要) 将 SVG 存放在 `frontend/packages/icons/src/svg/` 并重新运行构建或生成代码。

## 最佳实践

- **应当 (DO)**: 优先使用 Lucide 或 Iconify，减少构建包体积。
- **应当 (DO)**: 保持同类功能（如菜单、按钮）的图标风格一致。

## 示例

**提示词:**

> 在侧边栏导航中为“用户管理”添加一个 Lucide 图标。

**配置示例:**
`icon: 'lucide:user'` 或直接在 `meta` 中配置。
