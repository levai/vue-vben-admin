package com.vben.admin.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作模块枚举
 *
 * @author vben
 */
public enum OperationModule {

    /**
     * 系统管理
     */
    SYSTEM("系统管理"),

    /**
     * 用户管理
     */
    USER("用户管理"),

    /**
     * 角色管理
     */
    ROLE("角色管理"),

    /**
     * 菜单管理
     */
    MENU("菜单管理"),

    /**
     * 部门管理
     */
    DEPT("部门管理"),

    /**
     * 权限管理
     */
    PERMISSION("权限管理"),

    /**
     * 操作日志
     */
    OPERATION_LOG("操作日志");

    private final String label;

    OperationModule(String label) {
        this.label = label;
    }

    /**
     * 获取标签
     *
     * @return 标签
     */
    public String getLabel() {
        return label;
    }

    /**
     * 获取所有操作模块的标签列表
     *
     * @return 标签列表
     */
    public static List<String> getAllLabels() {
        return Arrays.stream(values())
                .map(OperationModule::getLabel)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 根据标签获取枚举
     *
     * @param label 标签
     * @return 枚举值，如果不存在则返回 null
     */
    public static OperationModule fromLabel(String label) {
        if (label == null || label.isEmpty()) {
            return null;
        }
        for (OperationModule module : values()) {
            if (module.getLabel().equals(label)) {
                return module;
            }
        }
        return null;
    }

    /**
     * 根据英文模块名转换为中文标签
     *
     * @param moduleName 英文模块名（如 "system", "user"）
     * @return 中文标签，如果找不到则返回原名称
     */
    public static String convertFromEnglish(String moduleName) {
        if (moduleName == null || moduleName.isEmpty()) {
            return "";
        }

        String lowerName = moduleName.toLowerCase();
        switch (lowerName) {
            case "system":
                return SYSTEM.getLabel();
            case "user":
            case "users":
                return USER.getLabel();
            case "role":
            case "roles":
                return ROLE.getLabel();
            case "menu":
            case "menus":
                return MENU.getLabel();
            case "dept":
            case "depts":
                return DEPT.getLabel();
            case "permission":
            case "permissions":
                return PERMISSION.getLabel();
            case "operation-log":
            case "operationlog":
                return OPERATION_LOG.getLabel();
            default:
                return moduleName;
        }
    }
}
