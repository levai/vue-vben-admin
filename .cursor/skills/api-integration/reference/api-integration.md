---
description: 引导 Agent 完成将后端 API 集成到前端应用程序的过程。
---

# API 集成工作流

前端与后端对接时，类型与命名约定以 **`.cursor/rules/api-rules.mdc`** 为准；此处为步骤与示例。

## 路径与约定

- **API 层位置**：`frontend/apps/web-antd/src/api/`，按模块分（如 `system/user.ts`）。
- **客户端**：仅用 `requestClient`（`#/api/request`），禁止 axios / baseRequestClient。
- **类型**：命名空间导出（如 `export namespace XxxApi { export interface Xxx { ... } }`）。
- **列表响应**：字段名为 **list**（+ total）。

## 工作流步骤

1. **契约**：确认接口方法、入参、出参；列表返回 `{ list, total }`。
2. **类型**：在 `src/api/` 下新建或修改模块文件，定义命名空间与接口，与后端字段一致。
3. **实现**：`requestClient.get/post/put/delete`，导出 getXxxList、getXxxById、createXxx、updateXxx、deleteXxx、getXxxOptions（按需）。
4. **UI**：在组件中调用，处理 loading/成功/失败；联调验证。

## 示例（与项目一致）

```typescript
// frontend/apps/web-antd/src/api/system/xxx.ts
import type { Recordable } from '@vben/types';
import { requestClient } from '#/api/request';

export namespace XxxApi {
  export interface Xxx {
    id: string;
    name: string;
    // ...
  }
}

async function getXxxList(params: Recordable<any>) {
  return requestClient.get<{ list: XxxApi.Xxx[]; total: number }>('/system/xxx', { params });
}

async function getXxxById(id: string) {
  return requestClient.get<XxxApi.Xxx>(`/system/xxx/${id}`);
}

export { getXxxList, getXxxById };
```

## 检查清单

- [ ] 类型命名空间导出，无 `any`
- [ ] 仅用 requestClient，方法命名 getXxxList / createXxx 等
- [ ] 列表接口返回类型含 list、total
- [ ] 导出函数并在组件中按需使用
