/**
 * 进程管理工具
 * 提供统一的进程启动和清理功能
 */

import { spawn } from 'child_process';
import { log } from './logger.js';

const isWindows = process.platform === 'win32';

// 存储所有子进程
const childProcesses = [];
let isCleaningUp = false;

/**
 * 启动服务
 * @param {string} name - 服务名称
 * @param {string} command - 执行命令
 * @param {string} cwd - 工作目录
 * @param {object} env - 环境变量
 * @returns {Promise} 返回进程 Promise
 */
export function startService(name, command, cwd, env = {}) {
  return new Promise((resolve, reject) => {
    log(`\n🚀 启动 ${name}...`, 'cyan');
    log(`📁 工作目录: ${cwd}`, 'blue');
    log(`💻 执行命令: ${command}\n`, 'blue');

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
          if (childProcesses.some((p) => !p.exited)) {
            log('   正在清理其他服务...', 'yellow');
            cleanup();
          }
        } else {
          log(`\n✅ ${name} 已停止`, 'green');
        }
      }
    });

    setTimeout(() => {
      if (!child.killed && child.pid) {
        resolve(child);
      }
    }, 500);
  });
}

/**
 * 清理所有进程
 */
export function cleanup() {
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
          const killProcess = spawn('taskkill', ['/pid', child.pid, '/f', '/t'], {
            stdio: 'ignore',
            shell: true,
          });
          killProcess.on('exit', () => resolve());
          killProcess.on('error', () => resolve());
        } else {
          child.kill('SIGTERM');
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

/**
 * 注册清理函数
 */
export function registerCleanupHandlers() {
  process.on('SIGINT', cleanup);
  process.on('SIGTERM', cleanup);

  // Windows 特殊处理：监听 Ctrl+C
  if (isWindows && process.stdin.isTTY) {
    try {
      process.stdin.setRawMode(true);
      process.stdin.resume();
      process.stdin.setEncoding('utf8');
      process.stdin.on('data', (data) => {
        if (data === '\u0003' || data.charCodeAt(0) === 3) {
          cleanup();
        }
      });
    } catch (error) {
      // 忽略错误
    }
  }
}

/**
 * 等待所有进程退出
 */
export function waitForAllProcesses() {
  return Promise.all(
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
}
