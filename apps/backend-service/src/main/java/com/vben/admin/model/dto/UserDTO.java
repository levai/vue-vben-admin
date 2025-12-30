package com.vben.admin.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
     * 规则：小写字母、数字、下划线，长度4-20，必须以字母开头
     */
    @NotBlank(message = "用户名不能为空", groups = Create.class)
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20之间", groups = Create.class)
    @Pattern(regexp = "^[a-z][a-z0-9_]{3,19}$", message = "用户名必须以小写字母开头，只能包含小写字母、数字和下划线，长度4-20", groups = Create.class)
    private String username;

    /**
     * 密码（创建时可选，不填则使用默认密码88888888；更新时可选，不填则不修改）
     */
    @Size(min = 6, max = 50, message = "密码长度必须在6-50之间", groups = {Create.class, Update.class})
    private String password;

    /**
     * 真实姓名（必填）
     */
    @NotBlank(message = "真实姓名不能为空", groups = {Create.class, Update.class})
    @Size(max = 50, message = "真实姓名长度不能超过50", groups = {Create.class, Update.class})
    private String realName;

    /**
     * 昵称（必填）
     */
    @NotBlank(message = "昵称不能为空", groups = {Create.class, Update.class})
    @Size(max = 50, message = "昵称长度不能超过50", groups = {Create.class, Update.class})
    private String nickname;

    /**
     * 手机号（必填，需要校验格式）
     */
    @NotBlank(message = "手机号不能为空", groups = {Create.class, Update.class})
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = {Create.class, Update.class})
    private String phone;

    /**
     * 性别：0-未知，1-男，2-女（必填）
     */
    @NotNull(message = "性别不能为空", groups = {Create.class, Update.class})
    private Integer gender;

    /**
     * 工号（必填）
     */
    @NotBlank(message = "工号不能为空", groups = {Create.class, Update.class})
    @Size(max = 50, message = "工号长度不能超过50", groups = {Create.class, Update.class})
    private String employeeNo;

    /**
     * 部门ID（必填）
     */
    @NotBlank(message = "部门不能为空", groups = {Create.class, Update.class})
    private String deptId;

    /**
     * 角色ID列表（必填，至少选择一个）
     */
    @NotNull(message = "角色不能为空", groups = {Create.class, Update.class})
    @Size(min = 1, message = "至少需要分配一个角色", groups = {Create.class, Update.class})
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
