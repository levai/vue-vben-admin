<script lang="ts" setup>
import type { OnActionClickParams } from '#/adapter/vxe-table';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';
import { $t } from '@vben/locales';
import { flattenTree } from '@vben/utils';

import { MenuBadge } from '@vben-core/menu-ui';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMenu, getMenuList, SystemMenuApi } from '#/api/system/menu';
import { SYSTEM_PERMISSION_CODES } from '#/constants/permission-codes';

import { useMenuList } from './composables/use-menu-list';
import { useColumns } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

// 使用菜单列表 Composable（仅用于拖拽功能）
const { createGridEvents } = useMenuList();

// 操作按钮点击处理（需要在 useColumns 之前定义）
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

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async () => {
          try {
            // 获取树形数据
            const treeData = await getMenuList();
            if (!Array.isArray(treeData)) {
              return [];
            }
            // transform: true 时需要扁平数据，将嵌套结构转换为扁平结构
            const flatData = flattenTree<SystemMenuApi.SystemMenu>(treeData, {
              childProps: 'children',
              parentIdField: 'pid',
              idField: 'id',
              initialParentId: null,
            });
            return flatData;
          } catch {
            return [];
          }
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      drag: true, // 启用行拖拽
    },
    rowDragConfig: {
      isCrossDrag: false,
      isPeerDrag: true, // 只允许同级拖拽
      trigger: 'row',
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      zoom: true,
      expand: true, // 启用展开/折叠按钮（框架自动处理）
    },
    treeConfig: {
      parentField: 'pid',
      rowField: 'id',
      transform: true, // 启用行拖拽时必须为 true
      reserve: true, // 保留展开状态
    },
  },
});

// 刷新函数
function onRefresh() {
  gridApi.query();
}

// 创建表格事件处理器（合并拖拽事件）
const dragEvents = createGridEvents(gridApi as any, onRefresh);

// 更新 gridEvents（合并拖拽事件）
gridApi.setState({
  gridEvents: {
    ...dragEvents,
  },
});

// CRUD 操作
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
        <Button
          v-access:code="SYSTEM_PERMISSION_CODES.MENU.ADD"
          type="primary"
          @click="onCreate"
        >
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
          <span class="flex-auto">{{ $t(row.meta?.title || '') }}</span>
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
