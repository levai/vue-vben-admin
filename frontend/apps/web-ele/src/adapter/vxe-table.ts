import type { VxeTableGridOptions } from '@vben/plugins/vxe-table';
import type { Recordable } from '@vben/types';

import type { ComponentType } from './component';

import { h } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $te } from '@vben/locales';
import {
  setupVbenVxeTable,
  useVbenVxeGrid as useGrid,
} from '@vben/plugins/vxe-table';
import { get, isFunction, isString } from '@vben/utils';

import { objectOmit } from '@vueuse/core';
import {
  ElButton,
  ElImage,
  ElMessage,
  ElPopconfirm,
  ElSwitch,
  ElTag,
} from 'element-plus';

import { $t } from '#/locales';

import { useVbenForm } from './form';

setupVbenVxeTable({
  configVxeTable: (vxeUI) => {
    vxeUI.setConfig({
      grid: {
        align: 'center',
        border: false,
        columnConfig: {
          resizable: true,
        },

        formConfig: {
          // 全局禁用vxe-table的表单配置，使用formOptions
          enabled: false,
        },
        minHeight: 180,
        proxyConfig: {
          autoLoad: true,
          response: {
            result: 'list',
            total: 'total',
          },
          showActiveMsg: true,
          showResponseMsg: false,
        },
        round: true,
        showOverflow: true,
        size: 'small',
      } as VxeTableGridOptions,
    });

    /**
     * 解决vxeTable在热更新时可能会出错的问题
     */
    vxeUI.renderer.forEach((_item, key) => {
      if (key.startsWith('Cell')) {
        vxeUI.renderer.delete(key);
      }
    });

    // 表格配置项可以用 cellRender: { name: 'CellImage' },
    vxeUI.renderer.add('CellImage', {
      renderTableDefault(renderOpts, params) {
        const { props } = renderOpts;
        const { column, row } = params;
        const src = row[column.field];
        return h(ElImage, { src, previewSrcList: [src], ...props });
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellLink' },
    vxeUI.renderer.add('CellLink', {
      renderTableDefault(renderOpts) {
        const { props } = renderOpts;
        return h(
          ElButton,
          { size: 'small', link: true },
          { default: () => props?.text },
        );
      },
    });

    // 单元格渲染： Tag
    vxeUI.renderer.add('CellTag', {
      renderTableDefault({ options, props }, { column, row }) {
        const value = get(row, column.field);
        const tagOptions = options ?? [
          {
            type: 'primary',
            label: $t('common.enabled'),
            value: 1,
          },
          {
            type: 'danger',
            label: $t('common.disabled'),
            value: 0,
          },
        ];
        const tagItem = tagOptions.find((item) => item.value === value);

        // Element Plus ElTag:
        // - type 属性：只有 'primary' 一个枚举值，用于设置主题类型
        // - color 属性：可以是任意颜色值（success, danger, warning, info 等）
        // 从 options 中提取 color，但保留 props 中可能传递的 type
        return h(
          ElTag,
          {
            ...props,
            color: tagItem?.color,
            ...objectOmit(tagItem ?? {}, ['label', 'color']),
          },
          { default: () => tagItem?.label ?? value },
        );
      },
    });

    // 单元格渲染： Switch
    vxeUI.renderer.add('CellSwitch', {
      renderTableDefault({ attrs, props }, { column, row }) {
        const loadingKey = `__loading_${column.field}`;
        const finallyProps = {
          activeText: $t('common.enabled'),
          activeValue: 1,
          inactiveText: $t('common.disabled'),
          inactiveValue: 0,
          inlinePrompt: true,
          ...props,
          modelValue: row[column.field],
          loading: row[loadingKey] ?? false,
          'onUpdate:modelValue': onChange,
        };
        async function onChange(newVal: any) {
          row[loadingKey] = true;
          try {
            const result = await attrs?.beforeChange?.(newVal, row);
            if (result !== false) {
              row[column.field] = newVal;
            }
          } finally {
            row[loadingKey] = false;
          }
        }
        return h(ElSwitch, finallyProps);
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellCopy' },
    vxeUI.renderer.add('CellCopy', {
      renderTableDefault({ props }, { column, row }) {
        const value = get(row, column.field);
        if (!value) {
          return h('span', { class: 'text-gray-400' }, '-');
        }
        // Element Plus 没有内置的复制组件，使用原生实现
        const handleCopy = (e: Event) => {
          e.stopPropagation(); // 阻止事件冒泡
          const text = String(value);
          if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(() => {
              ElMessage.success($t('common.copySuccess') || '复制成功');
            });
          } else {
            // 降级方案：使用 document.execCommand
            const textarea = document.createElement('textarea');
            textarea.value = text;
            textarea.style.position = 'fixed';
            textarea.style.opacity = '0';
            document.body.append(textarea);
            textarea.select();
            try {
              document.execCommand('copy');
              ElMessage.success($t('common.copySuccess') || '复制成功');
            } catch {
              ElMessage.error('复制失败');
            }
            textarea.remove();
          }
        };
        return h(
          'span',
          {
            class: 'flex items-center gap-1 cursor-pointer hover:text-blue-500',
            onClick: handleCopy,
            title: '点击复制',
            ...props,
          },
          [
            h('span', { class: 'flex-1 truncate' }, String(value)),
            h(IconifyIcon, {
              icon: 'lucide:copy',
              class: 'size-4 flex-shrink-0 text-gray-400 hover:text-blue-500',
              onClick: handleCopy, // 图标也绑定点击事件，确保阻止冒泡
            }),
          ],
        );
      },
    });

    /**
     * 注册表格的操作按钮渲染器
     */
    vxeUI.renderer.add('CellOperation', {
      renderTableDefault({ attrs, options, props }, { column, row }) {
        const defaultProps = {
          size: 'small',
          link: true,
          type: 'primary',
          ...props,
        };
        let align = 'end';
        switch (column.align) {
          case 'center': {
            align = 'center';
            break;
          }
          case 'left': {
            align = 'start';
            break;
          }
          default: {
            align = 'end';
            break;
          }
        }
        const presets: Recordable<Recordable<any>> = {
          delete: {
            type: 'danger',
            text: $t('common.delete'),
          },
          edit: {
            type: 'primary',
            text: $t('common.edit'),
          },
        };
        const operations: Array<Recordable<any>> = (
          options || ['edit', 'delete']
        )
          .map((opt) => {
            if (isString(opt)) {
              return presets[opt]
                ? { code: opt, ...defaultProps, ...presets[opt] }
                : {
                    code: opt,
                    text: $te(`common.${opt}`) ? $t(`common.${opt}`) : opt,
                    ...defaultProps,
                  };
            } else {
              return { ...defaultProps, ...presets[opt.code], ...opt };
            }
          })
          .map((opt) => {
            const optBtn: Recordable<any> = {};
            Object.keys(opt).forEach((key) => {
              optBtn[key] = isFunction(opt[key]) ? opt[key](row) : opt[key];
            });
            return optBtn;
          })
          .filter((opt) => opt.show !== false);

        function renderBtn(opt: Recordable<any>, listen = true) {
          // 提取 text 字段，不传递给 ElButton
          const { text, icon, code, show: _show, ...buttonProps } = opt;
          return h(
            ElButton,
            {
              ...props,
              ...buttonProps,
              onClick: listen
                ? () =>
                    attrs?.onClick?.({
                      code,
                      row,
                    })
                : undefined,
            },
            {
              default: () => {
                const content = [];
                if (icon) {
                  content.push(h(IconifyIcon, { class: 'size-5', icon }));
                }
                content.push(text);
                return content;
              },
            },
          );
        }

        function renderConfirm(opt: Recordable<any>) {
          let viewportWrapper: HTMLElement | null = null;
          // 过滤掉不应该传递给 ElPopconfirm 的属性
          const {
            text: _text,
            icon: _icon,
            code,
            show: _show,
            type: _type,
            link: _link,
            size: _size,
            ...popconfirmProps
          } = opt;
          return h(
            ElPopconfirm,
            {
              /**
               * 当popconfirm用在固定列中时，将固定列作为弹窗的容器时可能会因为固定列较窄而无法容纳弹窗
               * 将表格主体区域作为弹窗容器时又会因为固定列的层级较高而遮挡弹窗
               * 将body或者表格视口区域作为弹窗容器时又会导致弹窗无法跟随表格滚动。
               * 鉴于以上各种情况，一种折中的解决方案是弹出层展示时，禁止操作表格的滚动条。
               * 这样既解决了弹窗的遮挡问题，又不至于让弹窗随着表格的滚动而跑出视口区域。
               */
              teleported: true,
              placement: 'top-start',
              title: $t('ui.actionMessage.deleteConfirm', [
                row[attrs?.nameField || 'name'],
              ]),
              ...props,
              ...popconfirmProps,
              onShow: () => {
                // 当弹窗打开时，禁止表格的滚动
                viewportWrapper = document.querySelector(
                  '.vxe-table--viewport-wrapper',
                );
                viewportWrapper?.style.setProperty('pointer-events', 'none');
              },
              onHide: () => {
                viewportWrapper?.style.removeProperty('pointer-events');
              },
              onConfirm: () => {
                attrs?.onClick?.({
                  code,
                  row,
                });
              },
            },
            {
              reference: () => renderBtn(opt, false),
            },
          );
        }

        const btns = operations.map((opt) =>
          opt.code === 'delete' ? renderConfirm(opt) : renderBtn(opt),
        );
        return h(
          'div',
          {
            class: 'flex table-operations',
            style: { justifyContent: align },
          },
          btns,
        );
      },
    });

    // 这里可以自行扩展 vxe-table 的全局配置，比如自定义格式化
    // vxeUI.formats.add
  },
  useVbenForm,
});

export const useVbenVxeGrid = <T extends Record<string, any>>(
  ...rest: Parameters<typeof useGrid<T, ComponentType>>
) => useGrid<T, ComponentType>(...rest);

export type OnActionClickParams<T = Recordable<any>> = {
  code: string;
  row: T;
};
export type OnActionClickFn<T = Recordable<any>> = (
  params: OnActionClickParams<T>,
) => void;
export type * from '@vben/plugins/vxe-table';
