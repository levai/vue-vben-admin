<script lang="ts" setup>
import type { SystemOperationLogApi } from '#/api/system/operation-log';

import { computed } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { Descriptions, message, Tag, Typography } from 'ant-design-vue';

const { Text: TypographyText } = Typography;

const [Drawer, drawerApi] = useVbenDrawer({
  title: $t('system.operationLog.detail'),
  class: 'w-[800px]',
  footer: false,
});

const logData = computed(() => {
  return drawerApi.getData<SystemOperationLogApi.SystemOperationLog>() || null;
});

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
    view: 'blue',
    add: 'green',
    edit: 'orange',
    delete: 'red',
    export: 'cyan',
    import: 'purple',
    login: 'success',
    logout: 'default',
    download: 'blue',
    upload: 'green',
  };
  return colorMap[type] || 'default';
}

/**
 * 将操作模块英文值转换为中文
 */
function getOperationModuleLabel(module: string): string {
  const labelMap: Record<string, string> = {
    system: '系统管理',
    user: '用户管理',
    role: '角色管理',
    menu: '菜单管理',
    dept: '部门管理',
    permission: '权限管理',
    'operation-log': '操作日志',
  };
  return labelMap[module] || module;
}

/**
 * 格式化状态标签
 */
function getStatusTag(status: number) {
  return status === 1
    ? { color: 'success', text: $t('system.operationLog.status.success') }
    : { color: 'error', text: $t('system.operationLog.status.failed') };
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
 * 获取要复制的原始JSON字符串（未格式化的）
 */
function getRawJson(jsonStr?: string) {
  if (!jsonStr) {
    return '';
  }
  // 返回原始字符串，用于复制
  return jsonStr;
}
</script>

<template>
  <Drawer>
    <Descriptions v-if="logData" :column="1" bordered size="small">
      <Descriptions.Item :label="$t('system.operationLog.id')">
        {{ logData.id }}
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.createTime')">
        {{ logData.createTime }}
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.username')">
        <div>
          <div>{{ logData.username }}</div>
          <div v-if="logData.realName" class="text-xs text-gray-500">
            {{ logData.realName }}
          </div>
        </div>
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.operationType')">
        <Tag :color="getOperationTypeColor(logData.operationType)">
          {{ getOperationTypeLabel(logData.operationType) }}
        </Tag>
      </Descriptions.Item>
      <Descriptions.Item
        :label="$t('system.operationLog.operationModule.title')"
      >
        {{
          logData.operationModule
            ? getOperationModuleLabel(logData.operationModule)
            : '-'
        }}
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.operationPage')">
        {{ logData.operationPage || '-' }}
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.pageName')">
        {{ logData.pageName || '-' }}
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.requestMethod')">
        <Tag>{{ logData.requestMethod || '-' }}</Tag>
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.requestUrl')">
        <div class="break-all">{{ logData.requestUrl || '-' }}</div>
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.requestParams')">
        <div
          v-if="logData.requestParams"
          class="relative max-h-40 overflow-auto rounded bg-gray-100"
        >
          <div class="absolute right-2 top-2 z-10">
            <TypographyText
              :copyable="{
                text: getRawJson(logData.requestParams),
                onCopy: () => {
                  message.success($t('common.copySuccess') || '复制成功');
                },
              }"
            />
          </div>
          <pre class="min-w-max whitespace-pre p-2 pr-12 text-xs">{{
            formatJson(logData.requestParams)
          }}</pre>
        </div>
        <span v-else>-</span>
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.responseCode')">
        {{ logData.responseCode || '-' }}
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.responseData')">
        <div
          v-if="logData.responseData"
          class="relative max-h-40 overflow-auto rounded bg-gray-100"
        >
          <div class="absolute right-2 top-2 z-10">
            <TypographyText
              :copyable="{
                text: getRawJson(logData.responseData),
                onCopy: () => {
                  message.success($t('common.copySuccess') || '复制成功');
                },
              }"
            />
          </div>
          <pre class="min-w-max whitespace-pre p-2 pr-12 text-xs">{{
            formatJson(logData.responseData)
          }}</pre>
        </div>
        <span v-else>-</span>
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.ipAddress')">
        {{ logData.ipAddress || '-' }}
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.userAgent')">
        <div class="break-all text-xs">{{ logData.userAgent || '-' }}</div>
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.browser')">
        <div>
          <div v-if="logData.browser">{{ logData.browser }}</div>
          <div v-if="logData.os" class="text-xs text-gray-500">
            {{ logData.os }}
          </div>
          <span v-if="!logData.browser && !logData.os">-</span>
        </div>
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.duration')">
        {{ logData.duration ? `${logData.duration}ms` : '-' }}
      </Descriptions.Item>
      <Descriptions.Item :label="$t('system.operationLog.status.title')">
        <Tag :color="getStatusTag(logData.status).color">
          {{ getStatusTag(logData.status).text }}
        </Tag>
      </Descriptions.Item>
      <Descriptions.Item
        v-if="logData.errorMessage"
        :label="$t('system.operationLog.errorMessage')"
      >
        <div class="break-all text-red-500">{{ logData.errorMessage }}</div>
      </Descriptions.Item>
    </Descriptions>
  </Drawer>
</template>
