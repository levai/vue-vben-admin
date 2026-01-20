---
description: 规范化使用 Vben Admin 中的 useVbenModal 和 useVbenDrawer。
---

# 弹窗与抽屉使用工作流

此工作流确保应用内的弹出层交互（Modal/Drawer）体验一致且代码结构标准。

## 必备条件

- 确定展示内容量：内容较少推荐 Modal，内容较多或层级较深推荐 Drawer。
- 了解 `useVbenModal` / `useVbenDrawer` 钩子的使用。

## 工作流步骤

1.  **定义组件**: 在独立文件中创建 Modal/Drawer 内部组件（通常在 `modules/` 目录下）。
2.  **主页面注册**: 使用 `useVbenModal` (或 `useVbenDrawer`) 获取组件和 API。
3.  **数据传递**: 通过 `api.setData(data).open()` 开启弹窗并传递初始化数据。
4.  **回调处理**:
    - 在内部组件中通过 `emit('success')` 通知父组件刷新数据。
    - 使用 `api.close()` 关闭弹窗。
5.  **异步控制**: 使用 `api.lock()` 和 `api.unlock()` 控制提交按钮的加载状态。

## 最佳实践

- **应当 (DO)**: 弹窗内部应具备基本的表单校验逻辑。
- **应当 (DO)**: 使用 `destroyOnClose: true` 确保每次打开都是全新状态。
- **避免 (DON'T)**: 在一个组件内堆叠多个 Modal，应尽量组件化拆分。

## 示例

**提示词:**

> 创建一个用于修改用户信息的抽屉。

**调用示例:**

```typescript
const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
})

function onEdit(row: User) {
  formDrawerApi.setData(row).open()
}

function onRefresh() {
  gridApi.query()
}
```

**模板示例:**

```vue
<FormDrawer @success="onRefresh" />
```
