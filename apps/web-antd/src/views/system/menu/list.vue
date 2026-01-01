<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeGridListeners,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';

import { nextTick, onMounted, ref, watch } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';
import { $t } from '@vben/locales';
import { flattenTree } from '@vben/utils';

import { MenuBadge } from '@vben-core/menu-ui';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  batchUpdateMenuOrder,
  deleteMenu,
  getMenuList,
  SystemMenuApi,
} from '#/api/system/menu';

import { useColumns } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

/**
 * 将嵌套的树形数据转换为扁平数据（用于 transform: true）
 * @param treeData 嵌套的树形数据
 * @returns 扁平化的数据数组
 */
function flattenTreeData(
  treeData: SystemMenuApi.SystemMenu[],
): SystemMenuApi.SystemMenu[] {
  // 使用封装的工具函数
  return flattenTree<SystemMenuApi.SystemMenu>(treeData, {
    childProps: 'children',
    parentIdField: 'pid',
    idField: 'id',
    initialParentId: null,
  });
}

// 拖拽事件处理
function handleRowDragEnd() {
  // 先检查是否跨层级拖拽，如果是则直接返回，不执行保存
  if (!dragSourceRow.value) {
    // 如果没有源行信息，直接返回
    return;
  }

  // transform: true 模式下，使用 tableData 获取扁平数据
  const flatData = gridApi.grid?.getTableData()?.tableData || [];

  // 查找拖拽后的源行位置
  const draggedRow = flatData.find(
    (item) => item.id === dragSourceRow.value?.id,
  );

  if (!draggedRow) {
    // 如果找不到拖拽后的行，可能是数据问题，直接返回
    dragSourceRow.value = null;
    return;
  }

  const sourcePid = dragSourceRow.value.pid ?? null;
  const targetPid = draggedRow.pid ?? null;

  // 如果父级ID不同，说明跨层级了
  if (sourcePid !== targetPid) {
    message.warning('只能在同一层级内进行拖拽排序');
    // 刷新数据恢复原状
    dragSourceRow.value = null;
    onRefresh();
    return;
  }

  // 清除源行信息
  dragSourceRow.value = null;

  // 将扁平数据转换为树形结构，以便正确计算排序
  const buildTree = (
    items: SystemMenuApi.SystemMenu[],
    parentId: null | string = null,
  ): SystemMenuApi.SystemMenu[] => {
    return items
      .filter((item) => {
        const itemPid = item.pid ?? null;
        return itemPid === parentId;
      })
      .map((item) => ({
        ...item,
        children: buildTree(items, item.id),
      }));
  };

  const treeData = buildTree(flatData);

  // 递归遍历树形结构，收集所有需要更新的菜单
  // 只收集排序和父级ID发生变化的菜单，减少不必要的数据传输
  const collectMenus = (
    menus: SystemMenuApi.SystemMenu[],
    parentId: null | string = null,
    result: Array<{
      id: string;
      meta?: { order?: number };
      pid?: string;
    }> = [],
  ): Array<{
    id: string;
    meta?: { order?: number };
    pid?: string;
  }> => {
    menus.forEach((menu, index) => {
      const newOrder = index + 1;
      const newPid = parentId ?? menu.pid;
      const oldOrder = menu.meta?.order;
      const oldPid = menu.pid;

      // 只有当排序或父级ID发生变化时才添加到更新列表
      // 这样可以减少不必要的数据传输
      if (newOrder !== oldOrder || newPid !== oldPid) {
        result.push({
          id: menu.id,
          meta: {
            order: newOrder,
          },
          pid: newPid,
        });
      }

      // 递归处理子菜单
      if (menu.children && menu.children.length > 0) {
        collectMenus(menu.children, menu.id, result);
      }
    });
    return result;
  };

  // 收集所有需要更新的菜单
  const updatedMenus = collectMenus(treeData);

  // 批量更新排序
  if (updatedMenus.length > 0) {
    const hideLoading = message.loading({
      content: $t('ui.actionMessage.operating'),
      duration: 0,
      key: 'drag_sort_msg',
    });

    batchUpdateMenuOrder(updatedMenus)
      .then(() => {
        hideLoading();
        message.success({
          content: $t('ui.actionMessage.operationSuccess'),
          key: 'drag_sort_msg',
        });
        // 刷新数据以获取最新的排序结果
        onRefresh();
      })
      .catch((error) => {
        hideLoading();
        console.error('[Menu List] 批量更新排序失败:', error);
        message.error({
          content: $t('ui.actionMessage.operationFailed'),
          key: 'drag_sort_msg',
        });
        // 拖拽失败时刷新数据恢复原状
        onRefresh();
      });
  } else {
    // 如果没有需要更新的菜单，说明排序没有变化，直接清除拖拽状态
    dragSourceRow.value = null;
  }
}

// 使用 ref 存储扁平化的数据
const tableData = ref<SystemMenuApi.SystemMenu[]>([]);

// 存储拖拽开始时的源行信息
const dragSourceRow = ref<null | SystemMenuApi.SystemMenu>(null);

// 加载数据
async function loadData() {
  try {
    const data = await getMenuList();
    if (Array.isArray(data)) {
      // transform: true 时需要扁平数据，将嵌套结构转换为扁平结构
      tableData.value = flattenTreeData(data);
    }
  } catch (error) {
    console.error('[Menu List] 加载数据失败:', error);
    tableData.value = [];
  }
}

// 拖拽开始事件，保存源行信息
function handleRowDragStart(params: any) {
  // 保存拖拽开始时的源行信息
  // vxe-table 的事件参数格式：{ row, $rowIndex, column, $columnIndex, ... }
  const row = params?.row;
  if (row && row.id) {
    // 深拷贝保存源行信息，避免引用问题
    dragSourceRow.value = { ...row };
  }
  return true;
}

const gridEvents: VxeGridListeners<SystemMenuApi.SystemMenu> = {
  rowDragstart: handleRowDragStart,
  rowDragend: handleRowDragEnd,
};

const [Grid, gridApi] = useVbenVxeGrid({
  // 启用树形表格展开/折叠功能
  enableTreeExpandToggle: true,
  defaultTreeExpanded: true,
  gridEvents,
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
      isCrossDrag: true,
      // 禁止拖拽成为子节点（保持同级）
      isToChildDrag: false,
      // 禁止拖拽成为父节点（保持同级）
      isToParentDrag: false,
      // 拖拽触发方式：'row' 整行可拖拽
      trigger: 'row',
      // 不显示拖拽时的引导线
      showGuidesStatus: true,
      // 不显示拖拽时的提示文案（如"移动："等）
      showDragTip: false,
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

function onRefresh() {
  loadData();
}

// 监听数据变化，更新表格数据
watch(
  tableData,
  async (newData) => {
    gridApi.setState({
      gridOptions: {
        data: newData,
      },
    });
    // 数据更新后，等待表格渲染完成，然后展开所有树节点
    if (newData && newData.length > 0) {
      await nextTick();
      // 使用 requestAnimationFrame 确保表格已经完成渲染
      requestAnimationFrame(() => {
        gridApi.grid?.setAllTreeExpand(true);
      });
    }
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
