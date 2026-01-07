<script lang="ts" setup>
import type { SystemOperationLogApi } from '#/api/system/operation-log';

import { computed } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { useClipboard } from '@vueuse/core';
import {
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
  ElTag,
} from 'element-plus';

const [Drawer, drawerApi] = useVbenDrawer({
  title: $t('system.operationLog.detail'),
  class: 'w-[800px]',
  footer: false,
});

const logData = computed(() => {
  return drawerApi.getData<SystemOperationLogApi.SystemOperationLog>() || null;
});

const { copy, isSupported } = useClipboard();

/**
 * 将操作类型英文值转换为中文
 */
function getOperationTypeLabel(type: string): string {
  const labelMap: Record<string, string> = {
    view: '查看',
    add: '新增',
    edit: '编辑',
    delete: '删除',
    export: '导出',
    import: '导入',
    login: '登录',
    logout: '登出',
    download: '下载',
    upload: '上传',
  };
  return labelMap[type] || type;
}

/**
 * 获取操作类型标签颜色
 */
function getOperationTypeColor(type: string) {
  const colorMap: Record<string, string> = {
    view: 'info',
    add: 'success',
    edit: 'warning',
    delete: 'danger',
    export: 'primary',
    import: '',
    login: 'success',
    logout: 'info',
    download: 'primary',
    upload: 'success',
  };
  return colorMap[type] || 'info';
}

/**
 * 格式化状态标签
 */
function getStatusTag(status: number) {
  return status === 1
    ? { type: 'success', text: $t('system.operationLog.status.success') }
    : { type: 'danger', text: $t('system.operationLog.status.failed') };
}

/**
 * 格式化JSON字符串
 */
function formatJson(jsonStr?: string) {
  if (!jsonStr) {
    return '-';
  }
  try {
    const obj = JSON.parse(jsonStr);
    return JSON.stringify(obj, null, 2);
  } catch {
    return jsonStr;
  }
}

/**
 * 复制JSON内容
 */
async function copyJson(jsonStr?: string) {
  if (!jsonStr) {
    return;
  }
  if (isSupported.value) {
    await copy(jsonStr);
    ElMessage.success($t('common.copySuccess') || '复制成功');
  } else {
    ElMessage.error('浏览器不支持复制功能');
  }
}
</script>

<template>
  <Drawer>
    <ElDescriptions v-if="logData" :column="1" border>
      <ElDescriptionsItem :label="$t('system.operationLog.id')">
        {{ logData.id }}
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.createTime')">
        {{ logData.createTime }}
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.username')">
        <div>
          <div>{{ logData.username }}</div>
          <div v-if="logData.realName" class="text-xs text-gray-500">
            {{ logData.realName }}
          </div>
        </div>
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.operationType')">
        <ElTag :type="getOperationTypeColor(logData.operationType)">
          {{ getOperationTypeLabel(logData.operationType) }}
        </ElTag>
      </ElDescriptionsItem>
      <ElDescriptionsItem
        :label="$t('system.operationLog.operationModule.title')"
      >
        {{ logData.operationModule || '-' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.operationPage')">
        {{ logData.operationPage || '-' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.pageName')">
        {{ logData.pageName || '-' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.requestMethod')">
        <ElTag>{{ logData.requestMethod || '-' }}</ElTag>
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.requestUrl')">
        <div class="break-all">{{ logData.requestUrl || '-' }}</div>
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.requestParams')">
        <div
          v-if="logData.requestParams"
          class="relative max-h-40 overflow-auto rounded bg-gray-100"
        >
          <div class="absolute right-2 top-2 z-10">
            <button
              class="rounded bg-white px-2 py-1 text-xs hover:bg-gray-200"
              @click="copyJson(logData.requestParams)"
            >
              复制
            </button>
          </div>
          <pre class="min-w-max whitespace-pre p-2 pr-12 text-xs">{{
            formatJson(logData.requestParams)
          }}</pre>
        </div>
        <span v-else>-</span>
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.responseCode')">
        {{ logData.responseCode || '-' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.responseData')">
        <div
          v-if="logData.responseData"
          class="relative max-h-40 overflow-auto rounded bg-gray-100"
        >
          <div class="absolute right-2 top-2 z-10">
            <button
              class="rounded bg-white px-2 py-1 text-xs hover:bg-gray-200"
              @click="copyJson(logData.responseData)"
            >
              复制
            </button>
          </div>
          <pre class="min-w-max whitespace-pre p-2 pr-12 text-xs">{{
            formatJson(logData.responseData)
          }}</pre>
        </div>
        <span v-else>-</span>
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.ipAddress')">
        {{ logData.ipAddress || '-' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.userAgent')">
        <div class="break-all text-xs">{{ logData.userAgent || '-' }}</div>
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.browser')">
        <div>
          <div v-if="logData.browser">{{ logData.browser }}</div>
          <div v-if="logData.os" class="text-xs text-gray-500">
            {{ logData.os }}
          </div>
          <span v-if="!logData.browser && !logData.os">-</span>
        </div>
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.duration')">
        {{ logData.duration ? `${logData.duration}ms` : '-' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem :label="$t('system.operationLog.status.title')">
        <ElTag :type="getStatusTag(logData.status).type">
          {{ getStatusTag(logData.status).text }}
        </ElTag>
      </ElDescriptionsItem>
      <ElDescriptionsItem
        v-if="logData.errorMessage"
        :label="$t('system.operationLog.errorMessage')"
      >
        <div class="break-all text-red-500">{{ logData.errorMessage }}</div>
      </ElDescriptionsItem>
    </ElDescriptions>
  </Drawer>
</template>
