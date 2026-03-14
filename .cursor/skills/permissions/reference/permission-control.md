---
description: 在 Vben Admin 中使用权限控制指令和权限码的标准流程。
---

# 权限控制工作流

此工作流指导 Agent 如何在 Vben Admin 中正确实现基于权限码的访问控制。

## 路径与格式

- **权限码文件**：`frontend/apps/web-antd/src/constants/permission-codes.ts`
- **格式**：`ac:module:resource:action`（如 `ac:system:user:add`），详见 `.cursor/rules/access-system.mdc`

## 工作流步骤

1. **定义权限码**：在 `src/constants/permission-codes.ts` 的 `SYSTEM_PERMISSION_CODES`（或对应模块）下增加 `VIEW`、`ADD`、`EDIT`、`DELETE`，值为 `ac:module:resource:action`。
2. **页面按钮**：`v-access:code="SYSTEM_PERMISSION_CODES.XXX.ADD"`，导入 `#/constants/permission-codes`。
3. **表格操作列**：在 `data.ts` 用 `usePermissions(SYSTEM_PERMISSION_CODES.XXX)`，`hasPermission.EDIT()` 等过滤 `actionButtons`。
4. **后端**：必须同步校验，不依赖前端做唯一防护。

## 示例（与项目一致）

**权限码定义：**

```typescript
// src/constants/permission-codes.ts
export const SYSTEM_PERMISSION_CODES = {
  USER: {
    VIEW: 'ac:system:user:view',
    ADD: 'ac:system:user:add',
    EDIT: 'ac:system:user:edit',
    DELETE: 'ac:system:user:delete',
  },
} as const;
```

**页面按钮：**

```vue
<Button v-access:code="SYSTEM_PERMISSION_CODES.USER.ADD" type="primary" @click="onCreate">新增</Button>
```

**表格操作列（data.ts）：**

```typescript
const { hasPermission } = usePermissions(SYSTEM_PERMISSION_CODES.USER);
const operationButtons = [
  { code: 'edit', text: $t('common.edit'), hasAccess: hasPermission.EDIT },
  { code: 'delete', text: $t('common.delete'), hasAccess: hasPermission.DELETE },
].filter((btn) => btn.hasAccess?.() ?? true).map(({ hasAccess, ...rest }) => rest);
```
