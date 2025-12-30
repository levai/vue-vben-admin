package com.vben.admin.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import lombok.Data;

import java.util.List;

/**
 * 用户DTO
 *
 * @author vben
 */
@Data
public class UserDTO {

    /**
     * 用户名（创建时必填，更新时可选）
     */
    @NotBlank(message = "用户名不能为空", groups = Create.class)
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间", groups = Create.class)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线", groups = Create.class)
    private String username;

    /**
     * 密码（创建时可选，不填则使用默认密码88888888；更新时可选，不填则不修改）
     */
    @Size(min = 6, max = 50, message = "密码长度必须在6-50之间", groups = {Create.class, Update.class})
    private String password;

    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 角色ID列表
     */
    private List<String> roleIds;

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
