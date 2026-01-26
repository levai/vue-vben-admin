/**
 * 路径工具
 * 提供统一的路径管理
 */

import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export const rootDir = join(__dirname, '../..');
export const backendDir = join(rootDir, 'backend');
export const frontendDir = join(rootDir, 'frontend');
