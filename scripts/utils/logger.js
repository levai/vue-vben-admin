/**
 * 日志工具
 * 提供统一的颜色输出功能
 */

const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  cyan: '\x1b[36m',
  magenta: '\x1b[35m',
};

export function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

export { colors };
