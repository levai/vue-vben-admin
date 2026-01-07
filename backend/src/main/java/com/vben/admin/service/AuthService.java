package com.vben.admin.service;

import com.vben.admin.model.dto.LoginDTO;
import com.vben.admin.model.vo.LoginResultVO;
import com.vben.admin.model.vo.UserVO;

import java.util.List;

/**
 * 认证服务接口
 *
 * @author vben
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginResultVO login(LoginDTO loginDTO);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 新的AccessToken
     */
    String refreshToken(String refreshToken);

    /**
     * 获取当前用户权限码列表
     *
     * @return 权限码列表
     */
    List<String> getAccessCodes();

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    UserVO getCurrentUserInfo();
}
