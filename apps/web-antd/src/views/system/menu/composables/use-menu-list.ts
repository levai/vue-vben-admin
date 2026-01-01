import type { useVbenVxeGrid, VxeGridListeners } from '#/adapter/vxe-table';

import { nextTick, ref } from 'vue';

import { $t } from '@vben/locales';
import { flattenTree } from '@vben/utils';

import { message } from 'ant-design-vue';

import {
  batchUpdateMenuOrder,
  getMenuList,
  SystemMenuApi,
} from '#/api/system/menu';

// 从 useVbenVxeGrid 的返回类型中提取 gridApi 的类型
type VxeGridApiType = ReturnType<typeof useVbenVxeGrid>[1];

/**
 * 菜单列表 Composable
 * 封装菜单列表的数据加载、拖拽排序、展开状态管理等功能
 */
export function useMenuList() {
  // 使用 ref 存储扁平化的数据
  const tableData = ref<SystemMenuApi.SystemMenu[]>([]);

  // 存储拖拽开始时的源行信息
  const dragSourceRow = ref<null | SystemMenuApi.SystemMenu>(null);

  // 存储树形表格的展开状态（保存展开的节点ID列表）
  const expandedRowKeys = ref<string[]>([]);
  // 标记是否是首次加载
  const isFirstLoad = ref(true);

  /**
   * 将嵌套的树形数据转换为扁平数据（用于 transform: true）
   */
  function flattenTreeData(
    treeData: SystemMenuApi.SystemMenu[],
  ): SystemMenuApi.SystemMenu[] {
    return flattenTree<SystemMenuApi.SystemMenu>(treeData, {
      childProps: 'children',
      parentIdField: 'pid',
      idField: 'id',
      initialParentId: null,
    });
  }

  /**
   * 保存当前展开状态
   */
  function saveExpandedState(gridApi: VxeGridApiType) {
    if (!gridApi.grid) return;
    try {
      // 获取所有展开的行（transform: true 模式下使用 getTableData）
      const tableData = (gridApi.grid.getTableData()?.tableData ||
        []) as SystemMenuApi.SystemMenu[];
      const expandedRows = tableData.filter((row) => {
        // 检查行是否有子节点且已展开
        return gridApi.grid?.isTreeExpandByRow(row) === true;
      });
      expandedRowKeys.value = expandedRows.map((row) => row.id);
    } catch (error) {
      console.error('[Menu List] 保存展开状态失败:', error);
    }
  }

  /**
   * 恢复展开状态
   */
  function restoreExpandedState(gridApi: VxeGridApiType) {
    if (!gridApi.grid || expandedRowKeys.value.length === 0) return;
    try {
      // 根据保存的ID列表恢复展开状态
      const tableData = (gridApi.grid.getTableData()?.tableData ||
        []) as SystemMenuApi.SystemMenu[];
      expandedRowKeys.value.forEach((id) => {
        const row = tableData.find((item) => item.id === id);
        if (row) {
          gridApi.grid?.setTreeExpand(row, true);
        }
      });
    } catch (error) {
      console.error('[Menu List] 恢复展开状态失败:', error);
    }
  }

  /**
   * 加载数据
   */
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
   * 监听树形表格展开/折叠变化，实时保存展开状态
   */
  function handleTreeExpandChange(gridApi: VxeGridApiType) {
    setTimeout(() => {
      if (!isFirstLoad.value) {
        saveExpandedState(gridApi);
      }
    }, 50);
  }

  /**
   * 设置表格数据并恢复展开状态
   */
  function setupTableData(
    gridApi: VxeGridApiType,
    newData: SystemMenuApi.SystemMenu[],
  ) {
    gridApi.setState({
      gridOptions: {
        data: newData,
      },
    });

    // 数据更新后，等待表格渲染完成，然后恢复展开状态
    if (newData && newData.length > 0) {
      nextTick().then(() => {
        requestAnimationFrame(() => {
          if (isFirstLoad.value) {
            // 首次加载时展开所有节点
            gridApi.grid?.setAllTreeExpand(true);
            // 保存首次加载后的展开状态，并标记首次加载完成
            setTimeout(() => {
              saveExpandedState(gridApi);
              isFirstLoad.value = false;
            }, 200);
          } else {
            // 非首次加载，恢复之前的展开状态
            if (expandedRowKeys.value.length > 0) {
              restoreExpandedState(gridApi);
            } else {
              // 如果没有保存的状态，默认全部折叠
              gridApi.grid?.setAllTreeExpand(false);
            }
          }
        });
      });
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
      treeToggleExpand: () => handleTreeExpandChange(gridApi),
    } as VxeGridListeners<SystemMenuApi.SystemMenu>;
  }

  /**
   * 刷新数据（保存展开状态后刷新）
   */
  function refreshData(gridApi: VxeGridApiType) {
    saveExpandedState(gridApi);
    loadData();
  }

  return {
    tableData,
    loadData,
    refreshData,
    createGridEvents,
    setupTableData,
    saveExpandedState: (gridApi: VxeGridApiType) => saveExpandedState(gridApi),
  };
}
