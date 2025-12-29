package com.vben.admin.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import lombok.Data;

import java.util.List;

/**
 * 角色DTO
 *
 * @author vben
 */
@Data
public class RoleDTO {

    /**
     * 角色名称（创建时必填，更新时可选）
     */
    @NotBlank(message = "角色名称不能为空", groups = Create.class)
    private String name;

    /**
     * 权限列表（菜单ID列表）
     */
    private List<String> permissions;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态：0-禁用，1-启用
     */
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
