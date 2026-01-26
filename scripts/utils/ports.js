/**
 * 端口配置
 * 统一管理各服务的端口号
 * 前端端口从 .env.development 文件中动态读取
 */

import { getPortFromEnv } from './env-reader.js';

export const PORTS = {
  BACKEND: 8080,
};

/**
 * 获取前端端口（从环境文件动态读取）
 * @param {string} frontendType - 前端类型 ('antd' | 'ele' | 'docs')
 * @returns {number} 端口号
 */
export function getFrontendPort(frontendType) {
  const appMap = {
    antd: 'web-antd',
    ele: 'web-ele',
    docs: 'docs',
  };
  
  const appName = appMap[frontendType] || 'web-antd';
  return getPortFromEnv(appName);
}

/**
 * 获取 Ant Design Vue 端口
 */
export function getAntdPort() {
  return getPortFromEnv('web-antd');
}

/**
 * 获取 Element Plus 端口
 */
export function getElePort() {
  return getPortFromEnv('web-ele');
}

/**
 * 获取文档站点端口
 */
export function getDocsPort() {
  return getPortFromEnv('docs');
}
