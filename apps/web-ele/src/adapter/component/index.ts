/**
 * 通用组件共同的使用的基础组件，原先放在 adapter/form 内部，限制了使用范围，这里提取出来，方便其他地方使用
 * 可用于 vben-form、vben-modal、vben-drawer 等组件使用,
 */

/* eslint-disable vue/one-component-per-file */

import type { Component } from 'vue';

import type { BaseFormComponentType } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { computed, defineAsyncComponent, defineComponent, h, ref } from 'vue';

import { ApiComponent, globalShareState, IconPicker } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { useDebounceFn } from '@vueuse/core';
import { ElNotification } from 'element-plus';

const ElAutocomplete = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/autocomplete/index'),
    import('element-plus/es/components/autocomplete/style/css'),
  ]).then(([res]) => res.ElAutocomplete),
);
const ElButton = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/button/index'),
    import('element-plus/es/components/button/style/css'),
  ]).then(([res]) => res.ElButton),
);
const ElCheckbox = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/checkbox/index'),
    import('element-plus/es/components/checkbox/style/css'),
  ]).then(([res]) => res.ElCheckbox),
);
const ElCheckboxButton = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/checkbox/index'),
    import('element-plus/es/components/checkbox-button/style/css'),
  ]).then(([res]) => res.ElCheckboxButton),
);
const ElCheckboxGroup = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/checkbox/index'),
    import('element-plus/es/components/checkbox-group/style/css'),
  ]).then(([res]) => res.ElCheckboxGroup),
);
const ElDatePicker = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/date-picker/index'),
    import('element-plus/es/components/date-picker/style/css'),
  ]).then(([res]) => res.ElDatePicker),
);
const ElDivider = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/divider/index'),
    import('element-plus/es/components/divider/style/css'),
  ]).then(([res]) => res.ElDivider),
);
const ElInput = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/input/index'),
    import('element-plus/es/components/input/style/css'),
  ]).then(([res]) => res.ElInput),
);
// Textarea 使用 ElInput 并设置 type="textarea"
const Textarea = defineComponent({
  name: 'VbenTextarea',
  setup(props, { attrs, slots }) {
    return () => h(ElInput, { ...props, ...attrs, type: 'textarea' }, slots);
  },
});
const ElInputNumber = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/input-number/index'),
    import('element-plus/es/components/input-number/style/css'),
  ]).then(([res]) => res.ElInputNumber),
);
const ElRadio = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/radio/index'),
    import('element-plus/es/components/radio/style/css'),
  ]).then(([res]) => res.ElRadio),
);
const ElRadioButton = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/radio/index'),
    import('element-plus/es/components/radio-button/style/css'),
  ]).then(([res]) => res.ElRadioButton),
);
const ElRadioGroup = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/radio/index'),
    import('element-plus/es/components/radio-group/style/css'),
  ]).then(([res]) => res.ElRadioGroup),
);
const ElSelectV2 = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/select-v2/index'),
    import('element-plus/es/components/select-v2/style/css'),
  ]).then(([res]) => res.ElSelectV2),
);
const ElSpace = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/space/index'),
    import('element-plus/es/components/space/style/css'),
  ]).then(([res]) => res.ElSpace),
);
const ElSwitch = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/switch/index'),
    import('element-plus/es/components/switch/style/css'),
  ]).then(([res]) => res.ElSwitch),
);
const ElTimePicker = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/time-picker/index'),
    import('element-plus/es/components/time-picker/style/css'),
  ]).then(([res]) => res.ElTimePicker),
);
const ElTreeSelect = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/tree-select/index'),
    import('element-plus/es/components/tree-select/style/css'),
  ]).then(([res]) => res.ElTreeSelect),
);
const ElUpload = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/upload/index'),
    import('element-plus/es/components/upload/style/css'),
  ]).then(([res]) => res.ElUpload),
);
const ElCascader = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/cascader/index'),
    import('element-plus/es/components/cascader/style/css'),
  ]).then(([res]) => res.ElCascader),
);
// Element Plus 的 InputPassword 是 Input 组件，使用 type="password" 和 show-password 属性
const ElInputPassword = defineComponent({
  name: 'ElInputPassword',
  setup(props, { attrs, slots }) {
    return h(
      ElInput,
      {
        ...props,
        ...attrs,
        type: 'password',
        'show-password': true,
      },
      slots,
    );
  },
});
const ElRate = defineAsyncComponent(() =>
  Promise.all([
    import('element-plus/es/components/rate/index'),
    import('element-plus/es/components/rate/style/css'),
  ]).then(([res]) => res.ElRate),
);

const withDefaultPlaceholder = <T extends Component>(
  component: T,
  type: 'input' | 'select',
  componentProps: Recordable<any> = {},
) => {
  return defineComponent({
    name: component.name,
    inheritAttrs: false,
    setup: (props: any, { attrs, expose, slots }) => {
      const placeholder =
        props?.placeholder ||
        attrs?.placeholder ||
        $t(`ui.placeholder.${type}`);
      // 透传组件暴露的方法
      const innerRef = ref();
      expose(
        new Proxy(
          {},
          {
            get: (_target, key) => innerRef.value?.[key],
            has: (_target, key) => key in (innerRef.value || {}),
          },
        ),
      );
      return () =>
        h(
          component,
          { ...componentProps, placeholder, ...props, ...attrs, ref: innerRef },
          slots,
        );
    },
  });
};

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
      const wrapperRef = ref<any>();
      const searchKeyword = ref<string>('');

      // 检查是否启用后端搜索
      const enableBackendSearch =
        props?.enableBackendSearch ?? attrs?.enableBackendSearch ?? false;

      // 获取搜索参数字段名，默认为 'search'
      const searchFieldName =
        props?.searchFieldName ?? attrs?.searchFieldName ?? 'search';

      // 获取防抖延迟时间，默认为 300ms
      const searchDebounce =
        props?.searchDebounce ?? attrs?.searchDebounce ?? 300;

      // 获取 updateParam 方法
      const getUpdateParam = () => {
        if (!wrapperRef.value) return null;
        try {
          const updateParam = wrapperRef.value.updateParam;
          return typeof updateParam === 'function' ? updateParam : null;
        } catch {
          return null;
        }
      };

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

      // 处理后端搜索（带防抖）
      const handleRemoteMethod = useDebounceFn((value: string) => {
        if (!enableBackendSearch) return;
        updateSearchParam(value);
      }, searchDebounce);

      // 处理下拉框打开/关闭事件
      const handleVisibleChange = (visible: boolean) => {
        // 如果关闭下拉框且搜索关键词为空，清除搜索参数
        if (
          !visible &&
          !searchKeyword.value &&
          enableBackendSearch && // 重置搜索参数状态
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

      // 处理值变化事件（清除时重新获取数据）
      const handleChange = (val: any) => {
        // 调用原有的 onChange 事件
        if (typeof attrs?.onChange === 'function') {
          attrs.onChange(val);
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

      // 透传组件暴露的方法
      const innerRef = ref();
      expose(
        new Proxy(
          {},
          {
            get: (_target, key) => {
              if (wrapperRef.value && key in wrapperRef.value) {
                return (wrapperRef.value as any)[key];
              }
              return innerRef.value?.[key];
            },
            has: (_target, key) =>
              (wrapperRef.value && key in wrapperRef.value) ||
              key in (innerRef.value || {}),
          },
        ),
      );

      // 合并属性
      const mergedAttrs = computed(() => {
        const merged = {
          ...baseProps,
          ...props,
          ...attrs,
        };

        // 如果启用后端搜索，配置 Element Plus Select 的 remote-method
        if (enableBackendSearch) {
          // 确保启用 remote 和 filterable
          if (merged.remote === undefined) {
            merged.remote = true;
          }
          if (merged.filterable === undefined) {
            merged.filterable = true;
          }

          // 合并 remote-method
          const existingRemoteMethod = attrs?.remoteMethod;
          merged.remoteMethod = (value: string) => {
            // 先调用原有的 remote-method（如果存在）
            if (typeof existingRemoteMethod === 'function') {
              existingRemoteMethod(value);
            }
            // 然后处理后端搜索（防抖已内置）
            handleRemoteMethod(value);
          };

          // 绑定 onVisibleChange
          const existingVisibleChangeHandler = attrs?.onVisibleChange;
          merged.onVisibleChange = (visible: boolean) => {
            if (typeof existingVisibleChangeHandler === 'function') {
              existingVisibleChangeHandler(visible);
            }
            handleVisibleChange(visible);
          };

          // 绑定 onChange
          const existingChangeHandler = attrs?.onChange;
          merged.onChange = (val: any, ...args: any[]) => {
            if (typeof existingChangeHandler === 'function') {
              existingChangeHandler(val, ...args);
            }
            handleChange(val);
          };
        }

        // 移除 enableBackendSearch、searchFieldName、searchDebounce，不传递给底层组件
        delete merged.enableBackendSearch;
        delete merged.searchFieldName;
        delete merged.searchDebounce;

        return merged;
      });

      return () => {
        return h(
          baseComponent,
          {
            ...mergedAttrs.value,
            ref: (el: any) => {
              wrapperRef.value = el;
              innerRef.value = el;
            },
          },
          slots,
        );
      };
    },
  });
};

// 这里需要自行根据业务组件库进行适配，需要用到的组件都需要在这里类型说明
export type ComponentType =
  | 'ApiCascader'
  | 'ApiSelect'
  | 'ApiTreeSelect'
  | 'AutoComplete'
  | 'Cascader'
  | 'Checkbox'
  | 'CheckboxGroup'
  | 'DatePicker'
  | 'DefaultButton'
  | 'Divider'
  | 'IconPicker'
  | 'Input'
  | 'InputNumber'
  | 'InputPassword'
  | 'PrimaryButton'
  | 'Radio'
  | 'RadioGroup'
  | 'RangePicker'
  | 'Rate'
  | 'Select'
  | 'Space'
  | 'Switch'
  | 'Textarea'
  | 'TimePicker'
  | 'TreeSelect'
  | 'Upload'
  | BaseFormComponentType;

async function initComponentAdapter() {
  const components: Partial<Record<ComponentType, Component>> = {
    // 如果你的组件体积比较大，可以使用异步加载
    // Button: () =>
    // import('xxx').then((res) => res.Button),
    ApiCascader: withDefaultPlaceholder(
      {
        ...ApiComponent,
        name: 'ApiCascader',
      },
      'select',
      {
        clearable: true,
        component: ElCascader,
        props: { label: 'label', value: 'value', children: 'children' },
        loadingSlot: 'loading',
        visibleEvent: 'onVisibleChange',
      },
    ),
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
    ApiTreeSelect: withDefaultPlaceholder(
      {
        ...ApiComponent,
        name: 'ApiTreeSelect',
      },
      'select',
      {
        clearable: true,
        component: ElTreeSelect,
        props: { label: 'label', value: 'value', children: 'children' },
        nodeKey: 'value',
        loadingSlot: 'loading',
        optionsPropName: 'data',
        visibleEvent: 'onVisibleChange',
      },
    ),
    AutoComplete: (props, { attrs, slots }) => {
      // Element Plus 的 ElAutocomplete 需要 fetchSuggestions 回调
      // Ant Design Vue 使用 options 数组和 filterOption 方法
      const { options, filterOption, ...restAttrs } = attrs;

      // 创建 fetchSuggestions 回调函数
      const fetchSuggestions = (
        queryString: string,
        callback: (suggestions: any[]) => void,
      ) => {
        if (!options || !Array.isArray(options)) {
          callback([]);
          return;
        }

        // 如果没有输入，返回所有选项
        if (!queryString) {
          callback(options);
          return;
        }

        // 如果有自定义的 filterOption 函数，使用它
        if (typeof filterOption === 'function') {
          const filtered = options.filter((option) =>
            filterOption(queryString, option),
          );
          callback(filtered);
        } else {
          // 默认过滤逻辑：匹配 value 字段
          const filtered = options.filter((option) => {
            const value = option.value || option.label || '';
            return value.toLowerCase().includes(queryString.toLowerCase());
          });
          callback(filtered);
        }
      };

      return h(
        ElAutocomplete,
        {
          ...props,
          ...restAttrs,
          fetchSuggestions,
          placeholder:
            props?.placeholder ||
            attrs?.placeholder ||
            $t('ui.placeholder.input'),
        },
        slots,
      );
    },
    Cascader: withDefaultPlaceholder(ElCascader, 'select', {
      clearable: true,
    }),
    Checkbox: ElCheckbox,
    CheckboxGroup: (props, { attrs, slots }) => {
      let defaultSlot;
      if (Reflect.has(slots, 'default')) {
        defaultSlot = slots.default;
      } else {
        const { options, isButton } = attrs;
        if (Array.isArray(options)) {
          defaultSlot = () =>
            options.map((option) =>
              h(isButton ? ElCheckboxButton : ElCheckbox, option),
            );
        }
      }
      return h(
        ElCheckboxGroup,
        { ...props, ...attrs },
        { ...slots, default: defaultSlot },
      );
    },
    // 自定义默认按钮
    DefaultButton: (props, { attrs, slots }) => {
      return h(ElButton, { ...props, attrs, type: 'info' }, slots);
    },
    // 自定义主要按钮
    PrimaryButton: (props, { attrs, slots }) => {
      return h(ElButton, { ...props, attrs, type: 'primary' }, slots);
    },
    Divider: ElDivider,
    IconPicker: withDefaultPlaceholder(IconPicker, 'select', {
      iconSlot: 'append',
      modelValueProp: 'model-value',
      inputComponent: ElInput,
    }),
    Input: withDefaultPlaceholder(ElInput, 'input', {
      clearable: true,
    }),
    InputNumber: withDefaultPlaceholder(ElInputNumber, 'input', {
      clearable: true,
    }),
    InputPassword: withDefaultPlaceholder(ElInputPassword, 'input'),
    Textarea: withDefaultPlaceholder(Textarea, 'input'),
    Radio: ElRadio,
    RadioGroup: (props, { attrs, slots }) => {
      let defaultSlot;
      if (Reflect.has(slots, 'default')) {
        defaultSlot = slots.default;
      } else {
        const { options } = attrs;
        if (Array.isArray(options)) {
          defaultSlot = () =>
            options.map((option) =>
              h(attrs.isButton ? ElRadioButton : ElRadio, option),
            );
        }
      }
      return h(
        ElRadioGroup,
        { ...props, ...attrs },
        { ...slots, default: defaultSlot },
      );
    },
    Select: (props, { attrs, slots }) => {
      return h(ElSelectV2, { ...props, attrs }, slots);
    },
    Space: ElSpace,
    Switch: ElSwitch,
    TimePicker: (props, { attrs, slots }) => {
      const { name, id, isRange } = props;
      const extraProps: Recordable<any> = {};
      if (isRange) {
        if (name && !Array.isArray(name)) {
          extraProps.name = [name, `${name}_end`];
        }
        if (id && !Array.isArray(id)) {
          extraProps.id = [id, `${id}_end`];
        }
      }
      return h(
        ElTimePicker,
        {
          clearable: true,
          ...props,
          ...attrs,
          ...extraProps,
        },
        slots,
      );
    },
    DatePicker: (props, { attrs, slots }) => {
      const { name, id, type } = props;
      const extraProps: Recordable<any> = {};
      if (type && type.includes('range')) {
        if (name && !Array.isArray(name)) {
          extraProps.name = [name, `${name}_end`];
        }
        if (id && !Array.isArray(id)) {
          extraProps.id = [id, `${id}_end`];
        }
        // 日期范围选择器需要使用 start-placeholder 和 end-placeholder
        const startPlaceholder =
          props?.['start-placeholder'] ||
          attrs?.['start-placeholder'] ||
          props?.placeholder ||
          attrs?.placeholder ||
          $t('ui.placeholder.startDate');
        const endPlaceholder =
          props?.['end-placeholder'] ||
          attrs?.['end-placeholder'] ||
          props?.placeholder ||
          attrs?.placeholder ||
          $t('ui.placeholder.endDate');
        extraProps['start-placeholder'] = startPlaceholder;
        extraProps['end-placeholder'] = endPlaceholder;
      } else {
        // 单个日期选择器使用 placeholder
        const placeholder =
          props?.placeholder ||
          attrs?.placeholder ||
          $t('ui.placeholder.input');
        extraProps.placeholder = placeholder;
      }
      return h(
        ElDatePicker,
        {
          clearable: true,
          ...props,
          ...attrs,
          ...extraProps,
        },
        slots,
      );
    },
    RangePicker: (props, { attrs, slots }) => {
      const { name, id } = props;
      const extraProps: Recordable<any> = {};
      // RangePicker 默认是日期范围选择
      if (name && !Array.isArray(name)) {
        extraProps.name = [name, `${name}_end`];
      }
      if (id && !Array.isArray(id)) {
        extraProps.id = [id, `${id}_end`];
      }
      // Element Plus 日期范围选择器需要使用 start-placeholder 和 end-placeholder
      const startPlaceholder =
        props?.['start-placeholder'] ||
        attrs?.['start-placeholder'] ||
        props?.placeholder ||
        attrs?.placeholder ||
        $t('ui.placeholder.startDate');
      const endPlaceholder =
        props?.['end-placeholder'] ||
        attrs?.['end-placeholder'] ||
        props?.placeholder ||
        attrs?.placeholder ||
        $t('ui.placeholder.endDate');
      return h(
        ElDatePicker,
        {
          type: 'daterange',
          'start-placeholder': startPlaceholder,
          'end-placeholder': endPlaceholder,
          ...props,
          ...attrs,
          ...extraProps,
        },
        slots,
      );
    },
    Rate: ElRate,
    TreeSelect: withDefaultPlaceholder(ElTreeSelect, 'select', {
      clearable: true,
    }),
    Upload: ElUpload,
  };

  // 将组件注册到全局共享状态中
  globalShareState.setComponents(components);

  // 定义全局共享状态中的消息提示
  globalShareState.defineMessage({
    // 复制成功消息提示
    copyPreferencesSuccess: (title, content) => {
      ElNotification({
        title,
        message: content,
        position: 'bottom-right',
        duration: 0,
        type: 'success',
      });
    },
  });
}

export { initComponentAdapter };
