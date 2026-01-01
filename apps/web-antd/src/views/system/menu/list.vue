<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';

import { onMounted, watch } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';
import { $t } from '@vben/locales';

import { MenuBadge } from '@vben-core/menu-ui';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMenu, SystemMenuApi } from '#/api/system/menu';

import { useMenuList } from './composables/use-menu-list';
import { useColumns } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

// 使用菜单列表 Composable
const { tableData, loadData, refreshData, createGridEvents, setupTableData } =
  useMenuList();

const [Grid, gridApi] = useVbenVxeGrid({
  // 启用树形表格展开/折叠功能
  enableTreeExpandToggle: true,
  defaultTreeExpanded: true,
  gridEvents: {},
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    data: [], // 初始为空数组，通过 watch 更新
    pagerConfig: {
      enabled: false,
    },
    rowConfig: {
      keyField: 'id',
      drag: true, // 启用行拖拽
    },
    rowDragConfig: {
      // 允许跨层级拖拽（但实际只允许同级排序，通过其他配置限制）
      // 注意：isCrossDrag: false 可能会阻止子级节点的拖拽
      isCrossDrag: false,
      // 禁止拖拽成为子节点（保持同级）
      isToChildDrag: false,
      // 禁止拖拽成为父节点（保持同级）
      isToParentDrag: false,
      // 只允许同级拖拽
      isPeerDrag: true,
      // 拖拽触发方式：'row' 整行可拖拽
      trigger: 'row',
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
      transform: true, // 启用行拖拽时必须为 true
    },
  } as VxeTableGridOptions,
});

// 刷新函数（需要在 gridApi 定义后）
function onRefresh() {
  refreshData(gridApi);
}

// 创建表格事件处理器（需要在 gridApi 定义后）
const gridEvents = createGridEvents(gridApi, onRefresh);

// 更新 gridEvents
gridApi.setState({
  gridEvents,
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemMenuApi.SystemMenu>) {
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
    default: {
      break;
    }
  }
}

// 监听数据变化，更新表格数据
watch(
  tableData,
  async (newData) => {
    setupTableData(gridApi, newData);
  },
  { immediate: true },
);

// 组件挂载时加载数据
onMounted(() => {
  loadData();
});
function onEdit(row: SystemMenuApi.SystemMenu) {
  formDrawerApi.setData(row).open();
}
function onCreate() {
  formDrawerApi.setData({}).open();
}
function onAppend(row: SystemMenuApi.SystemMenu) {
  formDrawerApi.setData({ pid: row.id }).open();
}

function onDelete(row: SystemMenuApi.SystemMenu) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteMenu(row.id)
    .then(() => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.name]),
        key: 'action_process_msg',
      });
      onRefresh();
    })
    .catch(() => {
      hideLoading();
    });
}
</script>
<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <Grid>
      <template #toolbar-tools>
        <Button type="primary" @click="onCreate">
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.menu.name')]) }}
        </Button>
      </template>
      <template #title="{ row }">
        <div class="flex w-full items-center gap-1">
          <div class="size-5 flex-shrink-0">
            <IconifyIcon
              v-if="row.type === 'button'"
              icon="carbon:security"
              class="size-full"
            />
            <IconifyIcon
              v-else-if="row.meta?.icon"
              :icon="row.meta?.icon || 'carbon:circle-dash'"
              class="size-full"
            />
          </div>
          <span class="flex-auto">{{ $t(row.meta?.title) }}</span>
          <div class="items-center justify-end"></div>
        </div>
        <MenuBadge
          v-if="row.meta?.badgeType"
          class="menu-badge"
          :badge="row.meta.badge"
          :badge-type="row.meta.badgeType"
          :badge-variants="row.meta.badgeVariants"
        />
      </template>
    </Grid>
  </Page>
</template>
<style lang="scss" scoped>
.menu-badge {
  top: 50%;
  right: 0;
  transform: translateY(-50%);

  & > :deep(div) {
    padding-top: 0;
    padding-bottom: 0;
  }
}
</style>
