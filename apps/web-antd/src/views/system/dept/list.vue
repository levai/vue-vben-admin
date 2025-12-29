<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemDeptApi } from '#/api/system/dept';

import { ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDept, getDeptList } from '#/api/system/dept';
import { $t } from '#/locales';

import { useColumns } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/**
 * 编辑部门
 * @param row
 */
function onEdit(row: SystemDeptApi.SystemDept) {
  formModalApi.setData(row).open();
}

/**
 * 添加下级部门
 * @param row
 */
function onAppend(row: SystemDeptApi.SystemDept) {
  formModalApi.setData({ pid: row.id }).open();
}

/**
 * 创建新部门
 */
function onCreate() {
  formModalApi.setData(null).open();
}

/**
 * 删除部门
 * @param row
 */
function onDelete(row: SystemDeptApi.SystemDept) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteDept(row.id)
    .then(() => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.name]),
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
}: OnActionClickParams<SystemDeptApi.SystemDept>) {
  switch (code) {
    case 'append': {
      onAppend(row);
      break;
    }
    case 'delete': {
      onDelete(row);
      break;
    }
    case 'edit': {
      onEdit(row);
      break;
    }
  }
}

// 展开/折叠状态
const isExpanded = ref(true);

/**
 * 切换展开/折叠
 */
function toggleExpand() {
  isExpanded.value = !isExpanded.value;
  // 直接调用 setAllTreeExpand 立即生效
  if (gridApi.grid) {
    gridApi.grid.setAllTreeExpand(isExpanded.value);
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridEvents: {},
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_params) => {
          const result = await getDeptList();
          // 数据返回后，使用微任务确保树形结构构建完成后再设置展开状态
          // 使用 Promise.resolve().then() 确保在下一个事件循环中执行，避免闪烁
          Promise.resolve().then(() => {
            requestAnimationFrame(() => {
              if (gridApi.grid) {
                gridApi.grid.setAllTreeExpand(isExpanded.value);
              }
            });
          });
          return result;
        },
      },
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      zoom: true,
    },
    treeConfig: {
      parentField: 'pid',
      rowField: 'id',
      transform: false,
      // 初始状态跟随 isExpanded
      expandAll: isExpanded.value,
    },
  } as VxeTableGridOptions,
});

/**
 * 刷新表格
 */
function refreshGrid() {
  // 刷新前确保 treeConfig 与 isExpanded 状态一致
  gridApi.setGridOptions({
    treeConfig: {
      parentField: 'pid',
      rowField: 'id',
      transform: false,
      expandAll: isExpanded.value,
    },
  });
  gridApi.query();
}
</script>
<template>
  <Page auto-content-height>
    <FormModal @success="refreshGrid" />
    <Grid table-title="部门列表">
      <template #toolbar-tools>
        <Button type="primary" @click="onCreate">
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.dept.name')]) }}
        </Button>
        <Button
          class="ml-2 flex !h-8 !w-8 items-center justify-center !rounded-full !p-0"
          v-tippy="{ content: isExpanded ? '折叠全部' : '展开全部' }"
          @click="toggleExpand"
        >
          <IconifyIcon
            :icon="
              isExpanded ? 'lucide:chevrons-down-up' : 'lucide:chevrons-up-down'
            "
            class="size-4"
          />
        </Button>
      </template>
    </Grid>
  </Page>
</template>
