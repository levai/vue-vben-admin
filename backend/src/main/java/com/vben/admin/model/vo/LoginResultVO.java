package com.vben.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录结果VO
 *
 * @author vben
 */
@Data
@Schema(description = "登录结果VO")
public class LoginResultVO {

    /**
     * AccessToken
     */
    @Schema(description = "AccessToken（访问令牌）")
    private String accessToken;

    /**
     * RefreshToken（仅用于设置 Cookie，不返回给前端）
     */
    @Schema(description = "RefreshToken（仅用于设置 Cookie，不返回给前端）", hidden = true)
    private String refreshToken;
}
