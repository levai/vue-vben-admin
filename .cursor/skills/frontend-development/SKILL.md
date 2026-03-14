---
name: frontend-development
description: 基于 Vue Vben Admin (web-antd) 的前端开发规范与模式。在开发或修改 frontend/apps/web-antd 下的页面、组件、API、权限、表单、表格时应用本技能，确保类型安全、组件优先级与项目约定一致。
---

# 前端开发技能（web-antd）

前端应用路径：**`frontend/apps/web-antd/`**。详细规范见 `.cursor/rules/` 下对应文档，本技能提炼必守要点与引用关系。

## 技术栈与路径

- **栈**：Vue 3.5+ / Vite 6+ / TypeScript 5+ / Pinia / Ant Design Vue / TailwindCSS
- **包管理**：pnpm（禁止 npm/yarn）
- **路径别名**：`#/*` → `frontend/apps/web-antd/src/*`；Workspace 包用 `@vben/*`
- **规范文档**：
  - 通用前端：`.cursor/rules/frontend-development.mdc`
  - API 客户端：`.cursor/rules/api-rules.mdc`
  - 权限系统：`.cursor/rules/access-system.mdc`

## 核心原则

- 类型安全：禁止 `any`，接口/类型完整
- 组合式 API：统一 `<script setup lang="ts">`，禁止 Options API
- 代码完整：禁止 TODO/占位符，产出可运行代码
- 导入路径：应用内用 `#/xxx`，不要混用 `@/`

## 组件优先级

**Vben 组件（优先） → Ant Design Vue → 原生 HTML**

| 场景       | 使用                         | 说明           |
|------------|------------------------------|----------------|
| 表单       | `useVbenForm`（#/adapter/form） | Schema + 校验   |
| 弹窗       | `useVbenModal`（@vben/common-ui） | 支持 connectedComponent |
| 抽屉       | `useVbenDrawer`              | 同 Modal API   |
| 表格+搜索  | `useVbenVxeGrid`（#/adapter/vxe-table） | 列+proxyConfig+formOptions |
| 页面布局   | `Page`（@vben/common-ui）     | 统一标题/描述  |
| 按钮/提示  | ant-design-vue（Button, message, Modal） | 按需导入       |

参考现有列表页：`frontend/apps/web-antd/src/views/system/user/list.vue`（Grid + FormDrawer + 权限按钮）。

## API 与类型

- **客户端**：仅用 `requestClient`（来自 `#/api/request`），不用 axios/ baseRequestClient（除刷新 Token 等特殊场景）
- **文件位置**：`frontend/apps/web-antd/src/api/` 按模块分（如 `system/user.ts`）
- **结构约定**：
  - 类型用命名空间导出：`export namespace XxxApi { export interface Xxx { ... } }`
  - 函数：`async function getXxxList(params)`，`requestClient.get<ResponseType>(url, { params })`
  - 响应已按 `responseReturn: 'data'` 取 `data`，直接写返回类型即可
- **命名**：getXxxList / getXxxById / createXxx / updateXxx / deleteXxx / getXxxOptions

详见 `.cursor/rules/api-rules.mdc`。

## 权限

- **权限码**：常量在 `#/constants/permission-codes.ts`，格式 `ac:module:resource:action`（如 `SYSTEM_PERMISSION_CODES.USER.ADD`）
- **页面按钮**：`v-access:code="SYSTEM_PERMISSION_CODES.XXX.ADD"` 等，用常量勿手写字符串
- **表格操作列**：在 `data.ts` 用 `usePermissions(SYSTEM_PERMISSION_CODES.XXX)`，`hasPermission.EDIT()` / `hasPermission.DELETE()` 等过滤 `actionButtons`
- **编程判断**：优先 `usePermissions`；需多码或角色时用 `useAccess` 的 `hasAccessByCodes` / `hasAccessByRoles`

详见 `.cursor/rules/access-system.mdc`。

## 列表页模式（CRUD）

**完整 CRUD（列表+搜索+新增/编辑抽屉+删除+权限）** 见同目录 [crud-with-drawer](../crud-with-drawer/SKILL.md)，本段仅作要点提示。

1. **list.vue**：`useVbenVxeGrid`（formOptions.schema 用 `useGridFormSchema()`，gridOptions.columns 用 `useColumns(onActionClick)`，proxyConfig.ajax.query 调 API）
2. **data.ts**：`useColumns(onActionClick)` 返回列配置；操作列用 `usePermissions` 过滤 `actionButtons`；`useGridFormSchema()` 返回搜索表单项
3. **API**：对应 `api/system/xxx.ts`，namespace + getXxxList / getXxxById / createXxx / updateXxx / deleteXxx
4. **新增/编辑**：`useVbenDrawer` 或 `useVbenModal` + `connectedComponent` 表单单页，提交后 `gridApi.query()` 或 `reload()`

## 表单与表格细节

- **Vben 表单**：Schema 中 `component: 'ApiSelect'` 需后端搜索时加 `enableBackendSearch: true`；`ApiTreeSelect` 的 api 用动态 import 避免卡顿：`api: () => import('#/api/...').then(m => m.getXxxList)`
- **Ant Design Vue**：绑定用 `v-model:value`（Checkbox/Radio/Switch 等用适配器约定字段）
- **国际化**：文案用 `$t('key')`，来自 `#/locales`

## 命名与文件

- 组件/页面：PascalCase（`UserList.vue`）
- 工具/数据：camelCase（`formatDate.ts`、`data.ts`）
- 常量：UPPER_SNAKE 或常量对象（如 permission-codes）
- 类型/接口：PascalCase

## 新增功能检查清单

- [ ] 类型与接口完整，无 `any`
- [ ] 应用内引用使用 `#/` 别名
- [ ] 表单/表格/弹窗优先用 Vben 组件
- [ ] API 使用 `requestClient` + 命名空间类型
- [ ] 按钮权限用 `v-access:code` 或 `usePermissions`，权限码来自常量
- [ ] 文案走 `$t()`
- [ ] 新 API 文件符合 api-rules 命名与结构

## 参考实现

- 列表+抽屉+权限：`frontend/apps/web-antd/src/views/system/user/`（list.vue、data.ts、modules/form.vue）
- API+类型：`frontend/apps/web-antd/src/api/system/user.ts`
- 权限常量：`frontend/apps/web-antd/src/constants/permission-codes.ts`（若无对应资源需先在此增加再使用）
