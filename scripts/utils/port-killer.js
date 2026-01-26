/**
 * 端口清理工具
 * 在启动服务前杀死占用指定端口的进程
 * 跨平台支持：Windows/macOS/Linux
 */

import { spawn, execSync } from 'child_process';
import { log } from './logger.js';

const isWindows = process.platform === 'win32';

/**
 * 杀死占用指定端口的进程
 * @param {number} port - 端口号
 * @param {string} serviceName - 服务名称（用于日志）
 * @returns {Promise<void>}
 */
export function killPort(port, serviceName = '服务') {
  return new Promise((resolve) => {
    log(`\n🔍 检查端口 ${port} 占用情况...`, 'cyan');

    if (isWindows) {
      // Windows: 使用 netstat 查找占用端口的进程
      try {
        const result = execSync(
          `netstat -ano | findstr :${port}`,
          { encoding: 'utf8', stdio: 'pipe' }
        );
        
        const lines = result.trim().split('\n');
        const pids = new Set();
        
        for (const line of lines) {
          const match = line.match(/\s+(\d+)\s*$/);
          if (match) {
            pids.add(match[1]);
          }
        }

        if (pids.size === 0) {
          log(`✅ 端口 ${port} 未被占用`, 'green');
          resolve();
          return;
        }

        log(`⚠️  发现 ${pids.size} 个进程占用端口 ${port}，正在清理...`, 'yellow');
        
        // 杀死所有占用端口的进程
        const killPromises = Array.from(pids).map((pid) => {
          return new Promise((resolveKill) => {
            const killProcess = spawn('taskkill', ['/pid', pid, '/f', '/t'], {
              stdio: 'ignore',
              shell: true,
            });
            killProcess.on('exit', () => {
              log(`   ✓ 已终止进程 ${pid}`, 'green');
              resolveKill();
            });
            killProcess.on('error', () => resolveKill());
          });
        });

        Promise.all(killPromises).then(() => {
          log(`✅ 端口 ${port} 已清理完成`, 'green');
          // 等待一下确保端口释放
          setTimeout(resolve, 500);
        });
      } catch (error) {
        // 如果没有找到占用端口的进程，netstat 会抛出错误
        log(`✅ 端口 ${port} 未被占用`, 'green');
        resolve();
      }
    } else {
      // Unix/macOS/Linux: 使用 lsof 查找占用端口的进程
      try {
        const result = execSync(
          `lsof -ti :${port}`,
          { encoding: 'utf8', stdio: 'pipe' }
        );
        
        const pids = result.trim().split('\n').filter(Boolean);
        
        if (pids.length === 0) {
          log(`✅ 端口 ${port} 未被占用`, 'green');
          resolve();
          return;
        }

        log(`⚠️  发现 ${pids.length} 个进程占用端口 ${port}，正在清理...`, 'yellow');
        
        // 杀死所有占用端口的进程
        const killPromises = pids.map((pid) => {
          return new Promise((resolveKill) => {
            try {
              execSync(`kill -9 ${pid}`, { stdio: 'ignore' });
              log(`   ✓ 已终止进程 ${pid}`, 'green');
              resolveKill();
            } catch (error) {
              resolveKill();
            }
          });
        });

        Promise.all(killPromises).then(() => {
          log(`✅ 端口 ${port} 已清理完成`, 'green');
          // 等待一下确保端口释放
          setTimeout(resolve, 500);
        });
      } catch (error) {
        // 如果没有找到占用端口的进程，lsof 会抛出错误
        log(`✅ 端口 ${port} 未被占用`, 'green');
        resolve();
      }
    }
  });
}

/**
 * 清理多个端口
 * @param {Array<number>} ports - 端口号数组
 * @returns {Promise<void>}
 */
export async function killPorts(ports) {
  for (const port of ports) {
    await killPort(port);
  }
}
