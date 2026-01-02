import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

import { z } from '#/adapter/form';
import { SYSTEM_PERMISSION_CODES } from '#/constants/permission-codes';
import { usePermissions } from '#/hooks/use-permissions';
import { $t } from '#/locales';

export function useFormSchema(isEdit = false): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      componentProps: {
        placeholder: '4-20个字符，小写字母开头，可包含小写字母、数字、下划线',
      },
      fieldName: 'username',
      label: $t('system.user.username'),
      rules: z
        .string()
        .min(4, { message: '用户名长度不能少于4个字符' })
        .max(20, { message: '用户名长度不能超过20个字符' })
        .regex(/^[a-z][a-z0-9_]{3,19}$/, {
          message: '用户名必须以小写字母开头，只能包含小写字母、数字和下划线',
        }),
    },
    {
      component: 'InputPassword',
      fieldName: 'password',
      label: $t('system.user.password'),
      rules: undefined,
      componentProps: {
        placeholder: isEdit
          ? '留空则不修改密码'
          : '留空则使用默认密码（88888888）',
      },
    },
    {
      component: 'Input',
      fieldName: 'realName',
      label: $t('system.user.realName'),
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'nickname',
      label: $t('system.user.nickname'),
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'phone',
      label: $t('system.user.phone'),
      rules: z
        .string()
        .min(1, { message: '手机号不能为空' })
        .refine((v) => /^1[3-9]\d{9}$/.test(v), {
          message: '手机号格式不正确',
        }),
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: $t('system.user.genderUnknown'), value: 0 },
          { label: $t('system.user.genderMale'), value: 1 },
          { label: $t('system.user.genderFemale'), value: 2 },
        ],
        optionType: 'button',
      },
      defaultValue: 0,
      fieldName: 'gender',
      label: $t('system.user.gender'),
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'employeeNo',
      label: $t('system.user.employeeNo'),
      rules: 'required',
    },
    {
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: false,
        api: () => import('#/api/system/dept').then((m) => m.getDeptList()),
        class: 'w-full',
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
      },
      fieldName: 'deptId',
      label: $t('system.user.dept'),
      rules: 'required',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: false,
        class: 'w-full',
        multiple: true,
        options: [],
      },
      fieldName: 'roleIds',
      label: $t('system.user.setRoles'),
      rules: z.array(z.string()).min(1, { message: '至少需要分配一个角色' }),
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
        optionType: 'button',
      },
      defaultValue: 1,
      fieldName: 'status',
      label: $t('system.user.status'),
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'username',
      label: $t('system.user.username'),
    },
    {
      component: 'Input',
      fieldName: 'realName',
      label: $t('system.user.realName'),
    },
    {
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: () => import('#/api/system/dept').then((m) => m.getDeptList()),
        class: 'w-full',
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
      },
      fieldName: 'deptId',
      label: $t('system.user.dept'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
      },
      fieldName: 'status',
      label: $t('system.user.status'),
    },
    {
      component: 'RangePicker',
      fieldName: 'createTime',
      label: $t('system.user.createTime'),
    },
  ];
}

export function useColumns<T = SystemUserApi.SystemUser>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (newStatus: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  const { hasPermission } = usePermissions(SYSTEM_PERMISSION_CODES.USER);

  // 定义操作按钮配置及对应的权限检查方法
  const operationButtons = [
    {
      code: 'edit',
      text: $t('common.edit'),
      hasAccess: hasPermission.EDIT,
    },
    {
      code: 'resetPassword',
      text: $t('system.user.resetPassword'),
      hasAccess: hasPermission.EDIT, // 重置密码使用编辑权限
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
      field: 'username',
      minWidth: 120,
      title: $t('system.user.username'),
    },
    {
      field: 'realName',
      minWidth: 100,
      title: $t('system.user.realName'),
    },
    {
      field: 'nickname',
      minWidth: 100,
      title: $t('system.user.nickname'),
    },
    {
      field: 'phone',
      minWidth: 120,
      title: $t('system.user.phone'),
    },
    {
      cellRender: {
        name: 'CellTag',
        options: [
          { label: $t('system.user.genderUnknown'), value: 0 },
          { label: $t('system.user.genderMale'), value: 1, color: 'primary' },
          { label: $t('system.user.genderFemale'), value: 2, color: 'danger' },
        ],
      },
      field: 'gender',
      minWidth: 100,
      title: $t('system.user.gender'),
    },
    {
      field: 'deptName',
      minWidth: 120,
      title: $t('system.user.dept'),
    },
    {
      field: 'createByName',
      minWidth: 100,
      title: $t('system.user.createBy'),
    },
    {
      cellRender: {
        attrs: { beforeChange: onStatusChange },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
      },
      field: 'status',
      minWidth: 100,
      title: $t('system.user.status'),
    },
    {
      field: 'createTime',
      minWidth: 180,
      title: $t('system.user.createTime'),
    },
    {
      field: 'updateByName',
      minWidth: 100,
      title: $t('system.user.updateBy'),
    },
    {
      field: 'updateTime',
      minWidth: 180,
      title: $t('system.user.updateTime'),
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'username',
          nameTitle: $t('system.user.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filteredOperations,
      },
      field: 'operation',
      fixed: 'right',
      minWidth: 180,
      title: $t('system.user.operation'),
    },
  ];
}
