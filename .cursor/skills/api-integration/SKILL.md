---
name: api-integration
description: 将后端 API 集成到前端（契约、类型、服务层、UI 接入）。在用户要求对接新接口、写 api 层时使用。CRUD 中的 API 结构见 crud-with-drawer。
---

# API 集成

**完整工作流**：[reference/api-integration.md](reference/api-integration.md)  
**项目规范**：`.cursor/rules/api-rules.mdc`（requestClient、命名空间类型、getXxxList 等）

## 何时使用

- 新增或修改前端对后端接口的调用；需定义请求/响应类型

## 步骤要点

1. **契约**：确认方法、入参、出参；列表返回字段为 **list**（+ total）。
2. **类型与实现**：见 `.cursor/rules/api-rules.mdc`。在 **`src/api/`** 下命名空间导出类型 + requestClient 调用，导出 getXxxList 等函数。
3. **UI**：组件中调用，处理 loading/成功/失败；联调验证。
