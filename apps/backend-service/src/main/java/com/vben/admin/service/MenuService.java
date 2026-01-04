package com.vben.admin.service;

import com.vben.admin.core.validation.ValidId;
import com.vben.admin.model.dto.MenuDTO;
import com.vben.admin.model.dto.MenuOrderDTO;
import com.vben.admin.model.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author vben
 */
public interface MenuService {

    /**
     * 获取所有菜单（用于路由）
     *
     * @return 菜单列表（树形结构）
     */
    List<MenuVO> getAllMenus();

    /**
     * 获取菜单列表
     *
     * @param status 状态（0-禁用，1-启用，null-所有状态）
     * @return 菜单列表
     */
    List<MenuVO> getMenuList(Integer status);

    /**
     * 检查菜单名称是否存在
     *
     * @param name 菜单名称
     * @param id   菜单ID（可选，用于更新时排除自己）
     * @return 是否存在
     */
    boolean isNameExists(String name, @ValidId(message = "菜单ID不能为空或无效值") String id);

    /**
     * 检查菜单路径是否存在
     *
     * @param path 菜单路径
     * @param id   菜单ID（可选，用于更新时排除自己）
     * @return 是否存在
     */
    boolean isPathExists(String path, @ValidId(message = "菜单ID不能为空或无效值") String id);

    /**
     * 创建菜单
     *
     * @param menuDTO 菜单信息
     * @return 菜单ID
     */
    String createMenu(MenuDTO menuDTO);

    /**
     * 更新菜单
     *
     * @param id      菜单ID
     * @param menuDTO 菜单信息
     */
    void updateMenu(@ValidId(message = "菜单ID不能为空或无效值") String id, MenuDTO menuDTO);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void deleteMenu(@ValidId(message = "菜单ID不能为空或无效值") String id);

    /**
     * 批量更新菜单排序
     *
     * @param menus 菜单列表（包含 id、meta.order 和 pid）
     */
    void batchUpdateMenuOrder(List<MenuOrderDTO> menus);

    /**
     * 根据路径获取菜单名称链（父菜单 - 子菜单）
     * 例如：/system/operation-log -> "系统管理 - 操作日志"
     *
     * @param path 菜单路径
     * @return 菜单名称链，如果找不到菜单则返回 null
     */
    String getMenuNameChainByPath(String path);
}
