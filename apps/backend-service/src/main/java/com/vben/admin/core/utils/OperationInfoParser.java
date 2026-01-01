package com.vben.admin.core.utils;

import com.vben.admin.core.enums.OperationType;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 操作信息解析工具
 *
 * @author vben
 */
public class OperationInfoParser {

    /**
     * 操作信息
     */
    @Data
    public static class OperationInfo {
        /**
         * 操作类型
         */
        private String operationType;
        /**
         * 操作模块
         */
        private String operationModule;
        /**
         * 操作页面（前端页面路径）
         */
        private String operationPage;
    }

    /**
     * 根据URL和方法解析操作信息（使用菜单模块解析器，动态从菜单树获取）
     *
     * @param requestUrl        请求URL（后端API路径）
     * @param requestMethod     请求方法
     * @param pageUrl           前端页面URL（从Header获取，可为null）
     * @param menuModuleResolver 菜单模块解析器（必须提供，用于动态获取模块信息）
     * @return 操作信息
     */
    public static OperationInfo parseOperationInfo(String requestUrl, String requestMethod, String pageUrl,
                                                   MenuModuleResolver menuModuleResolver) {
        OperationInfo info = new OperationInfo();

        if (requestUrl == null) {
            return info;
        }

        // 设置操作页面：优先使用前端页面URL，否则从API路径推断
        String finalPageUrl = null;
        if (pageUrl != null && !pageUrl.isEmpty()) {
            finalPageUrl = pageUrl;
            info.setOperationPage(pageUrl);
        } else {
            finalPageUrl = inferPageUrlFromApi(requestUrl);
            info.setOperationPage(finalPageUrl);
        }

        // 解析操作模块：从前端页面URL推断（而不是从后端API URL），直接使用菜单名称
        // 优先根据页面路径匹配菜单，如果找到菜单，直接使用菜单的 title 作为操作模块
        // 如果找不到菜单，使用页面路径作为模块名（降级方案）
        // 例如：
        // - /dashboard/analytics -> 匹配菜单 path="/analytics" -> 使用菜单 title "数据分析"
        // - /system/user -> 匹配菜单 path="/system/user" -> 使用菜单 title "用户管理"
        String moduleName = null;
        if (menuModuleResolver != null) {
            if (finalPageUrl != null && !finalPageUrl.isEmpty()) {
                moduleName = menuModuleResolver.extractModuleFromPath(finalPageUrl);
            } else {
                // 如果没有前端页面URL，降级使用后端API URL推断
                moduleName = menuModuleResolver.extractModuleFromPath(requestUrl);
            }
        }

        if (moduleName != null && !moduleName.isEmpty()) {
            // 直接使用菜单名称作为操作模块（不再需要标准化）
            info.setOperationModule(moduleName);
        }

        // 根据请求方法和URL推断操作类型
        String operationType = inferOperationType(requestUrl, requestMethod);
        info.setOperationType(operationType);

        return info;
    }

    /**
     * 从API路径推断前端页面URL（去掉API后缀）
     */
    private static String inferPageUrlFromApi(String apiUrl) {
        if (apiUrl == null || apiUrl.isEmpty()) {
            return "";
        }

        // 去掉常见的API后缀
        String pageUrl = apiUrl;

        // 去掉末尾的 /types, /list, /detail, /add, /edit, /delete 等
        pageUrl = pageUrl.replaceAll("/(types|list|detail|add|edit|delete|export|import|download|upload)(/.*)?$", "");

        // 去掉末尾的ID（数字）
        pageUrl = pageUrl.replaceAll("/\\d+$", "");

        // 如果处理后为空，返回原路径
        if (pageUrl.isEmpty()) {
            return apiUrl;
        }

        return pageUrl;
    }

    /**
     * 推断操作类型（返回英文值，小写）
     */
    private static String inferOperationType(String requestUrl, String requestMethod) {
        if (requestMethod == null) {
            return OperationType.VIEW.name().toLowerCase();
        }

        String method = requestMethod.toUpperCase();
        String url = requestUrl.toLowerCase();

        // 特殊路径判断
        if (url.contains("/login")) {
            return OperationType.LOGIN.name().toLowerCase();
        }
        if (url.contains("/logout")) {
            return OperationType.LOGOUT.name().toLowerCase();
        }
        if (url.contains("/export")) {
            return OperationType.EXPORT.name().toLowerCase();
        }
        if (url.contains("/import")) {
            return OperationType.IMPORT.name().toLowerCase();
        }
        if (url.contains("/download")) {
            return OperationType.DOWNLOAD.name().toLowerCase();
        }
        if (url.contains("/upload")) {
            return OperationType.UPLOAD.name().toLowerCase();
        }

        // 根据HTTP方法推断
        switch (method) {
            case "GET":
                return OperationType.VIEW.name().toLowerCase();
            case "POST":
                return OperationType.ADD.name().toLowerCase();
            case "PUT":
            case "PATCH":
                return OperationType.EDIT.name().toLowerCase();
            case "DELETE":
                return OperationType.DELETE.name().toLowerCase();
            default:
                return OperationType.VIEW.name().toLowerCase();
        }
    }
}
