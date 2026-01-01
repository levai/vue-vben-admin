import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemRoleApi {
  export interface SystemRole {
    [key: string]: any;
    id: string;
    name: string;
    permissions: string[];
    remark?: string;
    status: 0 | 1;
    /** 显示标签（用于下拉选项显示） */
    label?: string;
    /** 选项值（用于下拉选项值） */
    value?: string;
  }
}

/**
 * 获取角色列表数据
 */
async function getRoleList(params: Recordable<any>) {
  return requestClient.get<Array<SystemRoleApi.SystemRole>>('/system/role', {
    params,
  });
}

/**
 * 创建角色
 * @param data 角色数据
 */
async function createRole(data: Omit<SystemRoleApi.SystemRole, 'id'>) {
  return requestClient.post('/system/role', data);
}

/**
 * 更新角色
 *
 * @param id 角色 ID
 * @param data 角色数据
 */
async function updateRole(
  id: string,
  data: Omit<SystemRoleApi.SystemRole, 'id'>,
) {
  return requestClient.put(`/system/role/${id}`, data);
}

/**
 * 删除角色
 * @param id 角色 ID
 */
async function deleteRole(id: string) {
  return requestClient.delete(`/system/role/${id}`);
}

/**
 * 获取角色选项列表（用于下拉选项，支持 limit 限制）
 * @param params 查询参数（支持条件查询和 limit 限制）
 */
async function getRoleOptions(params?: {
  endTime?: string;
  id?: string;
  limit?: number;
  name?: string;
  remark?: string;
  search?: string;
  startTime?: string;
  status?: number;
}) {
  return requestClient.get<{
    items: SystemRoleApi.SystemRole[];
    total: number;
  }>('/system/role/options', {
    params,
  });
}

export { createRole, deleteRole, getRoleList, getRoleOptions, updateRole };
