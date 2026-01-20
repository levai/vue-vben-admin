---
description: 在 Vben Admin 架构下添加新的多语言键值对并应用。
---

# 国际化 (i18n) 定制工作流

此工作流指导 Agent 在 `vue-vben-admin` 单体仓库 (Monorepo) 架构下正确维护多语言。

## 必备条件

- 确定支持的语言（通常为 `zh-CN` 和 `en-US`）。
- 了解多语言文件存放路径：
  - **业务功能** (推荐): `frontend/apps/web-antd/src/locales/langs/{lang}/{module}.json`

## 工作流步骤

1.  **定义键值对**: 在对应语言的 JSON/TS 文件中添加新的命名空间或键值。
2.  **代码应用**:
    - 在组件中引入 `useI18n`：`import { useI18n } from '@vben/locale';`。
    - 使用 `t('namespace.key')` 获取对应文本。
3.  **菜单集成**: 如果是菜单项，在路由配置的 `meta.title` 中使用对应的 i18n 键名。
4.  **验证**: 切换系统语言，确保新添加的文本能正确显示。

## 最佳实践

- **应当 (DO)**: 尽量以模块/页面为单位组织命名空间，避免全局键名冲突。
- **避免 (DON'T)**: 在组件模板中硬编码中文字符。

## 示例

**提示词:**

> 为“设置中心”添加中英文支持。

**操作示例:**
在 `zh-CN` 文件中：`"settings": { "title": "设置中心" }`
在组件中：`<span>{{ t('settings.title') }}</span>`
