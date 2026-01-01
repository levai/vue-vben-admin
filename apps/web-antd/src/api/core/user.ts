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
