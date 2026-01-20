---
description: 在 Vben Admin 中使用权限控制指令和权限码的标准流程。
---

# 权限控制工作流

此工作流指导 Agent 如何在 Vben Admin 中正确实现基于权限码的访问控制。

## 必备条件

- 了解项目的权限码定义位置（通常在 `constants/permission-codes.ts`）。
- 熟悉 `v-access` 指令的使用。

## 工作流步骤

1.  **定义权限码**:
    - 在 `constants/permission-codes.ts` 中添加新的权限码常量。
    - 按模块组织权限码（如 `USER.ADD`, `USER.EDIT`, `USER.DELETE`）。
2.  **在组件中使用权限控制**:
    - 使用 `v-access:code` 指令控制按钮或元素的显示。
    - 导入权限码常量：`import { SYSTEM_PERMISSION_CODES } from '#/constants/permission-codes';`
3.  **路由级权限** (可选):
    - 在路由的 `meta.authority` 中配置所需权限。
4.  **后端对接**:
    - 确保前端权限码与后端权限系统保持一致。

## 最佳实践

- **应当 (DO)**: 使用常量而非硬编码字符串定义权限码。
- **应当 (DO)**: 按功能模块组织权限码，便于维护。
- **避免 (DON'T)**: 仅依赖前端权限控制，后端必须同步验证。

## 示例

**提示词:**

> 为"新增用户"按钮添加权限控制。

**权限码定义:**

```typescript
// constants/permission-codes.ts
export const SYSTEM_PERMISSION_CODES = {
  USER: {
    ADD: 'system:user:add',
    EDIT: 'system:user:edit',
    DELETE: 'system:user:delete',
  },
}
```

**组件中使用:**

```vue
<script setup lang="ts">
import { SYSTEM_PERMISSION_CODES } from '#/constants/permission-codes'
</script>

<template>
  <Button v-access:code="SYSTEM_PERMISSION_CODES.USER.ADD" type="primary" @click="onCreate"> 新增用户 </Button>
</template>
```
