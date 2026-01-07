import { requestClient } from '#/api/request';

export namespace SystemOperationLogApi {
  export interface SystemOperationLog {
    [key: string]: any;
    /** 日志ID */
    id: string;
    /** 用户ID */
    userId: string;
    /** 用户名 */
    username: string;
    /** 真实姓名 */
    realName?: string;
    /** 操作类型 */
    operationType: string;
    /** 操作模块 */
    operationModule?: string;
    /** 操作页面路径 */
    operationPage?: string;
    /** 操作标题/描述 */
    operationTitle?: string;
    /** 请求方法 */
    requestMethod?: string;
    /** 请求URL */
    requestUrl?: string;
    /** 请求参数（JSON格式） */
    requestParams?: string;
    /** 响应状态码 */
    responseCode?: number;
    /** 响应数据（JSON格式） */
    responseData?: string;
    /** IP地址 */
    ipAddress?: string;
    /** 用户代理 */
    userAgent?: string;
    /** 浏览器类型 */
    browser?: string;
    /** 操作系统 */
    os?: string;
    /** 操作耗时（毫秒） */
    duration?: number;
    /** 状态：0-失败，1-成功 */
    status: number;
    /** 错误信息 */
    errorMessage?: string;
    /** 创建时间 */
    createTime?: string;
  }

  export interface OperationLogQueryParams {
    page?: number;
    pageSize?: number;
    userId?: string;
    username?: string;
    operationType?: string;
    operationModule?: string;
    status?: number;
    startTime?: string;
    endTime?: string;
    search?: string;
  }
}

/**
 * 获取操作日志列表
 */
async function getOperationLogList(
  params?: SystemOperationLogApi.OperationLogQueryParams,
) {
  return requestClient.get<{
    list: SystemOperationLogApi.SystemOperationLog[];
    total: number;
  }>('/system/operation-log', {
    params,
  });
}

/**
 * 获取操作日志详情
 * @param id 日志ID
 */
async function getOperationLogDetail(id: string) {
  return requestClient.get<SystemOperationLogApi.SystemOperationLog>(
    `/system/operation-log/${id}`,
  );
}

/**
 * 删除操作日志
 * @param id 日志ID
 */
async function deleteOperationLog(id: string) {
  return requestClient.delete(`/system/operation-log/${id}`);
}

/**
 * 批量删除操作日志
 * @param ids 日志ID列表
 */
async function batchDeleteOperationLog(ids: string[]) {
  return requestClient.delete('/system/operation-log/batch', {
    data: ids,
  });
}

/**
 * 选项接口（支持树形结构）
 */
export interface Option {
  label: string;
  value: string;
  children?: Option[];
}

/**
 * 获取操作模块列表（返回树形结构）
 * @param params 查询参数（仅支持 search）
 */
async function getOperationModuleList(params?: { search?: string }) {
  return requestClient.get<{
    list: Option[];
    total: number;
  }>('/system/operation-log/modules', {
    params,
  });
}

/**
 * 获取操作类型列表（返回全部数据）
 * @param params 查询参数（仅支持 search）
 */
async function getOperationTypeList(params?: { search?: string }) {
  return requestClient.get<{
    list: Option[];
    total: number;
  }>('/system/operation-log/types', {
    params,
  });
}

export {
  batchDeleteOperationLog,
  deleteOperationLog,
  getOperationLogDetail,
  getOperationLogList,
  getOperationModuleList,
  getOperationTypeList,
};
