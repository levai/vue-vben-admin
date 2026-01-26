#!/usr/bin/env node

/**
 * 启动脚本（命令行模式）
 * 支持通过命令行参数指定启动模式
 * 跨平台支持：Windows/macOS/Linux
 * 
 * 使用方式：
 *   node scripts/start.js [all|frontend|backend]
 */

import { log } from './utils/logger.js';
import { startService, registerCleanupHandlers, waitForAllProcesses } from './utils/process-manager.js';
import { checkDir, checkFrontendDependencies } from './utils/validator.js';
import { backendDir, frontendDir } from './utils/paths.js';
import { killPort } from './utils/port-killer.js';
import { waitForService, showServiceAddresses } from './utils/service-waiter.js';
import { PORTS, getAntdPort } from './utils/ports.js';

// 注册清理函数
registerCleanupHandlers();

// 主函数
async function main() {
  const args = process.argv.slice(2);
  const mode = args[0] || 'all'; // all, frontend, backend

  // 验证模式参数
  const validModes = ['all', 'frontend', 'backend'];
  if (!validModes.includes(mode)) {
    log(`❌ 无效的模式: ${mode}`, 'red');
    log(`   支持的模式: ${validModes.join(', ')}`, 'yellow');
    process.exit(1);
  }

  log('\n═══════════════════════════════════════', 'bright');
  log('   Vue Vben Admin 一键启动脚本', 'bright');
  log('═══════════════════════════════════════\n', 'bright');

  // 检查目录
  if (mode === 'all' || mode === 'backend') {
    if (!checkDir(backendDir, '后端')) {
      process.exit(1);
    }
  }

  if (mode === 'all' || mode === 'frontend') {
    if (!checkDir(frontendDir, '前端')) {
      process.exit(1);
    }
  }

  try {
    // 清理端口
    if (mode === 'all') {
      await killPort(PORTS.BACKEND, '后端');
      await killPort(getAntdPort(), '前端');
    } else if (mode === 'backend') {
      await killPort(PORTS.BACKEND, '后端');
    } else if (mode === 'frontend') {
      await killPort(getAntdPort(), '前端');
    }

    // 启动后端
    if (mode === 'all' || mode === 'backend') {
      await startService('后端服务', 'pnpm run dev', backendDir);
      // 等待后端启动
      await waitForService(mode === 'all' ? 3000 : 5000, '后端服务');
    }

    // 启动前端
    if (mode === 'all' || mode === 'frontend') {
      // 检查前端依赖
      if (!checkFrontendDependencies(frontendDir)) {
        log('\n❌ 前端依赖未安装！', 'red');
        log('   请先运行: ./install 或 node scripts/cli.js install', 'yellow');
        log('   然后重新启动前端服务', 'yellow');
        if (mode === 'all') {
          showServiceAddresses({ hasBackend: true, hasFrontend: false });
        }
        process.exit(1);
      }

      await startService('前端服务', 'pnpm -F @vben/web-antd run dev', frontendDir);
      // 等待前端启动
      await waitForService(3000, '前端服务');
    }

    // 统一显示所有地址
    showServiceAddresses({
      hasBackend: mode === 'all' || mode === 'backend',
      hasFrontend: mode === 'all' || mode === 'frontend',
      frontendType: 'antd',
    });

    // 等待所有进程退出
    await waitForAllProcesses();
  } catch (error) {
    log(`\n❌ 启动失败: ${error.message}`, 'red');
    if (error.stack) {
      log(`   详细错误: ${error.stack}`, 'red');
    }
    process.exit(1);
  }
}

// 运行
main().catch((error) => {
  log(`\n❌ 发生错误: ${error.message}`, 'red');
  process.exit(1);
});
