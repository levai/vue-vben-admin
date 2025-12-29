package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.mapper.MenuMapper;
import com.vben.admin.model.dto.MenuDTO;
import com.vben.admin.model.entity.SysMenu;
import com.vben.admin.model.vo.MenuVO;
import com.vben.admin.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author vben
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<MenuVO> getAllMenus() {
        List<SysMenu> menus = menuMapper.selectList(
                new QueryWrapper<SysMenu>()
                        .eq("status", 1)
                        .orderByAsc("sort_order")
        );
        return buildMenuTree(menus);
    }

    @Override
    public List<MenuVO> getMenuList() {
        List<SysMenu> menus = menuMapper.selectList(
                new QueryWrapper<SysMenu>()
                        .orderByAsc("sort_order")
        );
        return buildMenuTree(menus);
    }

    @Override
    public boolean isNameExists(String name, String id) {
        return menuMapper.existsByName(name, id);
    }

    @Override
    public boolean isPathExists(String path, String id) {
        return menuMapper.existsByPath(path, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createMenu(MenuDTO menuDTO) {
        // 检查名称是否已存在
        if (isNameExists(menuDTO.getName(), null)) {
            throw new BusinessException("菜单名称已存在");
        }

        // 检查路径是否已存在
        if (isPathExists(menuDTO.getPath(), null)) {
            throw new BusinessException("菜单路径已存在");
        }

        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        if (menuDTO.getMeta() != null) {
            try {
                menu.setMeta(objectMapper.writeValueAsString(menuDTO.getMeta()));
            } catch (Exception e) {
                throw new BusinessException("菜单元数据格式错误");
            }
        }
        if (menu.getPid() == null) {
            menu.setPid("0");
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }

        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(String id, MenuDTO menuDTO) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 检查名称是否已存在（排除自己）
        if (isNameExists(menuDTO.getName(), id)) {
            throw new BusinessException("菜单名称已存在");
        }

        // 检查路径是否已存在（排除自己）
        if (isPathExists(menuDTO.getPath(), id)) {
            throw new BusinessException("菜单路径已存在");
        }

        BeanUtils.copyProperties(menuDTO, menu, "id");
        if (menuDTO.getMeta() != null) {
            try {
                menu.setMeta(objectMapper.writeValueAsString(menuDTO.getMeta()));
            } catch (Exception e) {
                throw new BusinessException("菜单元数据格式错误");
            }
        }
        if (menu.getPid() == null) {
            menu.setPid("0");
        }

        menuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(String id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 检查是否有子菜单
        int childCount = menuMapper.countByPid(id);
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，无法删除");
        }

        menuMapper.deleteById(id);
    }

    /**
     * 构建菜单树
     */
    private List<MenuVO> buildMenuTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为VO
        List<MenuVO> menuVOs = menus.stream().map(this::convertToVO).collect(Collectors.toList());

        // 构建树形结构
        List<MenuVO> rootMenus = new ArrayList<>();
        Map<String, MenuVO> menuMap = menuVOs.stream()
                .collect(Collectors.toMap(MenuVO::getId, menu -> menu));

        for (MenuVO menu : menuVOs) {
            if ("0".equals(menu.getPid()) || menu.getPid() == null) {
                rootMenus.add(menu);
            } else {
                MenuVO parent = menuMap.get(menu.getPid());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }

        return rootMenus;
    }

    /**
     * 转换为VO
     */
    private MenuVO convertToVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);

        // 解析meta JSON
        Map<String, Object> meta = new HashMap<>();
        if (StringUtils.hasText(menu.getMeta())) {
            try {
                meta = objectMapper.readValue(menu.getMeta(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        vo.setMeta(meta);

        // 设置状态字段（前端需要）
        vo.setStatus(menu.getStatus());

        // name 字段直接使用数据库中的值（路由名称，由前端提交）
        // meta.title 字段用于菜单显示名称（中文，由前端提交）
        // 不需要转换，直接返回

        return vo;
    }
}
