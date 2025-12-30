import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemUserApi {
  export interface SystemUser {
    [key: string]: any;
    id: string;
    username: string;
    realName?: string;
    deptId?: string;
    deptName?: string;
    roles?: string[];
    roleNames?: string[];
    status: 0 | 1;
    createTime?: string;
    updateTime?: string;
  }
}

/**
 * 获取用户列表数据
 */
async function getUserList(params: Recordable<any>) {
  return requestClient.get<{
    items: SystemUserApi.SystemUser[];
    total: number;
  }>('/system/user/list', { params });
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

export {
  createUser,
  deleteUser,
  getUserById,
  getUserList,
  resetUserPassword,
  updateUser,
  updateUserStatus,
};
