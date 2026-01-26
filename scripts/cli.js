#!/usr/bin/env node

/**
 * 统一命令行入口
 * 支持所有项目命令的统一入口
 * 
 * 使用方式：
 *   node scripts/cli.js dev [mode]           # 启动开发服务器
 *   node scripts/cli.js install [options]    # 安装依赖
 *   node scripts/cli.js help                 # 显示帮助
 */

import { spawn } from 'child_process';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { log } from './utils/logger.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const scriptsDir = __dirname;

// 显示帮助信息
function showHelp() {
  log('\n═══════════════════════════════════════', 'bright');
  log('   Vue Vben Admin 命令行工具', 'bright');
  log('═══════════════════════════════════════\n', 'bright');
  
  log('可用命令：\n', 'cyan');
  
  log('  dev [mode]', 'green');
  log('    启动开发服务器', 'reset');
  log('    模式:', 'yellow');
  log('      (无参数)    交互式选择启动模式', 'reset');
  log('      all         启动全部（前端 + 后端）', 'reset');
  log('      frontend    仅启动前端', 'reset');
  log('      backend     仅启动后端', 'reset');
  log('      ele         启动前端（Element Plus 版本）', 'reset');
  log('      docs        启动文档站点', 'reset');
  log('');
  
  log('  install [options]', 'green');
  log('    安装项目依赖', 'reset');
  log('    选项:', 'yellow');
  log('      --frontend-only    仅安装前端依赖', 'reset');
  log('');
  
  log('  help', 'green');
  log('    显示此帮助信息', 'reset');
  log('');
  
  log('示例：', 'cyan');
  log('  node scripts/cli.js dev', 'blue');
  log('  node scripts/cli.js dev all', 'blue');
  log('  node scripts/cli.js install', 'blue');
  log('  node scripts/cli.js install --frontend-only', 'blue');
  log('');
}

// 执行脚本
function execScript(scriptPath, args = []) {
  return new Promise((resolve, reject) => {
    const child = spawn('node', [scriptPath, ...args], {
      stdio: 'inherit',
      shell: true,
    });

    child.on('error', (error) => {
      reject(error);
    });

    child.on('exit', (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`脚本退出码: ${code}`));
      }
    });
  });
}

// 主函数
async function main() {
  const args = process.argv.slice(2);
  const command = args[0];
  const commandArgs = args.slice(1);

  if (!command || command === 'help' || command === '--help' || command === '-h') {
    showHelp();
    return;
  }

  try {
    switch (command) {
      case 'dev': {
        if (commandArgs.length === 0) {
          // 无参数：交互式模式
          await execScript(join(scriptsDir, 'start-interactive.js'));
        } else {
          const mode = commandArgs[0];
          if (['all', 'frontend', 'backend'].includes(mode)) {
            // 使用 start.js
            await execScript(join(scriptsDir, 'start.js'), [mode]);
          } else if (['ele', 'docs'].includes(mode)) {
            // 使用 start-interactive.js 的命令行模式
            await execScript(join(scriptsDir, 'start-interactive.js'), [mode]);
          } else {
            log(`❌ 无效的模式: ${mode}`, 'red');
            log('   支持的模式: all, frontend, backend, ele, docs', 'yellow');
            log('   使用 "node scripts/cli.js help" 查看帮助', 'yellow');
            process.exit(1);
          }
        }
        break;
      }

      case 'install': {
        await execScript(join(scriptsDir, 'install.js'), commandArgs);
        break;
      }

      default:
        log(`❌ 未知命令: ${command}`, 'red');
        log('   使用 "node scripts/cli.js help" 查看帮助', 'yellow');
        process.exit(1);
    }
  } catch (error) {
    log(`\n❌ 执行失败: ${error.message}`, 'red');
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
