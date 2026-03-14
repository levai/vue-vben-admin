---
name: permissions
description: 在 Vben Admin 中使用权限码与 v-access 实现访问控制。在用户要求为按钮/菜单加权限、定义权限码时使用。CRUD 中的权限配置见 crud-with-drawer。
---

# 权限控制

**完整工作流**：[reference/permission-control.md](reference/permission-control.md)  
**项目规范**：`.cursor/rules/access-system.mdc`（格式 ac:module:resource:action、usePermissions、v-access）

## 何时使用

- 为按钮或菜单单独加权限、在 permission-codes 中新增常量

## 步骤要点

1. **`src/constants/permission-codes.ts`** 按模块添加 VIEW/ADD/EDIT/DELETE（格式 **`ac:module:resource:action`**，如 `ac:system:user:add`），详见 `.cursor/rules/access-system.mdc`。
2. 页面按钮：`v-access:code="SYSTEM_PERMISSION_CODES.XXX.ADD"`；表格操作列：`usePermissions(SYSTEM_PERMISSION_CODES.XXX)` 过滤 actionButtons。
3. 后端必须同步校验，不依赖前端做唯一防护。
