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
        // 查询并验证用户
        SysUser user = findAndValidateUser(loginDTO.getUsername(), loginDTO.getPassword());

        // 生成Token
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

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

        // 从refreshToken中获取用户信息并生成新的AccessToken
        String userId = jwtUtils.getUserIdFromToken(refreshToken);
        String username = jwtUtils.getUsernameFromToken(refreshToken);
        return generateAccessToken(userId, username);
    }

    @Override
    public List<String> getAccessCodes() {
        String userId = getCurrentUserIdOrThrow();
        return permissionMapper.selectCodesByUserId(userId);
    }

    @Override
    public UserVO getCurrentUserInfo() {
        String userId = getCurrentUserIdOrThrow();
        SysUser user = getUserByIdOrThrow(userId);

        // 查询用户角色
        List<String> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);

        // 构建用户信息VO
        return buildUserVO(user, roleIds);
    }

    /**
     * 查找并验证用户（用于登录）
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户实体
     * @throws BusinessException 如果用户不存在、密码错误或用户被禁用
     */
    private SysUser findAndValidateUser(String username, String password) {
        // 查询用户（排除已删除的用户）
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }

        return user;
    }

    /**
     * 生成访问Token
     *
     * @param user 用户实体
     * @return 访问Token
     */
    private String generateAccessToken(SysUser user) {
        return generateAccessToken(user.getId(), user.getUsername());
    }

    /**
     * 生成访问Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 访问Token
     */
    private String generateAccessToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return jwtUtils.generateAccessToken(claims);
    }

    /**
     * 生成刷新Token
     *
     * @param user 用户实体
     * @return 刷新Token
     */
    private String generateRefreshToken(SysUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        return jwtUtils.generateRefreshToken(claims);
    }

    /**
     * 获取当前用户ID，如果未登录则抛出异常
     *
     * @return 用户ID
     * @throws BusinessException 如果未登录
     */
    private String getCurrentUserIdOrThrow() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return userId;
    }

    /**
     * 根据ID获取用户，如果不存在则抛出异常
     *
     * @param userId 用户ID
     * @return 用户实体
     * @throws BusinessException 如果用户不存在
     */
    private SysUser getUserByIdOrThrow(String userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 构建用户信息VO
     *
     * @param user    用户实体
     * @param roleIds 角色ID列表
     * @return 用户信息VO
     */
    private UserVO buildUserVO(SysUser user, List<String> roleIds) {
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
}
