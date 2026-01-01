package com.vben.admin.model.dto;

import lombok.Data;

/**
 * 选项查询DTO（用于下拉选项查询）
 *
 * @author vben
 */
@Data
public class OptionQueryDTO {

    /**
     * 页码（从1开始，不传或传0则返回全部）
     */
    private Integer page;

    /**
     * 每页大小（不传或传0则返回全部）
     */
    private Integer pageSize;

    /**
     * 搜索关键词（用于模糊搜索）
     */
    private String search;
}
