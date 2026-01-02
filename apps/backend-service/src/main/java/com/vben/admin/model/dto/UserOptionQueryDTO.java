package com.vben.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户选项查询DTO
 *
 * @author vben
 */
@Data
@Schema(description = "用户选项查询DTO")
public class UserOptionQueryDTO {

    /**
     * 搜索关键词（模糊查询用户名和真实姓名，优先级高于 username/realName）
     */
    @Schema(description = "搜索关键词（模糊查询用户名和真实姓名，优先级高于 username/realName）")
    private String search;

    /**
     * 用户名（模糊查询，与 search 互斥）
     */
    @Schema(description = "用户名（模糊查询，与 search 互斥）")
    private String username;

    /**
     * 真实姓名（模糊查询，与 search 互斥）
     */
    @Schema(description = "真实姓名（模糊查询，与 search 互斥）")
    private String realName;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private String deptId;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Schema(description = "状态（0-禁用，1-启用）")
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
     * 最大返回数量（默认 1000，防止数据量过大）
     */
    @Schema(description = "最大返回数量（默认 1000，防止数据量过大）", example = "1000")
    private Integer limit = 1000;
}
