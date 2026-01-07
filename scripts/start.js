#!/usr/bin/env node

/**
 * 一键启动前后端服务
 * 支持同时启动或单独启动
 * 跨平台支持：Windows/macOS/Linux
 */

import { spawn } from 'child_process';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { existsSync } from 'fs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const rootDir = join(__dirname, '..');

const isWindows = process.platform === 'win32';

// 颜色输出
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  cyan: '\x1b[36m',
};

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

// 检查目录是否存在
function checkDir(dir, name) {
  if (!existsSync(dir)) {
    log(`❌ ${name} 目录不存在: ${dir}`, 'red');
    process.exit(1);
  }
}

// 存储所有子进程
const childProcesses = [];
let isCleaningUp = false;

// 启动服务
function startService(name, command, cwd, env = {}) {
  return new Promise((resolve, reject) => {
    log(`\n🚀 启动 ${name}...`, 'cyan');
    log(`📁 工作目录: ${cwd}`, 'blue');
    log(`💻 执行命令: ${command}\n`, 'blue');

    // 使用 shell 执行命令，支持跨平台
    const child = spawn(command, {
      cwd,
      shell: true,
      stdio: 'inherit',
      env: { ...process.env, ...env },
    });

    const processInfo = { name, process: child, exited: false };
    childProcesses.push(processInfo);

    child.on('error', (error) => {
      log(`❌ ${name} 启动失败: ${error.message}`, 'red');
      processInfo.exited = true;
      reject(error);
    });

    child.on('exit', (code) => {
      processInfo.exited = true;
      if (!isCleaningUp) {
        if (code !== 0 && code !== null) {
          log(`\n⚠️  ${name} 异常退出，退出码: ${code}`, 'yellow');
          // 如果某个进程异常退出，清理其他进程
          if (childProcesses.some((p) => !p.exited)) {
            log('   正在清理其他服务...', 'yellow');
            cleanup();
          }
        } else {
          log(`\n✅ ${name} 已停止`, 'green');
        }
      }
    });

    // 等待进程启动
    setTimeout(() => {
      if (!child.killed && child.pid) {
        resolve(child);
      }
    }, 500);
  });
}

// 清理所有进程
function cleanup() {
  if (isCleaningUp) return;
  isCleaningUp = true;

  log(`\n\n🛑 正在停止所有服务...`, 'yellow');
  
  const cleanupPromises = childProcesses.map(({ name, process: child }) => {
    return new Promise((resolve) => {
      if (!child || child.killed || !child.pid) {
        resolve();
        return;
      }

      try {
        log(`   停止 ${name}...`, 'yellow');
        
        if (isWindows) {
          // Windows 使用 taskkill 强制终止进程树
          const killProcess = spawn('taskkill', ['/pid', child.pid, '/f', '/t'], {
            stdio: 'ignore',
            shell: true,
          });
          killProcess.on('exit', () => resolve());
          killProcess.on('error', () => resolve());
        } else {
          // Unix 系统使用信号终止
          child.kill('SIGTERM');
          
          // 如果 3 秒后还没退出，强制终止
          setTimeout(() => {
            if (!child.killed) {
              child.kill('SIGKILL');
            }
            resolve();
          }, 3000);
          
          child.on('exit', () => resolve());
        }
      } catch (error) {
        resolve();
      }
    });
  });

  Promise.all(cleanupPromises).then(() => {
    setTimeout(() => process.exit(0), 500);
  });
}

// 注册清理函数
process.on('SIGINT', cleanup);
process.on('SIGTERM', cleanup);

// Windows 特殊处理：监听 Ctrl+C
if (isWindows && process.stdin.isTTY) {
  try {
    process.stdin.setRawMode(true);
    process.stdin.resume();
    process.stdin.setEncoding('utf8');
    process.stdin.on('data', (data) => {
      // Ctrl+C 的 ASCII 码是 3 (ETX)
      if (data === '\u0003' || data.charCodeAt(0) === 3) {
        cleanup();
      }
    });
  } catch (error) {
    // 如果设置 raw mode 失败，忽略错误
  }
}

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
  const backendDir = join(rootDir, 'backend');
  const frontendDir = join(rootDir, 'frontend');

  if (mode === 'all' || mode === 'backend') {
    checkDir(backendDir, '后端');
  }

  if (mode === 'all' || mode === 'frontend') {
    checkDir(frontendDir, '前端');
  }

  try {
    // 启动后端
    if (mode === 'all' || mode === 'backend') {
      await startService('后端服务', 'pnpm run dev', backendDir);
    }

    // 启动前端
    if (mode === 'all' || mode === 'frontend') {
      // 等待后端启动（如果同时启动）
      if (mode === 'all') {
        log('\n⏳ 等待 3 秒后启动前端...\n', 'yellow');
        await new Promise((resolve) => setTimeout(resolve, 3000));
      }

      await startService(
        '前端服务',
        'pnpm -F @vben/web-antd run dev',
        frontendDir,
      );
    }

    log('\n✅ 所有服务已启动！', 'green');
    log('\n📝 访问地址:', 'cyan');
    if (mode === 'all' || mode === 'backend') {
      log('   - 后端 API: http://localhost:8080', 'blue');
      log('   - API 文档: http://localhost:8080/doc.html', 'blue');
    }
    if (mode === 'all' || mode === 'frontend') {
      log('   - 前端应用: http://localhost:5173', 'blue');
    }
    log('\n💡 按 Ctrl+C 停止所有服务\n', 'yellow');

    // 等待所有进程退出
    await Promise.all(
      childProcesses.map(({ process: child }) => {
        return new Promise((resolve) => {
          if (child.killed) {
            resolve();
            return;
          }
          child.on('exit', resolve);
        });
      }),
    );
  } catch (error) {
    log(`\n❌ 启动失败: ${error.message}`, 'red');
    if (error.stack) {
      log(`   详细错误: ${error.stack}`, 'red');
    }
    cleanup();
    process.exit(1);
  }
}

// 运行
main().catch((error) => {
  log(`\n❌ 发生错误: ${error.message}`, 'red');
  process.exit(1);
});
