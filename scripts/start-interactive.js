#!/usr/bin/env node

/**
 * 交互式启动脚本
 * 支持选择启动模式，更友好的用户体验
 * 跨平台支持：Windows/macOS/Linux
 * 
 * 使用方式：
 *   node scripts/start-interactive.js [all|frontend|backend|ele|docs]
 */

import { log } from './utils/logger.js';
import { startService, registerCleanupHandlers, waitForAllProcesses } from './utils/process-manager.js';
import { checkDir, checkFrontendDependencies } from './utils/validator.js';
import { backendDir, frontendDir } from './utils/paths.js';
import { showInteractiveMenu } from './utils/interactive-menu.js';
import { killPort } from './utils/port-killer.js';
import { waitForService, showServiceAddresses } from './utils/service-waiter.js';
import { PORTS, getAntdPort, getElePort, getDocsPort } from './utils/ports.js';

// 注册清理函数
registerCleanupHandlers();

// 根据模式启动
async function startByMode(mode) {
  try {
    if (mode === 'all') {
      if (!checkDir(backendDir, '后端') || !checkDir(frontendDir, '前端')) {
        process.exit(1);
      }
      
      // 清理端口
      await killPort(PORTS.BACKEND, '后端');
      await killPort(getAntdPort(), '前端');
      
      await startService('后端服务', 'pnpm run dev', backendDir);
      
      // 等待后端启动
      await waitForService(3000, '后端服务');
      
      // 检查前端依赖
      if (!checkFrontendDependencies(frontendDir)) {
        log('\n❌ 前端依赖未安装！', 'red');
        log('   请先运行: ./install 或 node scripts/cli.js install', 'yellow');
        log('   然后重新启动前端服务', 'yellow');
        showServiceAddresses({ hasBackend: true, hasFrontend: false });
        return;
      }
      
      await startService('前端服务', 'pnpm -F @vben/web-antd run dev', frontendDir);
      
      // 等待前端启动
      await waitForService(3000, '前端服务');
      
      // 统一显示所有地址
      showServiceAddresses({ hasBackend: true, hasFrontend: true, frontendType: 'antd' });
      
    } else if (mode === 'frontend') {
      if (!checkDir(frontendDir, '前端')) {
        process.exit(1);
      }
      
      // 检查前端依赖
      if (!checkFrontendDependencies(frontendDir)) {
        log('\n❌ 前端依赖未安装！', 'red');
        log('   请先运行: ./install 或 node scripts/cli.js install', 'yellow');
        log('   然后重新启动前端服务', 'yellow');
        process.exit(1);
      }
      
      // 清理端口
      await killPort(getAntdPort(), '前端');
      
      await startService('前端服务', 'pnpm -F @vben/web-antd run dev', frontendDir);
      
      // 等待前端启动
      await waitForService(3000, '前端服务');
      
      // 统一显示地址
      showServiceAddresses({ hasBackend: false, hasFrontend: true, frontendType: 'antd' });
      
    } else if (mode === 'backend') {
      if (!checkDir(backendDir, '后端')) {
        process.exit(1);
      }
      
      // 清理端口
      await killPort(PORTS.BACKEND, '后端');
      
      await startService('后端服务', 'pnpm run dev', backendDir);
      
      // 等待后端启动
      await waitForService(5000, '后端服务');
      
      // 统一显示地址
      showServiceAddresses({ hasBackend: true, hasFrontend: false });
      
    } else if (mode === 'ele') {
      if (!checkDir(frontendDir, '前端')) {
        process.exit(1);
      }
      
      // 清理端口
      await killPort(getElePort(), '前端');
      
      await startService('前端服务 (Element Plus)', 'pnpm -F @vben/web-ele run dev', frontendDir);
      
      // 等待前端启动
      await waitForService(3000, '前端服务');
      
      // 统一显示地址
      showServiceAddresses({ hasBackend: false, hasFrontend: true, frontendType: 'ele' });
      
    } else if (mode === 'docs') {
      if (!checkDir(frontendDir, '前端')) {
        process.exit(1);
      }
      
      // 清理端口
      await killPort(getDocsPort(), '文档站点');
      
      await startService('文档站点', 'pnpm -F @vben/docs run dev', frontendDir);
      
      // 等待文档站点启动
      await waitForService(3000, '文档站点');
      
      // 统一显示地址
      showServiceAddresses({ hasBackend: false, hasFrontend: true, frontendType: 'docs' });
    }

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

// 主函数
async function main() {
  const args = process.argv.slice(2);
  
  // 如果提供了参数，直接使用（兼容原有方式）
  if (args.length > 0) {
    const mode = args[0];
    await startByMode(mode);
    return;
  }

  // 交互式选择菜单
  const menuOptions = [
    { label: '启动全部（前端Ant Design Vue 版本 + 后端）', value: 'all', color: 'green' },
    { label: '仅启动前端（Ant Design Vue 版本）', value: 'frontend', color: 'blue' },
    { label: '仅启动后端', value: 'backend', color: 'yellow' },
    { label: '启动前端（Element Plus 版本）', value: 'ele', color: 'magenta' },
    { label: '启动文档站点', value: 'docs', color: 'cyan' },
    { label: '退出', value: 'exit', color: 'red' },
  ];

  try {
    const selectedValue = await showInteractiveMenu(menuOptions);
    
    if (selectedValue === 'exit') {
      log('\n👋 再见！\n', 'cyan');
      process.exit(0);
    }
    
    await startByMode(selectedValue);
  } catch (error) {
    log(`\n❌ 选择失败: ${error.message}`, 'red');
    process.exit(1);
  }
}

// 运行
main().catch((error) => {
  log(`\n❌ 发生错误: ${error.message}`, 'red');
  process.exit(1);
});
