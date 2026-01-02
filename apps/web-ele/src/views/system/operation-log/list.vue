<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemOperationLogApi } from '#/api/system/operation-log';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { ElMessage, ElTag } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteOperationLog,
  getOperationLogDetail,
  getOperationLogList,
} from '#/api/system/operation-log';

import { useColumns, useSearchSchema } from './data';
import Detail from './modules/detail.vue';

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: Detail,
  destroyOnClose: true,
  class: 'w-[800px]',
});

/**
 * 查看详情
 */
function onView(row: SystemOperationLogApi.SystemOperationLog) {
  getOperationLogDetail(row.id).then((data) => {
    detailDrawerApi.setData(data).open();
  });
}

/**
 * 删除日志
 */
function onDelete(row: SystemOperationLogApi.SystemOperationLog) {
  ElMessage({
    duration: 1500,
    message: $t('ui.actionMessage.deleting', [row.id]),
  });
  deleteOperationLog(row.id)
    .then(() => {
      ElMessage.closeAll();
      ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
      refreshGrid();
    })
    .catch(() => {
      ElMessage.closeAll();
    });
}

/**
 * 表格操作按钮的回调函数
 */
function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemOperationLogApi.SystemOperationLog>) {
  switch (code) {
    case 'delete': {
      onDelete(row);
      break;
    }
    case 'view': {
      onView(row);
      break;
    }
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridEvents: {},
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true,
      pageSizes: [10, 20, 50, 100],
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const params: SystemOperationLogApi.OperationLogQueryParams = {
            page: page.currentPage,
            pageSize: page.pageSize,
          };

          // 只传递非空值
          if (formValues?.username?.trim()) {
            params.username = formValues.username.trim();
          }
          if (formValues?.operationType?.trim()) {
            params.operationType = formValues.operationType.trim();
          }
          if (formValues?.operationModule?.trim()) {
            params.operationModule = formValues.operationModule.trim();
          }
          if (formValues?.status !== undefined && formValues?.status !== null) {
            params.status = formValues.status;
          }
          if (formValues?.search?.trim()) {
            params.search = formValues.search.trim();
          }

          // 日期范围已通过 fieldMappingTime 自动映射为 startTime 和 endTime
          if (formValues?.startTime) {
            params.startTime = formValues.startTime;
          }
          if (formValues?.endTime) {
            params.endTime = formValues.endTime;
          }

          const result = await getOperationLogList(params);
          return {
            items: result.items || [],
            total: result.total || 0,
          };
        },
      },
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions,
  formOptions: {
    fieldMappingTime: [['createTime', ['startTime', 'endTime']]],
    schema: useSearchSchema(),
    submitOnChange: true,
  },
});

/**
 * 刷新表格
 */
function refreshGrid() {
  gridApi.query();
}

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
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid table-title="操作日志">
      <template #operationType="{ row }">
        <ElTag :type="getOperationTypeColor(row.operationType)">
          {{ getOperationTypeLabel(row.operationType) }}
        </ElTag>
      </template>
      <template #operationModule="{ row }">
        {{ row.operationModule || '-' }}
      </template>
      <template #status="{ row }">
        <ElTag :type="getStatusTag(row.status).type">
          {{ getStatusTag(row.status).text }}
        </ElTag>
      </template>
      <template #browser="{ row }">
        <div v-if="row.browser || row.os">
          <div v-if="row.browser">{{ row.browser }}</div>
          <div v-if="row.os" class="text-xs text-gray-500">{{ row.os }}</div>
        </div>
        <span v-else>-</span>
      </template>
    </Grid>
  </Page>
</template>
