import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemUserApi {
  export interface SystemUser {
    [key: string]: any;
    id: string;
    username: string;
    realName?: string;
    nickname?: string;
    phone?: string;
    gender?: 0 | 1 | 2; // 0-未知，1-男，2-女
    employeeNo?: string;
    deptId?: string;
    deptName?: string;
    roles?: string[];
    roleNames?: string[];
    status: 0 | 1;
    createBy?: string;
    createByName?: string;
    createTime?: string;
    updateBy?: string;
    updateByName?: string;
    updateTime?: string;
  }
}

/**
 * 获取用户列表数据
 */
async function getUserList(params: Recordable<any>) {
  return requestClient.get<{
    list: SystemUserApi.SystemUser[];
    total: number;
  }>('/system/user', { params });
}

/**
 * 获取用户详情
 * @param id 用户ID
 */
async function getUserById(id: string) {
  return requestClient.get<SystemUserApi.SystemUser>(`/system/user/${id}`);
}

/**
 * 创建用户
 * @param data 用户数据
 */
async function createUser(
  data: Omit<SystemUserApi.SystemUser, 'createTime' | 'id' | 'updateTime'>,
) {
  return requestClient.post('/system/user', data);
}

/**
 * 更新用户
 *
 * @param id 用户ID
 * @param data 用户数据
 */
async function updateUser(
  id: string,
  data: Partial<
    Omit<SystemUserApi.SystemUser, 'createTime' | 'id' | 'updateTime'>
  >,
) {
  return requestClient.put(`/system/user/${id}`, data);
}

/**
 * 删除用户
 * @param id 用户ID
 */
async function deleteUser(id: string) {
  return requestClient.delete(`/system/user/${id}`);
}

/**
 * 更新用户状态
 * @param id 用户ID
 * @param status 状态（0-禁用，1-启用）
 */
async function updateUserStatus(id: string, status: 0 | 1) {
  return requestClient.put(`/system/user/${id}/status`, { status });
}

/**
 * 重置用户密码
 * @param id 用户ID
 * @param password 新密码
 */
async function resetUserPassword(id: string, password: string) {
  return requestClient.put(`/system/user/${id}/password`, { password });
}

/**
 * 获取用户选项列表（用于下拉选项，支持分页或 limit 限制）
 * @param params 查询参数（支持条件查询、分页或 limit 限制）
 * @returns 返回完整的用户对象列表，前端自行处理 label 和 value
 */
async function getUserOptions(params?: {
  deptId?: string;
  endTime?: string;
  limit?: number;
  page?: number;
  pageSize?: number;
  realName?: string;
  search?: string;
  startTime?: string;
  status?: number;
  username?: string;
}) {
  return requestClient.get<{
    list: SystemUserApi.SystemUser[];
    total: number;
  }>('/system/user/options', {
    params,
  });
}

export {
  createUser,
  deleteUser,
  getUserById,
  getUserList,
  getUserOptions,
  resetUserPassword,
  updateUser,
  updateUserStatus,
};
