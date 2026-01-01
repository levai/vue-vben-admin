import type { UserInfo } from '@vben/types';

import { requestClient } from '#/api/request';

/**
 * 获取用户信息
 */
export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/user/info');
}

/**
 * 修改密码参数
 */
export interface ChangePasswordParams {
  /** 旧密码 */
  oldPassword: string;
  /** 新密码 */
  newPassword: string;
}

/**
 * 修改密码
 */
export async function changePasswordApi(params: ChangePasswordParams) {
  return requestClient.put<boolean>('/user/password', params);
}

/**
 * 更新用户基础信息参数
 */
export interface UpdateUserInfoParams {
  /** 真实姓名 */
  realName?: string;
  /** 昵称 */
  nickname?: string;
  /** 手机号 */
  phone?: string;
  /** 性别：0-未知，1-男，2-女 */
  gender?: number;
}

/**
 * 更新用户基础信息
 */
export async function updateUserInfoApi(params: UpdateUserInfoParams) {
  return requestClient.put<boolean>('/user/info', params);
}
