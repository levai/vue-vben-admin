package com.vben.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import lombok.Data;

import java.util.List;

/**
 * 角色DTO
 *
 * @author vben
 */
@Data
@Schema(description = "角色DTO")
public class RoleDTO {

    /**
     * 角色名称（创建时必填，更新时可选）
     */
    @Schema(description = "角色名称（创建时必填，更新时可选）", example = "管理员")
    @NotBlank(message = "角色名称不能为空", groups = Create.class)
    private String name;

    /**
     * 权限列表（菜单ID列表）
     */
    @Schema(description = "权限列表（菜单ID列表）", example = "[\"menu001\", \"menu002\"]")
    private List<String> permissions;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "系统管理员角色")
    private String remark;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    /**
     * 验证分组：创建
     */
    public interface Create extends Default {
    }

    /**
     * 验证分组：更新
     */
    public interface Update extends Default {
    }
}
