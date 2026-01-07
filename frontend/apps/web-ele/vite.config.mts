import process from 'node:process';

import { defineConfig } from '@vben/vite-config';

import { loadEnv } from 'vite';

import ElementPlus from 'unplugin-element-plus/vite';

export default defineConfig(async (config) => {
  // 使用 Vite 的 loadEnv 加载环境变量

  const env = loadEnv(config?.mode || 'development', process.cwd(), '');

  // 代理目标地址，可通过环境变量 VITE_API_PROXY 配置，默认为 http://localhost:5320/api
  const apiProxy = env.VITE_API_PROXY || 'http://localhost:5320/api';
  console.log('[ apiProxy ]-14', apiProxy);

  return {
    application: {},
    vite: {
      plugins: [
        ElementPlus({
          format: 'esm',
        }),
      ],
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            // 代理目标地址，可通过环境变量 VITE_API_PROXY 配置
            target: apiProxy,
            ws: true,
          },
        },
      },
    },
  };
});
