<script lang="ts" setup>
import type { OnActionClickParams } from '#/adapter/vxe-table';
import type { SystemOperationLogApi } from '#/api/system/operation-log';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { message, Tag } from 'ant-design-vue';

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
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteOperationLog(row.id)
    .then(() => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.id]),
        key: 'action_process_msg',
      });
      refreshGrid();
    })
    .catch(() => {
      hideLoading();
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

const [Grid, gridApi] =
  useVbenVxeGrid<SystemOperationLogApi.SystemOperationLog>({
    formOptions: {
      fieldMappingTime: [['createTime', ['startTime', 'endTime']]],
      schema: useSearchSchema(),
      submitOnChange: true,
    },
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
          query: async (
            { page }: any,
            formValues: SystemOperationLogApi.OperationLogQueryParams,
          ) => {
            return await getOperationLogList({
              page: page.currentPage,
              pageSize: page.pageSize,
              ...formValues,
            });
          },
        },
      },
      rowConfig: {
        keyField: 'id',
      },
      toolbarConfig: {
        custom: true,
        export: false,
        refresh: true,
        search: true,
        zoom: true,
      },
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
    view: 'default',
    add: 'green',
    edit: 'orange',
    delete: 'error',
    export: 'blue',
    import: 'purple',
    login: 'success',
    logout: 'default',
    download: 'cyan',
    upload: 'green',
  };
  return colorMap[type] || 'default';
}

/**
 * 格式化状态标签
 */
function getStatusTag(status: number) {
  return status === 1
    ? { color: 'success', text: $t('system.operationLog.status.success') }
    : { color: 'error', text: $t('system.operationLog.status.failed') };
}
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid table-title="操作日志">
      <template #operationType="{ row }">
        <Tag :color="getOperationTypeColor(row.operationType)">
          {{ getOperationTypeLabel(row.operationType) }}
        </Tag>
      </template>
      <template #operationModule="{ row }">
        {{ row.operationModule || '-' }}
      </template>
      <template #status="{ row }">
        <Tag :color="getStatusTag(row.status).color">
          {{ getStatusTag(row.status).text }}
        </Tag>
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
