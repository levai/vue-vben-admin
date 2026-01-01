import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemMenuApi } from '#/api/system/menu';

import { SYSTEM_PERMISSION_CODES } from '#/constants/permission-codes';
import { usePermissions } from '#/hooks/use-permissions';
import { $t } from '#/locales';

export function getMenuTypeOptions() {
  return [
    {
      color: 'processing',
      label: $t('system.menu.typeCatalog'),
      value: 'catalog',
    },
    { color: 'default', label: $t('system.menu.typeMenu'), value: 'menu' },
    { color: 'error', label: $t('system.menu.typeButton'), value: 'button' },
    {
      color: 'success',
      label: $t('system.menu.typeEmbedded'),
      value: 'embedded',
    },
    { color: 'warning', label: $t('system.menu.typeLink'), value: 'link' },
  ];
}

export function useColumns(
  onActionClick: OnActionClickFn<SystemMenuApi.SystemMenu>,
): VxeTableGridOptions<SystemMenuApi.SystemMenu>['columns'] {
  const { hasPermission } = usePermissions(SYSTEM_PERMISSION_CODES.MENU);

  // 定义操作按钮配置及对应的权限检查方法
  const operationButtons = [
    {
      code: 'append',
      text: '新增下级',
      // 新增下级使用新增权限
      hasAccess: hasPermission.ADD,
      // 按钮类型不允许新增下级
      disabled: (row: SystemMenuApi.SystemMenu) => {
        return row.type === 'button';
      },
    },
    {
      code: 'edit',
      text: $t('common.edit'),
      hasAccess: hasPermission.EDIT,
    },
    {
      code: 'delete',
      text: $t('common.delete'),
      hasAccess: hasPermission.DELETE,
    },
  ];

  // 根据权限过滤操作按钮
  const filteredOperations = operationButtons
    .filter((btn) => {
      // 如果没有配置权限检查方法，则默认显示
      if (!btn.hasAccess) {
        return true;
      }
      // 检查是否有对应权限
      return btn.hasAccess();
    })
    .map((btn) => {
      // 移除 hasAccess，只保留需要的属性
      const { hasAccess: _hasAccess, ...rest } = btn;
      return rest;
    });

  return [
    {
      align: 'center',
      fixed: 'left',
      dragSort: true,
      title: '',
      width: 50,
    },
    {
      align: 'left',
      field: 'meta.title',
      fixed: 'left',
      slots: { default: 'title' },
      title: $t('system.menu.menuTitle'),
      treeNode: true,
      width: 250,
    },
    {
      align: 'center',
      cellRender: { name: 'CellTag', options: getMenuTypeOptions() },
      field: 'type',
      title: $t('system.menu.type'),
      width: 100,
    },
    {
      field: 'authCode',
      title: $t('system.menu.authCode'),
      width: 200,
    },
    {
      align: 'left',
      field: 'path',
      title: $t('system.menu.path'),
      width: 200,
    },

    {
      align: 'left',
      field: 'component',
      formatter: ({ row }) => {
        switch (row.type) {
          case 'catalog':
          case 'menu': {
            return row.component ?? '';
          }
          case 'embedded': {
            return row.meta?.iframeSrc ?? '';
          }
          case 'link': {
            return row.meta?.link ?? '';
          }
        }
        return '';
      },
      minWidth: 200,
      title: $t('system.menu.component'),
    },
    {
      align: 'center',
      field: 'meta.order',
      title: $t('system.menu.order'),
      width: 100,
    },
    {
      cellRender: { name: 'CellTag' },
      field: 'status',
      title: $t('system.menu.status'),
      width: 100,
    },

    {
      align: 'right',
      cellRender: {
        attrs: {
          nameField: 'name',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filteredOperations,
      },
      field: 'operation',
      fixed: 'right',
      headerAlign: 'center',
      showOverflow: false,
      title: $t('system.menu.operation'),
      width: 200,
    },
  ];
}
