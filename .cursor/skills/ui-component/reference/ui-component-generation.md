---
description: 遵循 Vben Admin 设计系统和最佳实践，生成高质量的 Vue 3 组件。
---

# UI 组件生成工作流

此工作流允许 Agent 创建与 `vue-vben-admin` 生态系统无缝集成的、一致且高性能的 Vue 3 组件。

## 必备条件

- Vue 3 + TypeScript 环境。
- 访问设计系统文档或现有组件库。
- 了解项目的原子设计原则。

## 工作流步骤

1.  **需求分析**: 深入理解组件的目的、输入 Props、输出 Emits 及视觉样式要求。
2.  **架构设计**: 确定组件层级（原子/分子/有机体）。
3.  **编码实现**:
    - 使用 `<script setup lang="ts">`。
    - 定义严格的 TypeScript 类型。
    - 遵循项目规范（如 Tailwind 或 Scoped CSS）。
4.  **文档与注释**: 为复杂逻辑添加 JSDoc。
5.  **验证**: 确保渲染正确并通过 Lint 检查。

## 最佳实践

- **应当 (DO)**: 使用 `PascalCase` 命名组件，使用 `camelCase` 命名 Props。
- **避免 (DON'T)**: 将业务逻辑与 UI 逻辑过度耦合。

## 示例

**提示词:**

> 创建一个带有加载状态和图标的主操作按钮。

**代码示例:**

```vue
<template>
  <button :class="classes" :disabled="loading" @click="handleClick">
    <span v-if="loading" class="animate-spin mr-2">...</span>
    <slot name="icon"></slot>
    <slot></slot>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  type?: 'primary' | 'secondary'
  loading?: boolean
}>()

const emit = defineEmits(['click'])

const classes = computed(() => [
  'btn',
  props.type === 'primary' ? 'btn-primary' : 'btn-secondary',
  { 'is-loading': props.loading },
])

const handleClick = (e: MouseEvent) => {
  if (!props.loading) emit('click', e)
}
</script>
```
