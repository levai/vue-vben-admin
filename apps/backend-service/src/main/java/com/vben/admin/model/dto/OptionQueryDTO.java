package com.vben.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 选项查询DTO（用于下拉选项查询）
 *
 * @author vben
 */
@Data
@Schema(description = "选项查询DTO（用于下拉选项查询）")
public class OptionQueryDTO {

    /**
     * 页码（从1开始，不传或传0则返回全部）
     */
    @Schema(description = "页码（从1开始，不传或传0则返回全部）", example = "1")
    private Integer page;

    /**
     * 每页大小（不传或传0则返回全部）
     */
    @Schema(description = "每页大小（不传或传0则返回全部）", example = "20")
    private Integer pageSize;

    /**
     * 搜索关键词（用于模糊搜索）
     */
    @Schema(description = "搜索关键词（用于模糊搜索）")
    private String search;
}
