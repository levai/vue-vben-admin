---
name: crud-with-drawer
description: 根据项目前端规范生成完整 CRUD 功能（列表、搜索、新增/编辑抽屉或弹窗、删除、权限）。在用户要求做增删改查、某某管理、列表加弹窗、CRUD 模块时使用。基于 frontend/apps/web-antd 与 system/user 参考实现。
---

# CRUD + 弹窗/抽屉 开发技能

本技能已涵盖「CRUD 列表页」与「Modal/Drawer 弹窗/抽屉」的完整用法，无需再查其他工作流。前端应用路径：**`frontend/apps/web-antd/`**。参考实现：**`src/views/system/user/`**（list.vue、data.ts、modules/form.vue）及 **`src/api/system/user.ts`**。

## 何时使用本技能

- 用户要求：增删改查、XX 管理、列表+弹窗、CRUD、带表单的列表
- 产出：API 层 + 权限码（如需）+ 列表页 + 搜索 + 新增/编辑（抽屉或弹窗）+ 删除

## 1. API 层（`src/api/system/xxx.ts`）

**类型与命名约定**见 `.cursor/rules/api-rules.mdc`。要点：仅用 **`requestClient`**（#/api/request）；命名空间导出类型；方法 getXxxList / getXxxById / createXxx / updateXxx / deleteXxx / getXxxOptions；列表返回 `{ list, total }`。

## 2. 权限码（新增资源时）

**格式与用法**见 `.cursor/rules/access-system.mdc`。要点：在 **`src/constants/permission-codes.ts`** 的 `SYSTEM_PERMISSION_CODES` 下增加 VIEW/ADD/EDIT/DELETE（格式 `ac:module:resource:action`）；页面按钮 `v-access:code`；表格操作列 `usePermissions(模块常量)` 过滤 actionButtons。

## 3. 列表页（`src/views/system/xxx/list.vue`）

- **Page** 包一层，标题用 `$t()`。
- **抽屉**：`useVbenDrawer({ connectedComponent: Form, destroyOnClose: true })`，Form 为新增/编辑表单组件。
- **表格**：`useVbenVxeGrid<XxxApi.Xxx>({ formOptions, gridOptions })`：
  - `formOptions.schema`：`useGridFormSchema()`（来自 data.ts）
  - `formOptions.submitOnChange: true`，若有时间范围等：`fieldMappingTime: [['createTime', ['startTime', 'endTime']]]`
  - `gridOptions.columns`：`useColumns(onActionClick)`（来自 data.ts）
  - `gridOptions.proxyConfig.ajax.query`：入参 `({ page }, formValues)`，调用 `getXxxList({ page: page.currentPage, pageSize: page.pageSize, ...formValues })`
  - `gridOptions.rowConfig.keyField: 'id'`，`toolbarConfig` 按需（custom、refresh、search、zoom）。
- **工具栏「新增」按钮**：`v-access:code="SYSTEM_PERMISSION_CODES.XXX.ADD"`，点击打开抽屉：`formDrawerApi.setData(undefined); formDrawerApi.open()`；编辑：`formDrawerApi.setData(row); formDrawerApi.open()`。抽屉关闭后 `gridApi.query()` 或 `reload()`。
- **删除**：用 `Modal.confirm` 确认后调 `deleteXxx(id)`，成功后再查询列表。
- **操作回调**：`onActionClick({ code, row })` 里 switch `edit` / `delete` 等，编辑时 setData(row) 再 open。

## 4. data.ts（`src/views/system/xxx/data.ts`）

- **useFormSchema(isEdit)**：返回 `VbenFormSchema[]`，用于抽屉内表单；编辑时可为密码等字段设可选或占位提示。
- **useGridFormSchema()**：返回搜索区表单项（Input、Select、ApiTreeSelect、RangePicker 等），fieldName 与接口查询参数一致。
- **useColumns(onActionClick, 其它?)**
  - `usePermissions(SYSTEM_PERMISSION_CODES.XXX)`，定义 `operationButtons`: `[{ code, text: $t('...'), hasAccess: hasPermission.EDIT }, ...]`。
  - 用 `.filter(btn => btn.hasAccess?.() ?? true).map(({ hasAccess, ...rest }) => rest)` 得到 `filteredOperations`。
  - 列配置中含操作列：`cellRender: { name: 'CellOperation', attrs: { onClick: onActionClick }, options: filteredOperations }`，`title: $t('common.action')`，`fixed: 'right'`。
  - 其它列：field、title、minWidth，需要标签/开关时用 `cellRender: { name: 'CellTag' | 'CellSwitch', options | attrs }`。
- 所有文案用 **`$t()`**，与 locales 中 key 一致。

## 5. 表单组件（`src/views/system/xxx/modules/form.vue`）

- 作为抽屉的 **connectedComponent** 使用；若用弹窗则改为 **useVbenModal**，逻辑类似。
- **useVbenForm**：`schema: useFormSchema(false)`（或根据 isEdit 动态），`showDefaultActions: false`（按钮由抽屉提供）。
- **useVbenDrawer**（与 list 中抽屉同体）：
  - **onConfirm**：`await formApi.validate()`，通过后 `formApi.getValues()`，根据有无 id 调 `createXxx` / `updateXxx`，成功后 `emit('success')`、`drawerApi.close()`；提交前 `drawerApi.lock()`，失败 `drawerApi.unlock()`。
  - **onOpenChange(isOpen)**：打开时 `drawerApi.getData<Xxx>()`，有 data 则 `formApi.setValues(...)` 并视情况 `formApi.updateSchema(useFormSchema(true))`，无则重置表单。
- 若有下拉选项在表单内动态加载（如角色列表），在 onOpenChange 里请求并更新对应 schema 的 `componentProps.options`，再 `formApi.updateSchema(schema)`。
- 使用 **`#/adapter/form`** 的组件名（Input、InputPassword、Select、ApiSelect、ApiTreeSelect、RadioGroup、RangePicker 等）和校验（`rules: 'required'` 或 `z.xxx()`）。

## 6. 弹窗替代抽屉

- 列表页用 **useVbenModal** 替代 useVbenDrawer，`connectedComponent: Form`。
- 表单组件内用 **useVbenModal** 的 onConfirm / onOpenChange，用 `modalApi.getData/setData/close/lock/unlock`，其余与抽屉一致。

## 7. 检查清单

- [ ] API 使用 requestClient，类型为命名空间导出，方法命名 getXxxList/createXxx/updateXxx/deleteXxx
- [ ] 新资源在 permission-codes 中增加 VIEW/ADD/EDIT/DELETE，列表按钮 v-access:code，表格操作 usePermissions 过滤
- [ ] list.vue：Page + useVbenDrawer(connectedComponent) + useVbenVxeGrid（formOptions + proxyConfig + useColumns）
- [ ] data.ts：useFormSchema、useGridFormSchema、useColumns 含权限过滤后的 operationButtons
- [ ] modules/form.vue：useVbenForm + useVbenDrawer onConfirm/onOpenChange，提交后 emit('success') 并 close
- [ ] 所有文案 $t()，路径别名 #/

## 8. 参考文件速查

| 用途       | 路径 |
|------------|------|
| 列表+抽屉  | `frontend/apps/web-antd/src/views/system/user/list.vue` |
| 列与表单   | `frontend/apps/web-antd/src/views/system/user/data.ts` |
| 抽屉内表单 | `frontend/apps/web-antd/src/views/system/user/modules/form.vue` |
| API 与类型 | `frontend/apps/web-antd/src/api/system/user.ts` |
| 权限码     | `frontend/apps/web-antd/src/constants/permission-codes.ts` |

更细的组件/API/权限约定见 `.cursor/rules/frontend-development.mdc`、`api-rules.mdc`、`access-system.mdc`。
