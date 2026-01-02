package com.vben.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色VO
 *
 * @author vben
 */
@Data
@Schema(description = "角色VO")
public class RoleVO {

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private String id;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String name;

    /**
     * 权限列表（权限码列表）
     */
    @Schema(description = "权限列表（权限码列表）")
    private List<String> permissions;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /**
     * 显示标签（用于下拉选项显示，自动设置）
     */
    @Schema(description = "显示标签（用于下拉选项显示，自动设置）")
    private String label;

    /**
     * 选项值（用于下拉选项值，自动设置）
     */
    @Schema(description = "选项值（用于下拉选项值，自动设置）")
    private String value;
}
