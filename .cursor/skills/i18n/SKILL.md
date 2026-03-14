---
name: i18n
description: 在 Vben Admin 下添加多语言键值并应用。在用户要求做国际化、i18n、中英文文案、多语言、locale 时使用。
---

# 国际化 (i18n)

**完整工作流**：[reference/i18n-localization.md](reference/i18n-localization.md)

## 何时使用

- 新增或修改多语言文案；菜单、页面、按钮需中英文等支持

## 步骤要点

1. 在 **`src/locales/langs/{lang}/{module}.json`**（zh-CN、en-US）添加命名空间或键。
2. 组件中 `import { $t } from '#/locales'`，使用 **`$t('namespace.key')`**（项目统一用 $t，勿用 useI18n().t）。
3. 路由 `meta.title` 用 i18n 键名；切换语言验证。

**约定**：按模块组织命名空间；不在模板硬编码中文。
