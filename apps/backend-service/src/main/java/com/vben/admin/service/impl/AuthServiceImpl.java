package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.core.utils.JwtUtils;
import com.vben.admin.core.utils.SecurityUtils;
import com.vben.admin.mapper.PermissionMapper;
import com.vben.admin.mapper.UserMapper;
import com.vben.admin.mapper.UserRoleMapper;
import com.vben.admin.model.dto.LoginDTO;
import com.vben.admin.model.entity.SysUser;
import com.vben.admin.model.vo.LoginResultVO;
import com.vben.admin.model.vo.UserVO;
import com.vben.admin.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证服务实现
 *
 * @author vben
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResultVO login(LoginDTO loginDTO) {
        // 查询用户（排除已删除的用户）
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginDTO.getUsername())
                        .eq(SysUser::getDeleted, 0) // 排除已删除的用户
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }

        // 生成Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        String accessToken = jwtUtils.generateAccessToken(claims);
        String refreshToken = jwtUtils.generateRefreshToken(claims);

        LoginResultVO result = new LoginResultVO();
        result.setAccessToken(accessToken);
        result.setRefreshToken(refreshToken);
        return result;
    }

    @Override
    public void logout() {
        // 可以在这里实现token黑名单机制
        // 当前简单实现，前端删除token即可
    }

    @Override
    public String refreshToken(String refreshToken) {
        // 验证refreshToken
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException("刷新Token已过期，请重新登录");
        }

        // 从refreshToken中获取用户信息
        String userId = jwtUtils.getUserIdFromToken(refreshToken);
        String username = jwtUtils.getUsernameFromToken(refreshToken);

        // 生成新的AccessToken
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return jwtUtils.generateAccessToken(claims);
    }

    @Override
    public List<String> getAccessCodes() {
        // 从SecurityContext中获取当前用户ID
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return permissionMapper.selectCodesByUserId(userId);
    }

    @Override
    public UserVO getCurrentUserInfo() {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 查询用户角色
        List<String> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);

        UserVO userInfo = new UserVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setNickname(user.getNickname());
        userInfo.setPhone(user.getPhone());
        userInfo.setGender(user.getGender());
        userInfo.setRoles(roleIds);

        return userInfo;
    }

    /**
     * 获取当前用户ID（从请求属性中获取）
     */
    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}
