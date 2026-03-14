---
description: 标准化 Vben Admin 中的二次确认、消息提示与反馈交互。
---

# 交互确认与反馈工作流

此工作流确保用户在执行敏感操作（如删除、发布）时得到充分提醒，并提供一致的反馈信息。

## 必备条件

- 使用 **Ant Design Vue** 的 `Modal.confirm` 和 `message`：`import { message, Modal } from 'ant-design-vue';`
- 路径：frontend/apps/web-antd 下组件均可直接使用。

## 工作流步骤

1.  **敏感操作阻断**:
    - 使用 `Modal.confirm` 弹出危险操作提醒。
    - 明确告知用户：操作后果（如"数据删除后不可恢复"）。
2.  **执行反馈**:
    - **成功**: 使用 `message.success` 展示成功信息。
    - **失败**: 使用 `message.error` 展示后端错误内容。
    - **进行中**: 使用 `message.loading` 展示加载状态。
3.  **二次验证**: (如果需要) 针对极高风险操作（如清库），要求用户手动输入确认词。

## 最佳实践

- **应当 (DO)**: 为所有的"删除"操作添加二次确认。
- **应当 (DO)**: 后端接口报错时，应统一通过错误反馈展示具体原因，而非通用"操作失败"。
- **避免 (DON'T)**: 在短时间内弹出过多的 message 遮挡屏幕。

## 示例

**提示词:**

> 为用户列表的"删除"按钮添加确认。

**代码示例:**

```typescript
import { message, Modal } from 'ant-design-vue'

function confirm(content: string, title: string) {
  return new Promise((resolve, reject) => {
    Modal.confirm({
      content,
      title,
      onOk() {
        resolve(true)
      },
      onCancel() {
        reject(new Error('已取消'))
      },
    })
  })
}

async function onDelete(row: User) {
  try {
    await confirm(`确定要删除用户 ${row.username} 吗？`, '删除确认')
    const hideLoading = message.loading({
      content: `正在删除 ${row.username}...`,
      duration: 0,
      key: 'delete_msg',
    })
    await deleteUser(row.id)
    message.success({
      content: `删除 ${row.username} 成功`,
      key: 'delete_msg',
    })
    onRefresh()
  } catch {
    // 用户取消或删除失败
  }
}
```
