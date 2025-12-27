<script lang="ts" setup>
import { useElementPlusDesignTokens } from '@vben/hooks';

import { elementLocale } from '#/locales';
import { ElConfigProvider } from 'element-plus';

defineOptions({ name: 'App' });

useElementPlusDesignTokens();
</script>

<template>
  <ElConfigProvider :locale="elementLocale">
    <RouterView />
  </ElConfigProvider>
</template>
