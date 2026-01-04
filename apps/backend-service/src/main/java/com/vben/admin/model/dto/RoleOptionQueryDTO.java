package com.vben.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色选项查询DTO
 *
 * @author vben
 */
@Data
@Schema(description = "角色选项查询DTO")
public class RoleOptionQueryDTO {

    /**
     * 搜索关键词（模糊查询角色名称和ID，优先级高于 name/id）
     */
    @Schema(description = "搜索关键词（模糊查询角色名称和ID，优先级高于 name/id）")
    private String search;

    /**
     * 角色名称（模糊查询，与 search 互斥）
     */
    @Schema(description = "角色名称（模糊查询，与 search 互斥）")
    private String name;

    /**
     * 角色ID（模糊查询，与 search 互斥）
     */
    @Schema(description = "角色ID（模糊查询，与 search 互斥）")
    private String id;

    /**
     * 备注（模糊查询）
     */
    @Schema(description = "备注（模糊查询）")
    private String remark;

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
     * 页码（从1开始，可选。如果传了 page 和 pageSize，则使用分页查询；否则使用 limit 限制）
     */
    @Schema(description = "页码（从1开始，可选。如果传了 page 和 pageSize，则使用分页查询；否则使用 limit 限制）", example = "1")
    private Integer page;

    /**
     * 每页大小（可选。如果传了 page 和 pageSize，则使用分页查询；否则使用 limit 限制）
     */
    @Schema(description = "每页大小（可选。如果传了 page 和 pageSize，则使用分页查询；否则使用 limit 限制）", example = "20")
    private Integer pageSize;

    /**
     * 最大返回数量（默认 100，防止数据量过大）
     * - 普通下拉：100（默认，角色数据量通常较小）
     * - 需要全部数据：传 -1（实际最多返回 10000 条，有上限保护）
     * - 自定义数量：传具体数值（最大 10000）
     * 
     * 仅在未传 page 和 pageSize 时生效
     */
    @Schema(description = "最大返回数量（默认 100。传 -1 表示获取全部，实际最多 10000 条。仅在未传 page 和 pageSize 时生效）", example = "100")
    private Integer limit = 100;
}
