package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.core.utils.SecurityUtils;
import com.vben.admin.mapper.MenuMapper;
import com.vben.admin.model.dto.MenuDTO;
import com.vben.admin.model.dto.MenuOrderDTO;
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
        // 获取当前登录用户ID
        String userId = SecurityUtils.getCurrentUserId();

        // 如果未登录，返回空列表
        if (userId == null) {
            return new ArrayList<>();
        }

        // 根据用户ID查询该用户有权限访问的菜单（通过角色关联）
        List<SysMenu> userMenus = menuMapper.selectMenusByUserId(userId);

        // 如果用户没有任何菜单权限，返回空列表
        if (userMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有需要包含的菜单ID（包括父菜单）
        Map<String, SysMenu> menuMap = new HashMap<>();
        for (SysMenu menu : userMenus) {
            menuMap.put(menu.getId(), menu);
            // 递归添加所有父菜单
            addParentMenus(menu.getPid(), menuMap);
        }

        // 转换为列表并构建树形结构
        List<SysMenu> allMenus = new ArrayList<>(menuMap.values());
        return buildMenuTree(allMenus);
    }

    /**
     * 递归添加父菜单
     *
     * @param pid     父菜单ID
     * @param menuMap 菜单映射表
     */
    private void addParentMenus(String pid, Map<String, SysMenu> menuMap) {
        // 如果父ID为空或者是根节点，则停止递归
        if (pid == null || "0".equals(pid) || menuMap.containsKey(pid)) {
            return;
        }

        // 查询父菜单
        SysMenu parentMenu = menuMapper.selectById(pid);
        if (parentMenu != null && parentMenu.getStatus() == 1 && parentMenu.getDeleted() == 0) {
            menuMap.put(parentMenu.getId(), parentMenu);
            // 继续递归添加父菜单的父菜单
            addParentMenus(parentMenu.getPid(), menuMap);
        }
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
        // 校验菜单名称
        if (menuDTO.getName() == null || menuDTO.getName().trim().isEmpty()) {
            throw new BusinessException("菜单名称不能为空");
        }
        if (menuDTO.getName().length() < 2 || menuDTO.getName().length() > 30) {
            throw new BusinessException("菜单名称长度必须在2-30之间");
        }
        // 检查名称是否已存在
        if (isNameExists(menuDTO.getName(), null)) {
            throw new BusinessException("菜单名称已存在");
        }

        // 校验菜单类型
        if (menuDTO.getType() == null || menuDTO.getType().trim().isEmpty()) {
            throw new BusinessException("菜单类型不能为空");
        }
        String menuType = menuDTO.getType();

        // 根据菜单类型进行不同的校验
        switch (menuType) {
            case "catalog":
            case "embedded":
            case "menu":
                // 这些类型需要 path
                if (menuDTO.getPath() == null || menuDTO.getPath().trim().isEmpty()) {
                    throw new BusinessException("路由路径不能为空");
                }
                if (menuDTO.getPath().length() < 2 || menuDTO.getPath().length() > 100) {
                    throw new BusinessException("路由路径长度必须在2-100之间");
                }
                if (!menuDTO.getPath().startsWith("/")) {
                    throw new BusinessException("路由路径必须以'/'开头");
                }
                // 检查路径是否已存在
                if (isPathExists(menuDTO.getPath(), null)) {
                    throw new BusinessException("菜单路径已存在");
                }
                break;
            case "button":
                // button 类型不需要 path，但需要 authCode
                if (menuDTO.getAuthCode() == null || menuDTO.getAuthCode().trim().isEmpty()) {
                    throw new BusinessException("按钮类型菜单的权限标识不能为空");
                }
                break;
            case "link":
                // link 类型不需要 path，但需要 link（在 meta.link 中）
                if (menuDTO.getMeta() == null || menuDTO.getMeta().get("link") == null) {
                    throw new BusinessException("外链类型菜单的链接地址不能为空");
                }
                break;
            default:
                throw new BusinessException("不支持的菜单类型: " + menuType);
        }

        // menu 类型需要 component
        if ("menu".equals(menuType)) {
            if (menuDTO.getComponent() == null || menuDTO.getComponent().trim().isEmpty()) {
                throw new BusinessException("菜单类型必须指定组件路径");
            }
        }

        // embedded 和 link 类型需要 linkSrc（在 meta 中）
        if ("embedded".equals(menuType) || "link".equals(menuType)) {
            if (menuDTO.getMeta() == null) {
                throw new BusinessException("菜单元数据不能为空");
            }
            String linkSrc = "embedded".equals(menuType)
                ? (String) menuDTO.getMeta().get("iframeSrc")
                : (String) menuDTO.getMeta().get("link");
            if (linkSrc == null || linkSrc.trim().isEmpty()) {
                throw new BusinessException("链接地址不能为空");
            }
            // 可以添加 URL 格式校验
            if (!linkSrc.startsWith("http://") && !linkSrc.startsWith("https://")) {
                throw new BusinessException("链接地址格式不正确，必须以 http:// 或 https:// 开头");
            }
        }

        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);

        // 处理排序字段：从 meta.order 读取，保存到实体的 rank（映射到数据库 sort_order）
        Integer sortOrder = null;
        if (menuDTO.getMeta() != null) {
            Object orderObj = menuDTO.getMeta().get("order");
            if (orderObj != null) {
                if (orderObj instanceof Number) {
                    sortOrder = ((Number) orderObj).intValue();
                } else if (orderObj instanceof String) {
                    try {
                        sortOrder = Integer.parseInt((String) orderObj);
                    } catch (NumberFormatException e) {
                        // 忽略解析错误
                    }
                }
            }
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
        // 设置排序值
        if (sortOrder != null) {
            menu.setSortOrder(sortOrder);
        } else if (menu.getSortOrder() == null) {
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

        // 校验菜单名称
        if (menuDTO.getName() == null || menuDTO.getName().trim().isEmpty()) {
            throw new BusinessException("菜单名称不能为空");
        }
        if (menuDTO.getName().length() < 2 || menuDTO.getName().length() > 30) {
            throw new BusinessException("菜单名称长度必须在2-30之间");
        }
        // 检查名称是否已存在（排除自己）
        if (isNameExists(menuDTO.getName(), id)) {
            throw new BusinessException("菜单名称已存在");
        }

        // 校验菜单类型
        if (menuDTO.getType() == null || menuDTO.getType().trim().isEmpty()) {
            throw new BusinessException("菜单类型不能为空");
        }
        String menuType = menuDTO.getType();

        // 根据菜单类型进行不同的校验
        switch (menuType) {
            case "catalog":
            case "embedded":
            case "menu":
                // 这些类型需要 path
                if (menuDTO.getPath() == null || menuDTO.getPath().trim().isEmpty()) {
                    throw new BusinessException("路由路径不能为空");
                }
                if (menuDTO.getPath().length() < 2 || menuDTO.getPath().length() > 100) {
                    throw new BusinessException("路由路径长度必须在2-100之间");
                }
                if (!menuDTO.getPath().startsWith("/")) {
                    throw new BusinessException("路由路径必须以'/'开头");
                }
                // 检查路径是否已存在（排除自己）
                if (isPathExists(menuDTO.getPath(), id)) {
                    throw new BusinessException("菜单路径已存在");
                }
                break;
            case "button":
                // button 类型不需要 path，但需要 authCode
                if (menuDTO.getAuthCode() == null || menuDTO.getAuthCode().trim().isEmpty()) {
                    throw new BusinessException("按钮类型菜单的权限标识不能为空");
                }
                break;
            case "link":
                // link 类型不需要 path，但需要 link（在 meta.link 中）
                if (menuDTO.getMeta() == null || menuDTO.getMeta().get("link") == null) {
                    throw new BusinessException("外链类型菜单的链接地址不能为空");
                }
                break;
            default:
                throw new BusinessException("不支持的菜单类型: " + menuType);
        }

        // menu 类型需要 component
        if ("menu".equals(menuType)) {
            if (menuDTO.getComponent() == null || menuDTO.getComponent().trim().isEmpty()) {
                throw new BusinessException("菜单类型必须指定组件路径");
            }
        }

        // embedded 和 link 类型需要 linkSrc（在 meta 中）
        if ("embedded".equals(menuType) || "link".equals(menuType)) {
            if (menuDTO.getMeta() == null) {
                throw new BusinessException("菜单元数据不能为空");
            }
            String linkSrc = "embedded".equals(menuType)
                ? (String) menuDTO.getMeta().get("iframeSrc")
                : (String) menuDTO.getMeta().get("link");
            if (linkSrc == null || linkSrc.trim().isEmpty()) {
                throw new BusinessException("链接地址不能为空");
            }
            // 可以添加 URL 格式校验
            if (!linkSrc.startsWith("http://") && !linkSrc.startsWith("https://")) {
                throw new BusinessException("链接地址格式不正确，必须以 http:// 或 https:// 开头");
            }
        }

        BeanUtils.copyProperties(menuDTO, menu, "id");

        // 处理排序字段：从 meta.order 读取，保存到实体的 rank（映射到数据库 sort_order）
        Integer sortOrder = null;
        if (menuDTO.getMeta() != null) {
            Object orderObj = menuDTO.getMeta().get("order");
            if (orderObj != null) {
                if (orderObj instanceof Number) {
                    sortOrder = ((Number) orderObj).intValue();
                } else if (orderObj instanceof String) {
                    try {
                        sortOrder = Integer.parseInt((String) orderObj);
                    } catch (NumberFormatException e) {
                        // 忽略解析错误
                    }
                }
            }
            try {
                menu.setMeta(objectMapper.writeValueAsString(menuDTO.getMeta()));
            } catch (Exception e) {
                throw new BusinessException("菜单元数据格式错误");
            }
        }

        if (menu.getPid() == null) {
            menu.setPid("0");
        }
        // 设置排序值
        if (sortOrder != null) {
            menu.setSortOrder(sortOrder);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateMenuOrder(List<MenuOrderDTO> menus) {
        // 参数校验
        if (menus == null || menus.isEmpty()) {
            throw new BusinessException("菜单列表不能为空");
        }

        // 检查是否有重复的菜单ID
        long distinctCount = menus.stream()
                .filter(menu -> menu.getId() != null)
                .map(MenuOrderDTO::getId)
                .distinct()
                .count();
        if (distinctCount != menus.size()) {
            throw new BusinessException("菜单列表中存在重复的菜单ID");
        }

        for (MenuOrderDTO menuOrderDTO : menus) {
            // 参数校验
            if (menuOrderDTO.getId() == null || menuOrderDTO.getId().trim().isEmpty()) {
                throw new BusinessException("菜单ID不能为空");
            }

            if (menuOrderDTO.getMeta() == null) {
                throw new BusinessException("菜单元数据不能为空，菜单ID: " + menuOrderDTO.getId());
            }

            // 检查菜单是否存在
            SysMenu menu = menuMapper.selectById(menuOrderDTO.getId());
            if (menu == null) {
                throw new BusinessException("菜单不存在，ID: " + menuOrderDTO.getId());
            }

            // 处理父级ID
            String pid = menuOrderDTO.getPid();
            if (pid == null || pid.isEmpty() || "null".equalsIgnoreCase(pid)) {
                pid = "0";
            }

            // 校验：防止菜单成为自己的父级（循环引用）
            if (menuOrderDTO.getId().equals(pid)) {
                throw new BusinessException("菜单不能成为自己的父级，菜单ID: " + menuOrderDTO.getId());
            }

            // 校验：如果 pid 不是 "0"，检查父菜单是否存在
            if (!"0".equals(pid)) {
                SysMenu parentMenu = menuMapper.selectById(pid);
                if (parentMenu == null) {
                    throw new BusinessException("父菜单不存在，父菜单ID: " + pid + "，菜单ID: " + menuOrderDTO.getId());
                }
            }

            // 处理排序字段：从 meta.order 读取
            Integer sortOrder = null;
            Object orderObj = menuOrderDTO.getMeta().get("order");
            if (orderObj != null) {
                if (orderObj instanceof Number) {
                    sortOrder = ((Number) orderObj).intValue();
                } else if (orderObj instanceof String) {
                    try {
                        sortOrder = Integer.parseInt((String) orderObj);
                    } catch (NumberFormatException e) {
                        throw new BusinessException("排序值格式错误，必须是数字，菜单ID: " + menuOrderDTO.getId());
                    }
                } else {
                    throw new BusinessException("排序值格式错误，必须是数字，菜单ID: " + menuOrderDTO.getId());
                }

                // 校验排序值范围（可选，根据业务需求调整）
                if (sortOrder < 0) {
                    throw new BusinessException("排序值不能小于0，菜单ID: " + menuOrderDTO.getId());
                }
            } else {
                throw new BusinessException("排序值不能为空，菜单ID: " + menuOrderDTO.getId());
            }

            // 更新父级ID
            menu.setPid(pid);

            // 更新 meta 字段（保留原有的 meta 数据，只更新 order）
            try {
                Map<String, Object> existingMeta = new HashMap<>();
                if (menu.getMeta() != null && !menu.getMeta().isEmpty()) {
                    existingMeta = objectMapper.readValue(menu.getMeta(), new TypeReference<Map<String, Object>>() {});
                }
                // 合并新的 meta 数据
                existingMeta.putAll(menuOrderDTO.getMeta());
                menu.setMeta(objectMapper.writeValueAsString(existingMeta));
            } catch (Exception e) {
                throw new BusinessException("菜单元数据格式错误，菜单ID: " + menuOrderDTO.getId() + "，错误: " + e.getMessage());
            }

            // 设置排序值
            menu.setSortOrder(sortOrder);

            menuMapper.updateById(menu);
        }
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

        // 对根菜单和子菜单进行排序
        sortMenuTree(rootMenus);

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

        // 将排序值放到 meta.order 中（从数据库 sort_order 字段读取）
        if (menu.getSortOrder() != null) {
            meta.put("order", menu.getSortOrder());
        }

        vo.setMeta(meta);

        // 设置状态字段（前端需要）
        vo.setStatus(menu.getStatus());

        // name 字段直接使用数据库中的值（路由名称，由前端提交）
        // meta.title 字段用于菜单显示名称（中文，由前端提交）
        // meta.order 字段用于排序（从数据库 sort_order 字段读取，通过实体的 rank 字段）
        // 不需要转换，直接返回

        return vo;
    }

    /**
     * 递归排序菜单树
     */
    private void sortMenuTree(List<MenuVO> menus) {
        if (menus == null || menus.isEmpty()) {
            return;
        }
        // 按 meta.order 排序（如果 meta 为 null 或 order 不存在，则默认为 0）
        menus.sort((m1, m2) -> {
            Integer order1 = getMenuOrder(m1);
            Integer order2 = getMenuOrder(m2);
            return Integer.compare(order1, order2);
        });
        // 递归排序子菜单
        for (MenuVO menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                sortMenuTree(menu.getChildren());
            }
        }
    }

    /**
     * 获取菜单排序值
     */
    private Integer getMenuOrder(MenuVO menu) {
        if (menu.getMeta() != null && menu.getMeta().get("order") != null) {
            Object orderObj = menu.getMeta().get("order");
            if (orderObj instanceof Number) {
                return ((Number) orderObj).intValue();
            } else if (orderObj instanceof String) {
                try {
                    return Integer.parseInt((String) orderObj);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
