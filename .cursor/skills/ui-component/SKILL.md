---
name: ui-component
description: 按 Vben Admin 设计系统生成 Vue 3 组件。在用户要求创建新 UI 组件、按钮、表单控件、原子组件时使用。
---

# UI 组件生成

**完整工作流**：[reference/ui-component-generation.md](reference/ui-component-generation.md)

## 何时使用

- 新增可复用 Vue 3 组件；需与项目设计系统、Tailwind/Scoped 一致

## 步骤要点

1. **需求**：明确目的、Props、Emits、样式。
2. **设计**：确定层级（原子/分子/有机体）。
3. **编码**：`<script setup lang="ts">`，严格类型，遵循项目样式。
4. **文档**：复杂逻辑加 JSDoc；验证渲染与 Lint。

**约定**：组件名 PascalCase，Props camelCase；业务与 UI 逻辑解耦。
