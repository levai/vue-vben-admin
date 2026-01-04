package com.vben.admin.core.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 校验工具类
 * 用于统一处理字符串校验，拦截 "null"、"undefined"、空格等无效字符串
 *
 * <p>使用 Apache Commons Lang3 的 StringUtils 进行字符串处理</p>
 *
 * @author vben
 */
public class ValidationUtils {

    /**
     * 无效字符串列表（这些字符串应该被视为无效值）
     */
    private static final String[] INVALID_STRINGS = {
            "null",
            "undefined",
            "NULL",
            "UNDEFINED",
            "Null",
            "Undefined"
    };

    /**
     * 检查字符串是否有效（不为 null、不为空、不是无效字符串）
     *
     * <p>使用 Apache Commons Lang3 的 StringUtils.isBlank() 检查空白字符串</p>
     *
     * @param str 待检查的字符串
     * @return true 如果字符串有效，false 如果字符串无效
     */
    public static boolean isValidString(String str) {
        // 使用 Apache Commons Lang3 的 isBlank 检查 null、空字符串和空白字符
        if (StringUtils.isBlank(str)) {
            return false;
        }

        // 去除首尾空格（StringUtils.trim 是 null 安全的）
        String trimmed = StringUtils.trim(str);
        // trimmed 不会为 null（因为 isBlank 已经检查过），但为了代码健壮性保留检查
        if (StringUtils.isBlank(trimmed)) {
            return false;
        }

        // 检查是否是无效字符串（"null"、"undefined" 等）
        for (String invalid : INVALID_STRINGS) {
            if (invalid.equals(trimmed)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查字符串是否无效（为 null、为空、是无效字符串）
     *
     * @param str 待检查的字符串
     * @return true 如果字符串无效，false 如果字符串有效
     */
    public static boolean isInvalidString(String str) {
        return !isValidString(str);
    }

    /**
     * 验证字符串是否有效，如果无效则抛出异常
     *
     * @param str     待检查的字符串
     * @param fieldName 字段名称（用于错误提示）
     * @throws IllegalArgumentException 如果字符串无效
     */
    public static void requireValidString(String str, String fieldName) {
        if (isInvalidString(str)) {
            throw new IllegalArgumentException(fieldName + "不能为空或无效值");
        }
    }

    /**
     * 清理字符串（去除首尾空格，如果是无效字符串则返回 null）
     *
     * <p>使用 Apache Commons Lang3 的 StringUtils.trim() 安全地去除空格</p>
     *
     * @param str 待清理的字符串
     * @return 清理后的字符串，如果是无效字符串则返回 null
     */
    public static String cleanString(String str) {
        // 使用 Apache Commons Lang3 的 trim，null 安全
        String trimmed = StringUtils.trim(str);
        if (StringUtils.isBlank(trimmed)) {
            return null;
        }

        // 检查是否是无效字符串
        for (String invalid : INVALID_STRINGS) {
            if (invalid.equals(trimmed)) {
                return null;
            }
        }

        return trimmed;
    }

    /**
     * 验证ID是否有效（用于验证用户ID、角色ID、菜单ID等）
     *
     * @param id ID字符串
     * @param fieldName 字段名称（用于错误提示）
     * @throws IllegalArgumentException 如果ID无效
     */
    public static void requireValidId(String id, String fieldName) {
        if (isInvalidString(id)) {
            throw new IllegalArgumentException(fieldName + "不能为空或无效值");
        }
    }

    /**
     * 验证ID是否有效（用于验证用户ID、角色ID、菜单ID等）
     *
     * @param id ID字符串
     * @return true 如果ID有效，false 如果ID无效
     */
    public static boolean isValidId(String id) {
        return isValidString(id);
    }

    /**
     * 检查ID是否无效（为 null、为空、是无效字符串）
     *
     * @param id ID字符串
     * @return true 如果ID无效，false 如果ID有效
     */
    public static boolean isInvalidId(String id) {
        return !isValidId(id);
    }
}
