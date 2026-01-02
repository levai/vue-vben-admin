import { defineOverridesPreferences } from '@vben/preferences';

/**
 * @description 项目配置文件
 * 只需要覆盖项目中的一部分配置，不需要的配置不用覆盖，会自动使用默认配置
 * !!! 更改配置后请清空缓存，否则可能不生效
 */
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    name: import.meta.env.VITE_APP_TITLE,
    authPageLayout: 'panel-right',
    accessMode: 'backend',
  },
  theme: {
    mode: 'light',
  },
  widget: {
    fullscreen: true, // 全屏
    globalSearch: true, // 全局搜索
    languageToggle: false, // 语言切换
    lockScreen: true, // 锁屏
    notification: true, // 通知
    refresh: true, // 刷新
    sidebarToggle: true, // 侧边栏显示/隐藏
    themeToggle: true, // 主题切换
    timezone: false, // 时区
  },
});
