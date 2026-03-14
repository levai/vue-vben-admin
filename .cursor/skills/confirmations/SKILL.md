---
name: confirmations
description: 标准化二次确认与消息反馈。在用户要求为删除等敏感操作加确认、成功/失败提示、loading 反馈时使用。
---

# 交互确认与反馈

**完整工作流**：[reference/interaction-confirmations.md](reference/interaction-confirmations.md)

## 何时使用

- 删除、发布、清空等敏感操作需二次确认；需统一成功/失败/进行中反馈

## 步骤要点

1. **确认**：`Modal.confirm`（来自 `ant-design-vue`）明确后果（如「数据删除后不可恢复」）。
2. **反馈**：成功 `message.success`，失败 `message.error`（展示后端错误），进行中 `message.loading`（均来自 `ant-design-vue`）；同 key 可覆盖前一条。
3. **封装**：`confirm(content, title)` 返回 Promise，onOk resolve、onCancel reject；删除前 await confirm 再调接口。

**约定**：所有删除必须二次确认；避免短时间大量 message。
