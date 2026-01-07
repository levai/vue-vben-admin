package com.vben.admin.core.utils;

import com.vben.admin.model.vo.MenuVO;
import com.vben.admin.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单模块解析器
 * 从菜单树动态获取操作模块信息，而不是硬编码枚举
 *
 * @author vben
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuModuleResolver {

    private final MenuService menuService;

    /**
     * 模块信息缓存（路径 -> 模块信息）
     * key: 模块路径（如 "user", "dashboard"）
     * value: 模块中文名称（如 "用户管理", "仪表盘"）
     */
    private Map<String, String> moduleCache = new HashMap<>();
    private long cacheTimestamp = 0;
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟缓存

    /**
     * 根据完整页面路径获取模块中文名称
     * 例如：/dashboard/analytics -> "数据分析", /system/user -> "用户管理"
     * 优先匹配最具体的路径（子菜单），如果找不到则使用页面路径作为模块名称
     *
     * @param fullPagePath 完整页面路径（如 "/dashboard/analytics", "/system/user"）
     * @param modulePath    模块路径（如 "dashboard", "user"），作为降级方案
     * @return 模块中文名称，如果找不到菜单则返回格式化后的页面路径
     */
    public String getModuleLabelByPagePath(String fullPagePath, String modulePath) {
        if (fullPagePath == null || fullPagePath.isEmpty()) {
            // 如果没有完整路径，降级使用模块路径
            return getModuleLabel(modulePath);
        }

        // 特殊处理：个人中心（不在菜单树中）
        if (fullPagePath.equals("/profile") || "profile".equals(normalizeModulePath(modulePath))) {
            return "个人中心";
        }

        // 刷新缓存（如果过期）
        refreshCacheIfNeeded();

        // 从缓存中查找（使用完整路径作为key）
        String cacheKey = fullPagePath.toLowerCase();
        String label = moduleCache.get(cacheKey);
        if (label != null) {
            return label;
        }

        // 优先根据完整路径查找菜单（会找到最具体的子菜单）
        label = findMenuLabelByPath(fullPagePath);
        if (label != null) {
            // 更新缓存
            moduleCache.put(cacheKey, label);
            return label;
        }

        // 如果完整路径找不到，降级使用模块路径
        label = getModuleLabel(modulePath);
        if (label != null && !label.equals(modulePath)) {
            // 如果模块路径找到了菜单，返回菜单名称
            return label;
        }

        // 如果都没找到，直接使用页面路径作为模块名称（格式化后）
        return formatPagePathAsModuleName(fullPagePath);
    }

    /**
     * 根据模块路径获取模块中文名称（降级方案）
     * 例如：user -> "用户管理", dashboard -> "仪表盘"
     *
     * @param modulePath 模块路径（如 "user", "dashboard", "operation-log"）
     * @return 模块中文名称，如果找不到则返回原路径
     */
    public String getModuleLabel(String modulePath) {
        if (modulePath == null || modulePath.isEmpty()) {
            return "";
        }

        String normalizedPath = normalizeModulePath(modulePath);

        // 特殊处理：个人中心（不在菜单树中）
        if ("profile".equals(normalizedPath)) {
            return "个人中心";
        }

        // 刷新缓存（如果过期）
        refreshCacheIfNeeded();

        // 从缓存中查找
        String label = moduleCache.get(normalizedPath);
        if (label != null) {
            return label;
        }

        // 如果缓存中没有，尝试从菜单树中查找
        label = findModuleLabelFromMenuTree(normalizedPath);
        if (label != null) {
            // 更新缓存
            moduleCache.put(normalizedPath, label);
            return label;
        }

        // 如果找不到，返回格式化后的模块路径
        log.debug("未找到模块路径对应的菜单: {}", modulePath);
        return formatModulePathAsName(modulePath);
    }

    /**
     * 将页面路径格式化为模块名称
     * 例如：/dashboard/analytics -> "dashboard/analytics"
     *      /system/user -> "system/user"
     *
     * @param pagePath 页面路径
     * @return 格式化后的模块名称
     */
    private String formatPagePathAsModuleName(String pagePath) {
        if (pagePath == null || pagePath.isEmpty()) {
            return "";
        }
        // 去掉开头的斜杠
        String cleanPath = pagePath.startsWith("/") ? pagePath.substring(1) : pagePath;
        // 如果为空，返回原路径
        if (cleanPath.isEmpty()) {
            return pagePath;
        }
        return cleanPath;
    }

    /**
     * 将模块路径格式化为模块名称
     * 例如：operation-log -> "operation-log"
     *      user -> "user"
     *
     * @param modulePath 模块路径
     * @return 格式化后的模块名称
     */
    private String formatModulePathAsName(String modulePath) {
        if (modulePath == null || modulePath.isEmpty()) {
            return "";
        }
        return modulePath;
    }

    /**
     * 根据完整路径查找菜单标签（会找到最具体的子菜单）
     *
     * @param pagePath 完整页面路径（如 "/dashboard/analytics"）
     * @return 菜单中文名称，如果找不到则返回 null
     */
    private String findMenuLabelByPath(String pagePath) {
        try {
            // 1. 先尝试完整路径（如 "/dashboard/analytics"）
            String menuNameChain = menuService.getMenuNameChainByPath(pagePath);
            if (menuNameChain != null && !menuNameChain.isEmpty()) {
                // 取名称链的最后一个（最具体的子菜单名称）
                String[] parts = menuNameChain.split(" - ");
                if (parts.length > 0) {
                    return parts[parts.length - 1];
                }
            }

            // 2. 如果完整路径找不到，尝试提取最后一部分路径（如 "/analytics"）
            // 因为菜单表中的 path 可能是相对路径，而不是完整路径
            if (pagePath.contains("/")) {
                String lastPart = "/" + pagePath.substring(pagePath.lastIndexOf("/") + 1);
                if (!lastPart.equals(pagePath)) { // 避免重复查询
                    menuNameChain = menuService.getMenuNameChainByPath(lastPart);
                    if (menuNameChain != null && !menuNameChain.isEmpty()) {
                        String[] parts = menuNameChain.split(" - ");
                        if (parts.length > 0) {
                            return parts[parts.length - 1];
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("根据路径查找菜单失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 根据前端页面路径获取模块名称（直接使用菜单名称，而不是从路径提取）
     * 规则：
     * 1. 优先根据完整路径匹配菜单，如果找到菜单，直接使用菜单的 title 作为模块名
     * 2. 如果找不到，尝试最后一部分路径匹配菜单（因为菜单 path 可能是相对路径）
     * 3. 如果还找不到，使用页面路径作为模块名（降级方案）
     *
     * 例如：
     * - /dashboard/analytics -> 匹配菜单 path="/analytics" -> 返回菜单 title "数据分析"
     * - /system/user -> 匹配菜单 path="/system/user" -> 返回菜单 title "用户管理"
     * - /system/operation-log -> 匹配菜单 path="/system/operation-log" -> 返回菜单 title "操作日志"
     *
     * @param pagePath 前端页面路径
     * @return 模块名称（菜单 title 或页面路径）
     */
    public String extractModuleFromPath(String pagePath) {
        if (pagePath == null || pagePath.isEmpty()) {
            return null;
        }

        // 1. 优先根据完整路径匹配菜单，获取菜单名称
        String menuTitle = findMenuTitleByPagePath(pagePath);
        if (menuTitle != null && !menuTitle.isEmpty()) {
            return menuTitle;
        }

        // 2. 如果找不到菜单，使用页面路径作为模块名（降级方案）
        return formatPagePathAsModuleName(pagePath);
    }

    /**
     * 根据页面路径查找菜单名称（从菜单树中查找）
     * 例如：/dashboard/analytics -> 可能匹配到菜单 path="/analytics" -> 返回菜单 title "数据分析"
     *
     * @param pagePath 页面路径
     * @return 菜单名称（title），如果找不到则返回 null
     */
    private String findMenuTitleByPagePath(String pagePath) {
        try {
            // 从菜单树中查找匹配的菜单
            List<MenuVO> menuTree = menuService.getMenuList(null);
            MenuVO menu = findMenuInTree(menuTree, pagePath);
            if (menu != null) {
                return extractMenuTitle(menu);
            }

            // 如果完整路径找不到，尝试最后一部分路径（如 /analytics）
            // 因为子菜单的 path 可能是相对路径，而不是完整路径
            if (pagePath.contains("/")) {
                String lastPart = "/" + pagePath.substring(pagePath.lastIndexOf("/") + 1);
                if (!lastPart.equals(pagePath)) {
                    menu = findMenuInTree(menuTree, lastPart);
                    if (menu != null) {
                        return extractMenuTitle(menu);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("根据页面路径查找菜单失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 在菜单树中递归查找匹配的菜单
     *
     * @param menus    菜单列表
     * @param pagePath 页面路径
     * @return 菜单对象，如果找不到则返回 null
     */
    private MenuVO findMenuInTree(List<MenuVO> menus, String pagePath) {
        if (menus == null || menus.isEmpty()) {
            return null;
        }

        for (MenuVO menu : menus) {
            // 排除 button 类型
            if ("button".equals(menu.getType())) {
                continue;
            }

            // 如果菜单 path 匹配页面路径，返回菜单对象
            if (menu.getPath() != null && menu.getPath().equals(pagePath)) {
                return menu;
            }

            // 递归查找子菜单
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                MenuVO foundMenu = findMenuInTree(menu.getChildren(), pagePath);
                if (foundMenu != null) {
                    return foundMenu;
                }
            }
        }

        return null;
    }

    /**
     * 提取菜单标题
     *
     * @param menu 菜单对象
     * @return 菜单标题（优先使用 meta.title，否则使用 name）
     */
    private String extractMenuTitle(MenuVO menu) {
        // 优先从 meta.title 获取
        if (menu.getMeta() != null && menu.getMeta().get("title") != null) {
            return menu.getMeta().get("title").toString();
        }
        // 如果没有 title，使用 name
        return menu.getName();
    }

    /**
     * 将模块路径转换为存储格式（小写，下划线转横线）
     * 例如：operation_log -> operation-log
     *
     * @param modulePath 模块路径
     * @return 转换后的模块路径
     */
    public String normalizeModulePath(String modulePath) {
        if (modulePath == null || modulePath.isEmpty()) {
            return "";
        }
        return modulePath.toLowerCase().replace("_", "-");
    }

    /**
     * 刷新缓存（如果过期）
     */
    private void refreshCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - cacheTimestamp > CACHE_EXPIRE_TIME) {
            moduleCache.clear();
            cacheTimestamp = currentTime;
        }
    }

    /**
     * 从菜单树中查找模块标签
     *
     * @param modulePath 模块路径
     * @return 模块中文名称，如果找不到则返回 null
     */
    private String findModuleLabelFromMenuTree(String modulePath) {
        try {
            List<MenuVO> menuTree = menuService.getMenuList(null);
            return findModuleLabelInTree(menuTree, modulePath);
        } catch (Exception e) {
            log.warn("从菜单树查找模块失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 在菜单树中递归查找模块标签
     *
     * @param menus      菜单列表
     * @param modulePath 模块路径
     * @return 模块中文名称，如果找不到则返回 null
     */
    private String findModuleLabelInTree(List<MenuVO> menus, String modulePath) {
        if (menus == null || menus.isEmpty()) {
            return null;
        }

        String normalizedModulePath = normalizeModulePath(modulePath);

        for (MenuVO menu : menus) {
            // 排除 button 类型
            if ("button".equals(menu.getType())) {
                continue;
            }

            // 从菜单路径提取模块名
            String menuModulePath = extractModuleFromPath(menu.getPath());
            if (menuModulePath != null && normalizedModulePath.equals(normalizeModulePath(menuModulePath))) {
                // 找到匹配的菜单，返回菜单标题
                return extractMenuTitle(menu);
            }

            // 递归查找子菜单
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                String label = findModuleLabelInTree(menu.getChildren(), modulePath);
                if (label != null) {
                    return label;
                }
            }
        }

        return null;
    }
}
