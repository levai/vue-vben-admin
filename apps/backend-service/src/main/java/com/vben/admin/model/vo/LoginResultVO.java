package com.vben.admin.model.vo;

import lombok.Data;

/**
 * 登录结果VO
 *
 * @author vben
 */
@Data
public class LoginResultVO {

    /**
     * AccessToken
     */
    private String accessToken;

    /**
     * RefreshToken（仅用于设置 Cookie，不返回给前端）
     */
    private String refreshToken;
}
