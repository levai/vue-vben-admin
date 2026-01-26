/**
 * 验证工具
 * 提供目录和命令验证功能
 */

import { existsSync } from 'fs';
import { execSync } from 'child_process';
import { join } from 'path';
import { log } from './logger.js';

/**
 * 检查目录是否存在
 * @param {string} dir - 目录路径
 * @param {string} name - 目录名称（用于错误提示）
 * @returns {boolean} 目录是否存在
 */
export function checkDir(dir, name) {
  if (!existsSync(dir)) {
    log(`❌ ${name} 目录不存在: ${dir}`, 'red');
    return false;
  }
  return true;
}

/**
 * 检查命令是否存在
 * @param {string} command - 命令名称
 * @returns {boolean} 命令是否存在
 */
export function checkCommand(command) {
  try {
    execSync(`which ${command}`, { stdio: 'ignore' });
    return true;
  } catch {
    try {
      execSync(`where ${command}`, { stdio: 'ignore', shell: true });
      return true;
    } catch {
      return false;
    }
  }
}

/**
 * 检查前端依赖是否已安装
 * @param {string} frontendDir - 前端目录路径
 * @returns {boolean} 依赖是否已安装
 */
export function checkFrontendDependencies(frontendDir) {
  // 检查 node_modules 是否存在
  const nodeModulesDir = join(frontendDir, 'node_modules');
  if (!existsSync(nodeModulesDir)) {
    return false;
  }
  
  // 检查 vite 命令是否存在
  const vitePath = join(nodeModulesDir, '.bin', 'vite');
  return existsSync(vitePath);
}
