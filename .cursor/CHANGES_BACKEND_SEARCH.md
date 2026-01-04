# 后端搜索功能改造对比

## 📋 改动概览

将后端搜索功能从核心组件 `ApiComponent` 移到各自的适配层（`web-antd` 和 `web-ele`），实现框架特定的搜索逻辑，保持核心组件的通用性。

---

## 🔄 主要改动文件

### 1. `packages/effects/common-ui/src/components/api-component/api-component.vue`

#### ❌ 移除内容（恢复到原始状态）

**1.1 移除后端搜索相关的 Props**

```typescript
// 已移除
enableBackendSearch?: boolean;
searchFieldName?: string;
searchEventName?: string;
searchDebounce?: number;
```

**1.2 移除相关导入**

```typescript
// 已移除
import { useDebounceFn } from '@vueuse/core';
```

**1.3 移除状态变量**

```typescript
// 已移除
const searchKeyword = ref<string>('');
```

**1.4 移除相关函数**

- `updateSearchParam(value: string)` - 已移除
- `handleBackendSearch(value: string)` - 已移除
- `handleVisibleChangeForBackendSearch(visible: boolean)` - 已移除
- `handleValueChange(val: any)` - 已移除

**1.5 恢复 `mergedParams` 计算属性**

```typescript
// 恢复为简单合并
const mergedParams = computed(() => {
  return {
    ...props.params,
    ...unref(innerParams),
  };
});
```

**1.6 恢复 `bindProps` 计算属性**

```typescript
// 恢复为简单绑定
const bindProps = computed(() => {
  return {
    [props.modelPropName]: unref(modelValue),
    [props.optionsPropName]: unref(getOptions),
    [`onUpdate:${props.modelPropName}`]: (val: string) => {
      modelValue.value = val;
    },
    ...objectOmit(attrs, [`onUpdate:${props.modelPropName}`]),
    ...(props.visibleEvent
      ? {
          [props.visibleEvent]: handleFetchForVisible,
        }
      : {}),
  };
});
```

**1.7 恢复 `updateParam` 方法**

```typescript
// 恢复为简单实现
updateParam(newParams: Record<string, any>) {
  innerParams.value = newParams;
}
```

---

### 2. `apps/web-antd/src/adapter/component/index.ts`

#### ✅ 新增内容

**2.1 新增导入**

```typescript
import {
  computed,
  defineAsyncComponent,
  defineComponent,
  h,
  nextTick,
  ref,
} from 'vue';

import { useDebounceFn } from '@vueuse/core';
```

**2.2 新增 `withAntDesignBackendSearch` 包装器**

```typescript
/**
 * Ant Design Vue Select 后端搜索包装器
 * Ant Design Vue Select 使用 onSearch 事件
 * 此包装器将 enableBackendSearch 转换为 onSearch 事件处理
 */
const withAntDesignBackendSearch = (
  baseComponent: Component,
  baseProps: Recordable<any> = {},
) => {
  return defineComponent({
    name: 'ApiSelectWithAntDesignBackendSearch',
    inheritAttrs: false,
    setup: (props: any, { attrs, expose, slots }) => {
      // 实现后端搜索逻辑
      // - 处理 onSearch 事件
      // - 防抖处理
      // - 参数更新
      // - 清除处理
    },
  });
};
```

**2.3 更新 `ApiSelect` 定义**

```typescript
// 改动前：直接使用 ApiComponent
ApiSelect: withDefaultPlaceholder(ApiComponent, 'select', {
  component: Select,
  loadingSlot: 'suffixIcon',
  modelPropName: 'value',
  visibleEvent: 'onVisibleChange',
}),

// 改动后：使用 withAntDesignBackendSearch 包装
ApiSelect: withAntDesignBackendSearch(
  withDefaultPlaceholder(ApiComponent, 'select', {
    component: Select,
    loadingSlot: 'suffixIcon',
    modelPropName: 'value',
    visibleEvent: 'onVisibleChange',
  }),
),
```

**2.4 核心功能**

- ✅ 处理 `onSearch` 事件（Ant Design Vue Select 的搜索事件）
- ✅ 自动设置 `showSearch: true` 和 `filterOption: false`
- ✅ 防抖处理（默认 300ms，可配置）
- ✅ 清除参数时重新获取数据
- ✅ 下拉框关闭时清除搜索参数

---

### 3. `apps/web-ele/src/adapter/component/index.ts`

#### ✅ 新增内容

**3.1 新增导入**

```typescript
import { computed, defineAsyncComponent, defineComponent, h, ref } from 'vue';

import { useDebounceFn } from '@vueuse/core';
```

**3.2 新增 `withElementPlusBackendSearch` 包装器**

```typescript
/**
 * Element Plus Select 后端搜索包装器
 * Element Plus Select 使用 remote-method prop 而不是 onSearch 事件
 * 此包装器将 enableBackendSearch 转换为 remote-method 处理
 */
const withElementPlusBackendSearch = (
  baseComponent: Component,
  baseProps: Recordable<any> = {},
) => {
  return defineComponent({
    name: 'ApiSelectWithElementPlusBackendSearch',
    inheritAttrs: false,
    setup: (props: any, { attrs, expose, slots }) => {
      // 实现后端搜索逻辑
      // - 处理 remote-method prop
      // - 防抖处理
      // - 参数更新
      // - 清除处理
    },
  });
};
```

**3.3 更新 `ApiSelect` 定义**

```typescript
// 改动前：直接使用 ApiComponent
ApiSelect: withDefaultPlaceholder(
  {
    ...ApiComponent,
    name: 'ApiSelect',
  },
  'select',
  {
    clearable: true,
    component: ElSelectV2,
    loadingSlot: 'loading',
    visibleEvent: 'onVisibleChange',
  },
),

// 改动后：使用 withElementPlusBackendSearch 包装
ApiSelect: withElementPlusBackendSearch(
  withDefaultPlaceholder(
    {
      ...ApiComponent,
      name: 'ApiSelect',
    },
    'select',
    {
      clearable: true,
      component: ElSelectV2,
      loadingSlot: 'loading',
      visibleEvent: 'onVisibleChange',
    },
  ),
),
```

**3.4 核心功能**

- ✅ 处理 `remote-method` prop（Element Plus Select 的远程搜索方法）
- ✅ 自动设置 `remote: true` 和 `filterable: true`
- ✅ 防抖处理（默认 300ms，可配置）
- ✅ 清除参数时重新获取数据
- ✅ 下拉框关闭时清除搜索参数
- ✅ 性能优化：状态缓存，避免重复更新

---

### 4. 实际使用示例

#### 4.1 `apps/web-antd/src/views/system/operation-log/data.ts`

```typescript
{
  component: 'ApiSelect',
  componentProps: {
    allowClear: true,
    api: getUserOptions,
    enableBackendSearch: true,        // 启用后端搜索（适配层自动处理）
    params: { limit: -1 },
    labelField: 'username',
    resultField: 'list',
    valueField: 'id',
  },
  fieldName: 'userId',
  label: $t('system.operationLog.username'),
}
```

#### 4.2 `apps/web-ele/src/views/system/operation-log/data.ts`

```typescript
{
  component: 'ApiSelect',
  componentProps: {
    allowClear: true,
    api: getUserOptions,
    enableBackendSearch: true,        // 启用后端搜索（适配层自动处理）
    params: { limit: -1 },
    labelField: 'username',
    resultField: 'list',
    valueField: 'id',
  },
  fieldName: 'userId',
  label: $t('system.operationLog.username'),
}
```

**注意**：两个框架的使用方式完全一致，适配层会自动处理框架差异。

---

## 🎯 功能对比

### 改动前（集成到核心组件）

| 特性         | 实现方式                     |
| ------------ | ---------------------------- |
| **架构**     | 核心组件包含所有逻辑（单层） |
| **代码位置** | `api-component.vue`          |
| **框架差异** | 需要判断不同框架的事件机制   |
| **维护性**   | 框架特定逻辑混在核心组件中   |
| **扩展性**   | 新增框架需要修改核心组件     |

### 改动后（适配层处理）

| 特性         | 实现方式                          |
| ------------ | --------------------------------- |
| **架构**     | 核心组件通用 + 适配层处理（分层） |
| **代码位置** | `adapter/component/index.ts`      |
| **框架差异** | 各适配层独立处理框架特定逻辑      |
| **维护性**   | 框架特定逻辑隔离在适配层          |
| **扩展性**   | 新增框架只需添加适配层包装器      |

---

## ✨ 架构优势

### 1. 核心组件保持通用

- `ApiComponent` 不包含任何 UI 框架特定逻辑
- 可以轻松适配新的 UI 框架
- 核心逻辑清晰，易于维护

### 2. 适配层处理差异

- **Ant Design Vue**：使用 `onSearch` 事件
- **Element Plus**：使用 `remote-method` prop
- 各框架的特殊处理集中在各自的适配层

### 3. 易于维护

- 修改某个 UI 框架的逻辑不影响其他框架
- 代码职责清晰，便于定位问题

### 4. 易于扩展

- 新增 UI 框架时，只需在适配层添加包装器
- 不需要修改核心组件

---

## 🔧 技术细节

### 1. Ant Design Vue 适配层实现

```typescript
// 处理后端搜索（带防抖）
const handleSearch = useDebounceFn((value: string) => {
  if (!enableBackendSearch) {
    // 如果未启用后端搜索，调用原有的 onSearch 事件
    if (typeof attrs?.onSearch === 'function') {
      attrs.onSearch(value);
    }
    return;
  }
  updateSearchParam(value);
}, searchDebounce);

// 自动配置（在 mergedAttrs 中）
if (enableBackendSearch) {
  // 确保启用搜索功能
  if (merged.showSearch === undefined) {
    merged.showSearch = true;
  }

  // 合并 onSearch 事件（保留原有处理函数）
  const existingSearchHandler = attrs?.onSearch;
  merged.onSearch = (value: string) => {
    if (typeof existingSearchHandler === 'function') {
      existingSearchHandler(value);
    }
    handleSearch(value);
  };

  // 禁用前端过滤
  if (merged.filterOption === undefined) {
    merged.filterOption = false;
  }
}
```

### 2. Element Plus 适配层实现

```typescript
// 处理后端搜索（带防抖）
const handleRemoteMethod = useDebounceFn((value: string) => {
  if (!enableBackendSearch) return;
  updateSearchParam(value);
}, searchDebounce);

// 自动配置（在 mergedAttrs 中）
if (enableBackendSearch) {
  // 确保启用 remote 和 filterable
  if (merged.remote === undefined) {
    merged.remote = true;
  }
  if (merged.filterable === undefined) {
    merged.filterable = true;
  }

  // 合并 remote-method（保留原有处理函数）
  const existingRemoteMethod = attrs?.remoteMethod;
  merged.remoteMethod = (value: string) => {
    // 先调用原有的 remote-method（如果存在）
    if (typeof existingRemoteMethod === 'function') {
      existingRemoteMethod(value);
    }
    // 然后处理后端搜索（防抖已内置）
    handleRemoteMethod(value);
  };
}
```

### 3. 参数更新机制

```typescript
// 维护当前搜索参数状态，避免不必要的更新
const currentSearchParam = ref<null | string>(null);

// 更新搜索参数
const updateSearchParam = (value: string) => {
  if (!enableBackendSearch) return;

  const trimmedValue = value?.trim() || '';
  searchKeyword.value = trimmedValue;

  // 如果搜索关键词没有变化，不更新参数（避免重复请求）
  if (currentSearchParam.value === trimmedValue) {
    return;
  }

  currentSearchParam.value = trimmedValue;

  const updateParamFn = getUpdateParam();
  if (updateParamFn) {
    if (trimmedValue) {
      // 有搜索关键词，设置参数
      updateParamFn({ [searchFieldName]: trimmedValue });
    } else {
      // 搜索关键词为空，直接清除参数
      // 如果之前没有搜索参数，不需要更新
      if (currentSearchParam.value !== null) {
        updateParamFn({});
      }
    }
  }
};
```

**关键优化点**：

- ✅ 使用 `currentSearchParam` 缓存当前搜索参数，避免重复更新
- ✅ 空值处理：直接清除参数，不再使用 `__CLEAR__` 标记
- ✅ 性能优化：相同搜索关键词不会触发重复请求

### 4. 下拉框关闭处理

```typescript
// 处理下拉框打开/关闭事件
const handleVisibleChange = (visible: boolean) => {
  // 如果关闭下拉框且搜索关键词为空，清除搜索参数
  if (
    !visible &&
    !searchKeyword.value &&
    enableBackendSearch &&
    currentSearchParam.value !== null
  ) {
    currentSearchParam.value = null;
    const updateParamFn = getUpdateParam();
    if (updateParamFn) {
      updateParamFn({});
    }
  }

  // 调用原有的 onVisibleChange 事件
  if (typeof attrs?.onVisibleChange === 'function') {
    attrs.onVisibleChange(visible);
  }
};
```

### 5. 值清除处理

```typescript
// 处理值变化事件（清除时重新获取数据）
const handleChange = (val: any, ...args: any[]) => {
  // 调用原有的 onChange 事件
  if (typeof attrs?.onChange === 'function') {
    attrs.onChange(val, ...args);
  }

  // 如果值被清除，清除搜索参数
  if (
    enableBackendSearch &&
    (val === undefined || val === null || val === '')
  ) {
    searchKeyword.value = '';
    // 重置搜索参数状态
    if (currentSearchParam.value !== null) {
      currentSearchParam.value = null;
      const updateParamFn = getUpdateParam();
      if (updateParamFn) {
        updateParamFn({});
      }
    }
  }
};
```

---

## 📊 性能优化

### 1. 防抖优化

- 减少搜索请求频率（用户停止输入 300ms 后才请求）
- 默认 300ms，可通过 `searchDebounce` 配置

### 2. 状态缓存

- 使用 `currentSearchParam` 缓存当前搜索参数
- 避免重复更新相同参数，减少不必要的 API 调用
- 两个框架都实现了相同的优化

### 3. 事件处理优化

- 统一事件处理，减少重复逻辑
- 保留外部事件处理函数，确保兼容性
- 先调用原有事件处理函数，再执行后端搜索逻辑

### 4. 清除逻辑优化

- 下拉框关闭时自动清除搜索参数（如果搜索关键词为空）
- 值清除时同步清除搜索参数
- 避免双重更新，直接清除参数而不是使用标记值

---

## ✅ 向后兼容性

### 兼容性保证

- ✅ **默认关闭**：`enableBackendSearch` 默认为 `false`，现有组件不受影响
- ✅ **条件保护**：只有显式启用才会执行后端搜索逻辑
- ✅ **外部优先**：保留外部设置的事件处理函数，先调用原有函数再执行搜索逻辑
- ✅ **事件兼容**：保留并先调用原有的搜索事件处理函数（`onSearch` / `remote-method`）
- ✅ **属性透传**：`enableBackendSearch`、`searchFieldName`、`searchDebounce` 不会传递给底层组件

### 迁移指南

**无需迁移**：现有使用 `ApiSelect` 的组件无需任何修改，功能完全兼容。

**启用后端搜索**：只需在 `componentProps` 中添加 `enableBackendSearch: true` 即可：

```typescript
// 改动前
{
  component: 'ApiSelect',
  componentProps: {
    api: getUserOptions,
    // ... 其他配置
  },
}

// 改动后（只需添加一行）
{
  component: 'ApiSelect',
  componentProps: {
    api: getUserOptions,
    enableBackendSearch: true,  // 新增这一行
    // ... 其他配置
  },
}
```

**结论**：现有组件不会受到影响，可以安全使用。需要后端搜索时，只需添加配置即可。

---

## 📝 使用示例

### 基础用法

```typescript
{
  component: 'ApiSelect',
  componentProps: {
    api: getUserOptions,
    enableBackendSearch: true,  // 适配层会自动处理框架差异
    labelField: 'username',
    resultField: 'list',
    valueField: 'id',
  },
  fieldName: 'userId',
  label: '用户',
}
```

### 高级配置

```typescript
{
  component: 'ApiSelect',
  componentProps: {
    api: getUserOptions,
    enableBackendSearch: true,
    searchFieldName: 'keyword',      // 自定义搜索字段名（默认: 'search'）
    searchDebounce: 500,              // 自定义防抖延迟（默认: 300ms）
    params: { limit: -1 },
    labelField: 'username',
    resultField: 'list',
    valueField: 'id',
  },
  fieldName: 'userId',
  label: '用户',
}
```

### 在 VxeTable Grid 中使用

```typescript
// apps/web-antd/src/views/system/role/list.vue
const [Grid, gridApi] = useVbenVxeGrid<SystemRoleApi.SystemRole>({
  formOptions: {
    schema: useGridFormSchema(), // 包含 enableBackendSearch 的 ApiSelect
    submitOnChange: true,
  },
  gridOptions: {
    proxyConfig: {
      ajax: {
        query: async ({ page }: any, formValues: Recordable<any>) => {
          // formValues 会自动包含后端搜索参数
          return await getRoleList({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues, // 包含搜索关键词
          });
        },
      },
    },
  },
});
```

### 配置说明

| 配置项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enableBackendSearch` | `boolean` | `false` | 是否启用后端搜索 |
| `searchFieldName` | `string` | `'search'` | 搜索参数字段名，会传递给 API |
| `searchDebounce` | `number` | `300` | 防抖延迟时间（毫秒） |

### 注意事项

1. **API 参数处理**：后端搜索的关键词会通过 `searchFieldName` 指定的字段名传递给 API，确保后端接口支持该参数。

2. **防抖优化**：默认 300ms 防抖，用户停止输入后才会触发搜索，可根据实际情况调整。

3. **下拉框关闭**：下拉框关闭时会自动清除搜索参数（如果搜索关键词为空），确保下次打开时显示完整数据。

4. **值清除**：当选择的值被清除时，搜索参数也会同步清除。

5. **框架差异**：两个框架的使用方式完全一致，适配层会自动处理：
   - **Ant Design Vue**：使用 `onSearch` 事件
   - **Element Plus**：使用 `remote-method` prop

---

## 🎉 总结

这次改造将后端搜索功能从核心组件移到适配层，带来了：

1. **架构优化**：核心组件保持通用，框架特定逻辑隔离在适配层
2. **易于维护**：修改某个框架的逻辑不影响其他框架
3. **易于扩展**：新增 UI 框架只需添加适配层包装器
4. **性能提升**：状态缓存、防抖优化，减少不必要的 API 调用
5. **向后兼容**：不影响现有组件，安全可靠
6. **使用简单**：两个框架使用方式完全一致，只需添加 `enableBackendSearch: true`

### 架构对比

```text
改动前：
ApiComponent (包含所有框架逻辑)
  └── web-antd
  └── web-ele

改动后：
ApiComponent (通用核心)
  ├── web-antd (withAntDesignBackendSearch)
  │   └── 处理 onSearch 事件
  └── web-ele (withElementPlusBackendSearch)
      └── 处理 remote-method prop
```

### 核心优势

1. **单一职责**：每个层次都有明确的职责
   - `ApiComponent`：通用 API 数据获取逻辑
   - 适配层包装器：框架特定的搜索事件处理

2. **框架隔离**：各框架的特殊处理集中在各自的适配层
   - Ant Design Vue：`onSearch` 事件
   - Element Plus：`remote-method` prop

3. **易于扩展**：新增 UI 框架时，只需：
   - 在适配层添加新的包装器函数
   - 处理该框架的搜索事件机制
   - 不需要修改核心组件

4. **性能优化**：
   - 防抖处理（默认 300ms）
   - 状态缓存，避免重复请求
   - 智能清除，避免不必要的更新

这种架构更符合单一职责原则和开闭原则，便于维护和扩展。
