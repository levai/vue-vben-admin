package com.vben.admin.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作类型枚举
 *
 * @author vben
 */
public enum OperationType {

    /**
     * 查看
     */
    VIEW("查看"),

    /**
     * 新增
     */
    ADD("新增"),

    /**
     * 编辑
     */
    EDIT("编辑"),

    /**
     * 删除
     */
    DELETE("删除"),

    /**
     * 登录
     */
    LOGIN("登录"),

    /**
     * 登出
     */
    LOGOUT("登出"),

    /**
     * 导出
     */
    EXPORT("导出"),

    /**
     * 导入
     */
    IMPORT("导入"),

    /**
     * 下载
     */
    DOWNLOAD("下载"),

    /**
     * 上传
     */
    UPLOAD("上传");

    private final String label;

    OperationType(String label) {
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
     * 获取所有操作类型的标签列表
     *
     * @return 标签列表
     */
    public static List<String> getAllLabels() {
        return Arrays.stream(values())
                .map(OperationType::getLabel)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 根据标签获取枚举
     *
     * @param label 标签
     * @return 枚举值，如果不存在则返回 null
     */
    public static OperationType fromLabel(String label) {
        if (label == null || label.isEmpty()) {
            return null;
        }
        for (OperationType type : values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        return null;
    }
}
