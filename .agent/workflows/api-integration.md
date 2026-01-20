---
description: 引导 Agent 完成将后端 API 集成到前端应用程序的过程。
---

# API 集成工作流

此工作流简化了前端与后端服务的连接过程，确保数据获取、错误处理和状态管理模式的一致性。

## 必备条件

- 后端 API 端点信息或 Swagger 文档。
- 项目中预配置的 HTTP 客户端。
- 了解 API 服务层目录结构。

## 工作流步骤

1.  **契约评审**: 分析 API 端点、方法、入参和出参。需确认列表数据字段名为 `list`。
2.  **定义类型**: 创建请求和响应的 TypeScript 接口。
3.  **实现服务层**: 在 `api/` 目录添加相应的调用函数。
4.  **状态集成**: (可选) 与 Pinia 等状态管理库集成。
5.  **UI 接入**: 在组件中调用服务，处理加载、成功及失败状态。
6.  **端到端验证**: 进行接口联调验证。

## 最佳实践

- **应当 (DO)**: 始终包含错误处理机制。
- **避免 (DON'T)**: 在多个地方重复定义相同的 API 调用。

## 示例

**提示词:**

> 集成一个 API 来获取当前用户的个人资料详情。

**代码示例 (Service):**

```typescript
// api/user.ts
import { defHttp } from '/@/utils/http/axios'

export interface UserProfile {
  id: string
  name: string
  email: string
}

export const getUserProfile = () => {
  return defHttp.get<UserProfile>({ url: '/user/profile' })
}
```
