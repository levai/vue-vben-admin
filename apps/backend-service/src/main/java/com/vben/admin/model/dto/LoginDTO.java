package com.vben.admin.model.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author vben
 */
@Data
public class LoginDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
