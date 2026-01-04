import type { VxeTableGridOptions } from '@vben/plugins/vxe-table';

import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn } from '#/adapter/vxe-table';
import type { SystemOperationLogApi } from '#/api/system/operation-log';

import {
  getOperationModuleList,
  getOperationTypeList,
} from '#/api/system/operation-log';
import { getUserOptions } from '#/api/system/user';
import { SYSTEM_PERMISSION_CODES } from '#/constants/permission-codes';
import { usePermissions } from '#/hooks/use-permissions';
import { $t } from '#/locales';

/**
 * 获取搜索表单配置
 */
export function useSearchSchema(): VbenFormSchema[] {
  return [
    {
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        api: getUserOptions,
        labelField: 'username',
        resultField: 'list',
        valueField: 'id',
      },
      fieldName: 'userId',
      label: $t('system.operationLog.username'),
    },
    {
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        api: getOperationTypeList,
        resultField: 'list',
      },
      fieldName: 'operationType',
      label: $t('system.operationLog.operationType'),
    },
    {
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: getOperationModuleList,
        resultField: 'list',
        labelField: 'label',
        valueField: 'label',
        childrenField: 'children',
        showSearch: true,
        treeDefaultExpandAll: false,
        placeholder: $t('system.operationLog.operationModule.placeholder'),
      },
      fieldName: 'operationModule',
      label: $t('system.operationLog.operationModule.title'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('system.operationLog.status.success'), value: 1 },
          { label: $t('system.operationLog.status.failed'), value: 0 },
        ],
      },
      fieldName: 'status',
      label: $t('system.operationLog.status.title'),
    },
    {
      component: 'RangePicker',
      fieldName: 'createTime',
      label: $t('system.operationLog.dateRange'),
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: $t('system.operationLog.keywordPlaceholder'),
      },
      fieldName: 'search',
      label: $t('system.operationLog.keyword'),
    },
  ];
}

/**
 * 获取表格列配置
 */
export function useColumns(
  onActionClick: OnActionClickFn<SystemOperationLogApi.SystemOperationLog>,
): VxeTableGridOptions<SystemOperationLogApi.SystemOperationLog>['columns'] {
  const { hasPermission } = usePermissions(
    SYSTEM_PERMISSION_CODES.OPERATION_LOG,
  );

  const operationButtons = [
    {
      code: 'view',
      text: $t('common.view'),
      hasAccess: hasPermission.VIEW,
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
      type: 'seq',
      title: $t('common.seq'),
      width: 60,
      fixed: 'left',
    },
    {
      field: 'createTime',
      title: $t('system.operationLog.createTime'),
      width: 180,
      fixed: 'left',
    },

    {
      field: 'operationType',
      title: $t('system.operationLog.operationType'),
      width: 100,
      slots: { default: 'operationType' },
    },
    {
      field: 'operationModule',
      title: $t('system.operationLog.operationModule.title'),
      width: 100,
      slots: { default: 'operationModule' },
    },
    {
      field: 'requestMethod',
      title: $t('system.operationLog.requestMethod'),
      width: 100,
    },
    {
      field: 'requestUrl',
      title: $t('system.operationLog.requestUrl'),
      align: 'left',
      width: 250,
      showOverflow: 'tooltip',
    },
    {
      field: 'username',
      title: $t('system.operationLog.username'),
      width: 120,
    },
    {
      field: 'ipAddress',
      title: $t('system.operationLog.ipAddress'),
      width: 150,
    },
    {
      field: 'browser',
      title: $t('system.operationLog.browser'),
      width: 150,
      slots: { default: 'browser' },
    },
    {
      cellRender: {
        name: 'CellTag',
        options: [
          {
            label: $t('system.operationLog.status.success'),
            value: 1,
            color: 'success',
          },
          {
            label: $t('system.operationLog.status.failed'),
            value: 0,
            color: 'error',
          },
        ],
      },
      field: 'status',
      title: $t('system.operationLog.status.title'),
      width: 100,
    },
    {
      field: 'duration',
      title: $t('system.operationLog.duration'),
      align: 'left',
      width: 100,
      formatter: ({ row }) => {
        return row.duration ? `${row.duration}ms` : '-';
      },
    },
    {
      align: 'right',
      cellRender: {
        attrs: {
          nameField: 'id',
          nameTitle: $t('system.operationLog.id'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filteredOperations,
      },
      field: 'operation',
      fixed: 'right',
      headerAlign: 'center',
      showOverflow: false,
      title: $t('common.operation'),
      width: 100,
    },
  ];
}
