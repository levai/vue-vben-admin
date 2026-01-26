/**
 * 交互式菜单工具
 * 支持方向键选择和回车确认
 * 无需外部依赖，使用 Node.js 内置模块
 */

import readline from 'readline';
import { log } from './logger.js';

const isWindows = process.platform === 'win32';

/**
 * 显示交互式菜单并返回用户选择
 * @param {Array<{label: string, value: string, color?: string}>} options - 菜单选项
 * @returns {Promise<string>} 用户选择的值
 */
export function showInteractiveMenu(options) {
  return new Promise((resolve, reject) => {
    if (!process.stdin.isTTY) {
      // 非 TTY 环境，回退到普通输入
      const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout,
      });
      
      console.log('\n请选择：');
      options.forEach((opt, index) => {
        console.log(`  ${index + 1}. ${opt.label}`);
      });
      
      rl.question('\n请输入选项 (1-' + options.length + '): ', (answer) => {
        const choice = parseInt(answer.trim()) - 1;
        rl.close();
        if (choice >= 0 && choice < options.length) {
          resolve(options[choice].value);
        } else {
          reject(new Error('无效选项'));
        }
      });
      return;
    }

    let selectedIndex = 0;
    let isResolved = false;

    // 设置 raw mode
    const wasRawMode = process.stdin.isRaw;
    if (!wasRawMode) {
      process.stdin.setRawMode(true);
    }
    process.stdin.resume();
    process.stdin.setEncoding('utf8');

    // 启用 keypress 事件
    readline.emitKeypressEvents(process.stdin);

    // 渲染菜单
    function renderMenu() {
      // 清屏并移动光标到顶部
      process.stdout.write('\x1B[2J\x1B[H');
      
      log('\n═══════════════════════════════════════', 'bright');
      log('   Vue Vben Admin 启动菜单', 'bright');
      log('═══════════════════════════════════════\n', 'bright');
      
      log('请使用方向键 ↑↓ 选择，回车确认：\n', 'cyan');
      
      options.forEach((opt, index) => {
        const isSelected = index === selectedIndex;
        const prefix = isSelected ? '❯ ' : '  ';
        
        if (isSelected) {
          // 高亮显示选中的项
          process.stdout.write(`\x1B[47m\x1B[30m${prefix}${opt.label}\x1B[0m\n`);
        } else {
          log(`${prefix}${opt.label}`, opt.color || 'reset');
        }
      });
      
      log('\n💡 提示：↑↓ 键选择，回车确认，Ctrl+C 退出\n', 'yellow');
    }

    // 处理键盘输入
    function handleKeypress(str, key) {
      if (isResolved) return;

      // Ctrl+C
      if (key && key.ctrl && key.name === 'c') {
        cleanup();
        process.exit(0);
        return;
      }

      // 方向键上
      if (key && key.name === 'up') {
        selectedIndex = (selectedIndex - 1 + options.length) % options.length;
        renderMenu();
        return;
      }

      // 方向键下
      if (key && key.name === 'down') {
        selectedIndex = (selectedIndex + 1) % options.length;
        renderMenu();
        return;
      }

      // 回车确认
      if (key && (key.name === 'return' || key.name === 'enter')) {
        cleanup();
        isResolved = true;
        resolve(options[selectedIndex].value);
        return;
      }

      // 数字键快速选择
      const num = parseInt(str);
      if (!isNaN(num) && num >= 1 && num <= options.length) {
        cleanup();
        isResolved = true;
        resolve(options[num - 1].value);
        return;
      }
    }

    // 绑定事件
    process.stdin.on('keypress', handleKeypress);

    // 初始渲染
    renderMenu();

    // 清理函数
    function cleanup() {
      if (!wasRawMode) {
        process.stdin.setRawMode(false);
      }
      process.stdin.pause();
      process.stdin.removeAllListeners('keypress');
    }

    // 错误处理
    process.stdin.on('error', (error) => {
      if (!isResolved) {
        cleanup();
        reject(error);
      }
    });

    // 确保在退出时清理
    process.on('exit', cleanup);
    process.on('SIGINT', () => {
      cleanup();
      process.exit(0);
    });
  });
}
