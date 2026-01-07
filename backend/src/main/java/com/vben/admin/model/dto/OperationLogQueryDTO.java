package com.vben.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作日志查询DTO
 *
 * @author vben
 */
@Data
@Schema(description = "操作日志查询DTO")
public class OperationLogQueryDTO {

    /**
     * 页码（从1开始）
     */
    @Schema(description = "页码（从1开始）", example = "1")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 操作类型：view/add/edit/delete/export/import/login/logout/download/upload
     */
    @Schema(description = "操作类型：view/add/edit/delete/export/import/login/logout/download/upload", example = "view")
    private String operationType;

    /**
     * 操作模块：system/user/menu/dept/role等
     */
    @Schema(description = "操作模块：system/user/menu/dept/role等", example = "system")
    private String operationModule;

    /**
     * 状态：0-失败，1-成功
     */
    @Schema(description = "状态：0-失败，1-成功", example = "1")
    private Integer status;

    /**
     * 开始时间（格式：yyyy-MM-dd）
     */
    @Schema(description = "开始时间（格式：yyyy-MM-dd）", example = "2024-01-01")
    private String startTime;

    /**
     * 结束时间（格式：yyyy-MM-dd）
     */
    @Schema(description = "结束时间（格式：yyyy-MM-dd）", example = "2024-12-31")
    private String endTime;

    /**
     * 搜索关键词（模糊查询操作标题、请求URL等）
     */
    @Schema(description = "搜索关键词（模糊查询操作标题、请求URL等）")
    private String search;
}
