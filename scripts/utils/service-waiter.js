/**
 * 服务等待工具
 * 等待服务启动完成后再显示地址
 */

import { log } from './logger.js';
import { getFrontendPort } from './ports.js';

/**
 * 等待服务启动完成
 * @param {number} delay - 等待时间（毫秒）
 * @param {string} serviceName - 服务名称
 */
export function waitForService(delay, serviceName) {
  return new Promise((resolve) => {
    log(`\n⏳ 等待 ${serviceName} 启动完成...`, 'yellow');
    setTimeout(() => {
      resolve();
    }, delay);
  });
}

/**
 * 显示服务地址信息
 * @param {Object} options - 地址选项
 * @param {boolean} options.hasBackend - 是否有后端服务
 * @param {boolean} options.hasFrontend - 是否有前端服务
 * @param {string} options.frontendType - 前端类型（'antd' | 'ele' | 'docs'）
 */
export function showServiceAddresses({ hasBackend, hasFrontend, frontendType = 'antd' }) {
  const frontendPort = getFrontendPort(frontendType);
  
  log('\n═══════════════════════════════════════', 'bright');
  log('✅ 所有服务已启动！', 'green');
  log('═══════════════════════════════════════\n', 'bright');
  
  log('📝 访问地址:', 'cyan');
  
  if (hasBackend) {
    log('   🔹 后端 API:     http://localhost:8080', 'blue');
    log('   🔹 API 文档:     http://localhost:8080/doc.html', 'blue');
  }
  
  if (hasFrontend) {
    const frontendLabels = {
      antd: '前端应用 (Ant Design Vue)',
      ele: '前端应用 (Element Plus)',
      docs: '文档站点',
    };
    log(`   🔹 ${frontendLabels[frontendType]}: http://localhost:${frontendPort}`, 'blue');
  }
  
  log('\n💡 提示：按 Ctrl+C 停止所有服务\n', 'yellow');
}
