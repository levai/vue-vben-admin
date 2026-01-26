/**
 * 环境变量读取工具
 * 从 .env.development 文件中读取端口配置
 */

import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { frontendDir } from './paths.js';

/**
 * 读取 .env.development 文件中的端口配置
 * @param {string} appName - 应用名称 ('web-antd' | 'web-ele' | 'docs')
 * @returns {number} 端口号，如果未找到则返回默认值
 */
export function getPortFromEnv(appName) {
  const envFile = join(frontendDir, 'apps', appName, '.env.development');
  
  if (!existsSync(envFile)) {
    // 如果文件不存在，返回默认端口
    const defaultPorts = {
      'web-antd': 5666,
      'web-ele': 5777,
      'docs': 5173,
    };
    return defaultPorts[appName] || 5173;
  }

  try {
    const content = readFileSync(envFile, 'utf8');
    const lines = content.split('\n');
    
    for (const line of lines) {
      // 跳过注释和空行
      if (line.trim().startsWith('#') || !line.trim()) {
        continue;
      }
      
      // 查找 VITE_PORT 配置
      const match = line.match(/^VITE_PORT\s*=\s*(\d+)/);
      if (match) {
        return parseInt(match[1], 10);
      }
    }
  } catch (error) {
    console.warn(`无法读取 ${envFile}:`, error.message);
  }

  // 如果未找到，返回默认端口
  const defaultPorts = {
    'web-antd': 5666,
    'web-ele': 5777,
    'docs': 5173,
  };
  return defaultPorts[appName] || 5173;
}

/**
 * 根据前端类型获取端口
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
