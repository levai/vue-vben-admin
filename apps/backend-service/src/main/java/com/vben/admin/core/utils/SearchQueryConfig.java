package com.vben.admin.core.utils;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索查询配置
 * 用于封装搜索查询的参数，提升代码可读性和可维护性
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 普通搜索（链式调用，推荐）
 * SearchQueryConfig.of(search)
 *     .searchField(SysUser::getUsername)
 *     .searchField(SysUser::getRealName)
 *     .fallbackField(SysUser::getUsername, username)
 *     .fallbackField(SysUser::getRealName, realName);
 *
 * // ID 精确匹配
 * SearchQueryConfig.of(search)
 *     .searchField(SysRole::getName)
 *     .exactIdField(SysRole::getId, id)
 *     .fallbackField(SysRole::getName, name);
 * }</pre>
 *
 * @param <T> 实体类型
 * @author vben
 */
@Data
public class SearchQueryConfig<T> {

    /**
     * 搜索关键词（优先级高，用于多字段 OR 查询）
     */
    private String search;

    /**
     * 搜索字段列表（模糊查询，当 search 有值时使用）
     */
    private List<SFunction<T, ?>> searchFields = new ArrayList<>();

    /**
     * ID 字段（精确匹配，可选。如果提供，search 时会同时进行 ID 精确匹配）
     */
    private SFunction<T, ?> idField;

    /**
     * ID 值（精确匹配，当 search 为空时使用）
     */
    private String idValue;

    /**
     * 回退字段映射（当 search 为空时使用，key=字段，value=字段值）
     */
    private Map<SFunction<T, ?>, String> fallbackFields = new HashMap<>();

    /**
     * 创建搜索配置（使用链式调用）
     *
     * @param search 搜索关键词
     * @param <T>    实体类型
     * @return 配置对象（支持链式调用）
     */
    public static <T> SearchQueryConfig<T> of(String search) {
        SearchQueryConfig<T> config = new SearchQueryConfig<>();
        config.setSearch(search);
        return config;
    }

    /**
     * 添加搜索字段（模糊查询）
     *
     * @param field 字段
     * @return 当前配置对象（支持链式调用）
     */
    public SearchQueryConfig<T> searchField(SFunction<T, ?> field) {
        if (field != null) {
            this.searchFields.add(field);
        }
        return this;
    }

    /**
     * 设置 ID 字段（精确匹配）
     *
     * @param idField ID 字段
     * @param idValue ID 值
     * @return 当前配置对象（支持链式调用）
     */
    public SearchQueryConfig<T> exactIdField(SFunction<T, ?> idField, String idValue) {
        this.idField = idField;
        this.idValue = idValue;
        return this;
    }

    /**
     * 添加回退字段（当 search 为空时使用，模糊查询）
     *
     * @param field 字段
     * @param value 字段值
     * @return 当前配置对象（支持链式调用）
     */
    public SearchQueryConfig<T> fallbackField(SFunction<T, ?> field, String value) {
        if (field != null && value != null && !value.trim().isEmpty()) {
            this.fallbackFields.put(field, value);
        }
        return this;
    }
}
