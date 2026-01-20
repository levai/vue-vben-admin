---
description: 遵循 Vben Admin 模式快速生成标准 CRUD（增删改查）页面。
---

# CRUD 页面生成工作流

此工作流指导 Agent 使用 Vben Admin 的 VXE Table 适配器快速构建数据管理界面。

## 必备条件

- 已完成 `api-integration` 后端接口集成。
- 熟悉 VXE Table 的列配置和代理模式。

## 工作流步骤

1.  **配置表格 (Grid)**:
    - 使用 `useVbenVxeGrid` 钩子创建表格实例。
    - 定义 `columns` 列配置（使用 `useColumns` 辅助函数）。
    - 配置 `proxyConfig.ajax.query` 对接后端分页接口。
    - **注意**: 确保后端接口返回的列表字段为 `list`，与 `vxe-table` 默认配置保持一致。如果不一致，请要求后端修改。
    - **注意**: 确保前端定义的 ID 类型（string/number）与后端 API 返回类型严格一致。
    - 配置 `formOptions.schema` 定义搜索表单。
2.  **集成表单 (Form)**:
    - 为新增/编辑操作创建独立的表单组件（通常放在 `modules/form.vue`）。
    - 使用 `useVbenDrawer` 或 `useVbenModal` 承载表单。
3.  **连接操作列**:
    - 在 `columns` 中定义操作列，使用 `onActionClick` 回调处理编辑、删除等操作。
    - 删除操作应使用 `Modal.confirm` 进行二次确认。
4.  **工具栏按钮**:
    - 使用 `#toolbar-tools` 插槽添加"新增"等自定义按钮。

## 最佳实践

- **应当 (DO)**: 使用 `gridApi.query()` 刷新表格数据。
- **应当 (DO)**: 将列配置和表单 Schema 抽离到 `data.ts` 文件中。
- **避免 (DON'T)**: 在表格配置中直接编写复杂的业务计算，应解耦至工具函数。

## 示例

**提示词:**

> 为"系统日志"创建一个带有搜索、日期范围筛选的增删改查页面。

**核心配置示例:**

```typescript
const [Grid, gridApi] = useVbenVxeGrid<LogApi.SystemLog>({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: true,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    proxyConfig: {
      ajax: {
        query: async ({ page }: any, formValues: Recordable<any>) => {
          return await getLogList({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          })
        },
      },
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  },
})

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
})

function onCreate() {
  formDrawerApi.setData({}).open()
}
```
