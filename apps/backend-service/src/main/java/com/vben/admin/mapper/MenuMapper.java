package com.vben.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vben.admin.model.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper
 *
 * @author vben
 */
public interface MenuMapper extends BaseMapper<SysMenu> {

    /**
     * 检查菜单名称是否存在
     *
     * @param name 菜单名称
     * @param id   菜单ID（排除自己，用于更新时）
     * @return 是否存在
     */
    boolean existsByName(@Param("name") String name, @Param("id") String id);

    /**
     * 检查菜单路径是否存在
     *
     * @param path 菜单路径
     * @param id   菜单ID（排除自己，用于更新时）
     * @return 是否存在
     */
    boolean existsByPath(@Param("path") String path, @Param("id") String id);

    /**
     * 查询是否有子菜单
     *
     * @param pid 父级ID
     * @return 子菜单数量
     */
    int countByPid(@Param("pid") String pid);

    /**
     * 根据用户ID查询菜单列表（通过角色关联）
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") String userId);
}
