package com.vben.admin.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 * @author vben
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> items;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 创建分页结果
     *
     * @param items  数据列表
     * @param total  总记录数
     * @param <T>    数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> items, Long total) {
        return new PageResult<>(items, total);
    }
}
