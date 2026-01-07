import type { useVbenVxeGrid, VxeGridListeners } from '#/adapter/vxe-table';

import { ref } from 'vue';

import { $t } from '@vben/locales';

import { message } from 'ant-design-vue';

import { batchUpdateMenuOrder, SystemMenuApi } from '#/api/system/menu';

// 从 useVbenVxeGrid 的返回类型中提取 gridApi 的类型
type VxeGridApiType = ReturnType<typeof useVbenVxeGrid>[1];

/**
 * 菜单列表 Composable
 * 封装菜单列表的拖拽排序功能
 * 数据加载已由 proxyConfig 处理
 */
export function useMenuList() {
  // 存储拖拽开始时的源行信息
  const dragSourceRow = ref<null | SystemMenuApi.SystemMenu>(null);

  /**
   * 拖拽开始事件，保存源行信息
   */
  function handleRowDragStart(params: any) {
    const row = params?.row;
    if (row && row.id) {
      // 深拷贝保存源行信息，避免引用问题
      dragSourceRow.value = { ...row };
    }
    return true;
  }

  /**
   * 拖拽结束事件处理
   */
  function handleRowDragEnd(gridApi: VxeGridApiType, onRefresh: () => void) {
    // 先检查是否跨层级拖拽，如果是则直接返回，不执行保存
    if (!dragSourceRow.value) {
      return;
    }

    // transform: true 模式下，使用 tableData 获取扁平数据
    const flatData = (gridApi.grid?.getTableData()?.tableData ||
      []) as SystemMenuApi.SystemMenu[];

    // 查找拖拽后的源行位置
    const draggedRow = flatData.find(
      (item) => item.id === dragSourceRow.value?.id,
    );

    if (!draggedRow) {
      dragSourceRow.value = null;
      return;
    }

    const sourcePid = dragSourceRow.value.pid ?? null;
    const targetPid = draggedRow.pid ?? null;

    // 如果父级ID不同，说明跨层级了
    if (sourcePid !== targetPid) {
      message.warning('只能在同一层级内进行拖拽排序');
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
        .filter((item: SystemMenuApi.SystemMenu) => {
          const itemPid = item.pid ?? null;
          return itemPid === parentId;
        })
        .map((item: SystemMenuApi.SystemMenu) => ({
          ...item,
          children: buildTree(items, item.id),
        }));
    };

    const treeData = buildTree(flatData);

    // 递归遍历树形结构，收集所有需要更新的菜单
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
      menus.forEach((menu: SystemMenuApi.SystemMenu, index: number) => {
        const newOrder = index + 1;
        const newPid = parentId ?? menu.pid;
        const oldOrder = menu.meta?.order;
        const oldPid = menu.pid;

        // 只有当排序或父级ID发生变化时才添加到更新列表
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
          onRefresh();
        })
        .catch((error) => {
          hideLoading();
          console.error('[Menu List] 批量更新排序失败:', error);
          message.error({
            content: $t('ui.actionMessage.operationFailed'),
            key: 'drag_sort_msg',
          });
          onRefresh();
        });
    } else {
      dragSourceRow.value = null;
    }
  }

  /**
   * 创建表格事件处理器
   */
  function createGridEvents(
    gridApi: VxeGridApiType,
    onRefresh: () => void,
  ): VxeGridListeners<SystemMenuApi.SystemMenu> {
    return {
      rowDragstart: handleRowDragStart,
      rowDragend: () => handleRowDragEnd(gridApi, onRefresh),
    } as VxeGridListeners<SystemMenuApi.SystemMenu>;
  }

  return {
    createGridEvents,
  };
}
