package com.vben.admin.core.constants;

/**
 * 系统常量
 *
 * @author vben
 */
public class SystemConstants {

    /**
     * 超级管理员角色ID（不允许删除、修改名称、禁用）
     */
    public static final String ADMIN_ROLE_ID = "1";

    /**
     * 私有构造函数，防止实例化
     */
    private SystemConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}
