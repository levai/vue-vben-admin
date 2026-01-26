#!/usr/bin/env node

/**
 * 依赖安装脚本
 * 跨平台支持：Windows/macOS/Linux
 * 
 * 使用方式：
 *   node scripts/install.js [--frontend-only]
 */

import { spawn } from 'child_process';
import { log } from './utils/logger.js';
import { checkCommand } from './utils/validator.js';
import { backendDir, frontendDir } from './utils/paths.js';
import { existsSync } from 'fs';
import { join } from 'path';

// 执行命令
function execCommand(command, cwd, description) {
  return new Promise((resolve, reject) => {
    log(`\n📦 ${description}...`, 'cyan');
    log(`📁 工作目录: ${cwd}`, 'blue');
    log(`💻 执行命令: ${command}\n`, 'blue');

    const child = spawn(command, {
      cwd,
      shell: true,
      stdio: 'inherit',
    });

    child.on('error', (error) => {
      log(`❌ ${description}失败: ${error.message}`, 'red');
      reject(error);
    });

    child.on('exit', (code) => {
      if (code === 0) {
        log(`✅ ${description}完成`, 'green');
        resolve();
      } else {
        log(`❌ ${description}失败，退出码: ${code}`, 'red');
        reject(new Error(`${description}失败`));
      }
    });
  });
}

// 安装前端依赖
async function installFrontend() {
  if (!existsSync(join(frontendDir, 'package.json'))) {
    log('❌ frontend/package.json 不存在', 'red');
    throw new Error('前端目录不存在 package.json');
  }

  await execCommand('pnpm install', frontendDir, '安装前端依赖');
}

// 安装后端依赖
async function installBackend() {
  if (!existsSync(join(backendDir, 'pom.xml'))) {
    log('⚠️  backend/pom.xml 不存在，跳过后端依赖安装', 'yellow');
    return;
  }

  if (!checkCommand('mvn')) {
    log('⚠️  未检测到 Maven，跳过后端依赖安装', 'yellow');
    log('   提示: 后端依赖由 Maven 管理，如需安装请先安装 Maven', 'yellow');
    return;
  }

  await execCommand('mvn dependency:resolve -q', backendDir, '安装后端依赖');
}

// 主函数
async function main() {
  const args = process.argv.slice(2);
  const frontendOnly = args.includes('--frontend-only');

  log('\n═══════════════════════════════════════', 'bright');
  log('   Vue Vben Admin 依赖安装', 'bright');
  log('═══════════════════════════════════════\n', 'bright');

  // 检查必要工具
  if (!checkCommand('node')) {
    log('❌ Node.js 未安装，请先安装 Node.js >= 20.12.0', 'red');
    process.exit(1);
  }

  if (!checkCommand('pnpm')) {
    log('❌ pnpm 未安装，请先安装 pnpm >= 10.0.0', 'red');
    log('   安装命令: npm install -g pnpm', 'yellow');
    process.exit(1);
  }

  // 检查目录
  if (!existsSync(frontendDir)) {
    log('❌ frontend 目录不存在', 'red');
    process.exit(1);
  }

  try {
    // 安装前端依赖
    await installFrontend();

    // 安装后端依赖（如果不是仅前端模式）
    if (!frontendOnly) {
      await installBackend();
    }

    log('\n═══════════════════════════════════════', 'bright');
    log('✅ 所有依赖安装完成！', 'green');
    log('═══════════════════════════════════════\n', 'bright');
    
    log('📝 使用以下命令启动项目：', 'cyan');
    log('   - 一键启动: ./dev 或 node scripts/start-interactive.js', 'blue');
    log('   - 仅前端: node scripts/start.js frontend', 'blue');
    log('   - 仅后端: node scripts/start.js backend', 'blue');
    log('');
  } catch (error) {
    log(`\n❌ 安装失败: ${error.message}`, 'red');
    process.exit(1);
  }
}

// 运行
main().catch((error) => {
  log(`\n❌ 发生错误: ${error.message}`, 'red');
  process.exit(1);
});
