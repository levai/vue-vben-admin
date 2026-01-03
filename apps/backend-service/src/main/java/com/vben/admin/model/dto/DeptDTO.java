package com.vben.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部门DTO
 *
 * @author vben
 */
@Data
@Schema(description = "部门DTO")
public class DeptDTO {

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    @NotBlank(message = "部门名称不能为空")
    private String name;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID", example = "dept001")
    private String pid;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "负责技术开发")
    private String remark;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}
