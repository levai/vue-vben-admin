package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.core.utils.QueryHelper;
import com.vben.admin.core.utils.SearchQueryConfig;
import com.vben.admin.core.utils.SecurityUtils;
import com.vben.admin.mapper.DeptMapper;
import com.vben.admin.mapper.RoleMapper;
import com.vben.admin.mapper.UserMapper;
import com.vben.admin.mapper.UserRoleMapper;
import com.vben.admin.model.dto.UserDTO;
import com.vben.admin.model.dto.UserOptionQueryDTO;
import com.vben.admin.model.entity.SysDept;
import com.vben.admin.model.entity.SysRole;
import com.vben.admin.model.entity.SysUser;
import com.vben.admin.model.entity.SysUserRole;
import com.vben.admin.model.vo.UserVO;
import com.vben.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author vben
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final DeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> getUserList(Integer page, Integer pageSize, String search, String username, String realName, String deptId, Integer status, String startTime, String endTime) {
        // 构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        // 搜索关键词处理（优先级高于 username/realName）
        QueryHelper.applySearch(
                queryWrapper,
                SearchQueryConfig.<SysUser>of(search)
                        .searchField(SysUser::getUsername)
                        .searchField(SysUser::getRealName)
                        .fallbackField(SysUser::getUsername, username)
                        .fallbackField(SysUser::getRealName, realName)
        );

        // 其他查询条件
        if (StringUtils.hasText(deptId)) {
            queryWrapper.eq(SysUser::getDeptId, deptId);
        }
        if (status != null) {
            queryWrapper.eq(SysUser::getStatus, status);
        }

        // 时间范围查询
        QueryHelper.applyTimeRange(queryWrapper, startTime, endTime, SysUser::getCreateTime);

        queryWrapper.orderByDesc(SysUser::getCreateTime);

        // 分页查询
        Page<SysUser> pageParam = new Page<>(page, pageSize);
        IPage<SysUser> pageResult = userMapper.selectPage(pageParam, queryWrapper);

        // 转换为VO
        List<UserVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal());
    }

    @Override
    public UserVO getUserById(String id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, userDTO.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号是否已存在
        long phoneCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, userDTO.getPhone())
        );
        if (phoneCount > 0) {
            throw new BusinessException("手机号已存在");
        }

        // 检查工号是否已存在
        long employeeNoCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmployeeNo, userDTO.getEmployeeNo())
        );
        if (employeeNoCount > 0) {
            throw new BusinessException("工号已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(userDTO.getUsername());
        // 密码处理：如果未提供密码，则使用默认密码88888888
        String password = StringUtils.hasText(userDTO.getPassword())
                ? userDTO.getPassword()
                : "88888888";
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(userDTO.getRealName());
        user.setNickname(userDTO.getNickname());
        user.setPhone(userDTO.getPhone());
        user.setGender(userDTO.getGender());
        user.setEmployeeNo(userDTO.getEmployeeNo());
        user.setDeptId(userDTO.getDeptId());
        if (userDTO.getStatus() == null) {
            user.setStatus(1); // 默认启用
        } else {
            user.setStatus(userDTO.getStatus());
        }

        userMapper.insert(user);

        // 保存用户角色关联
        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            saveUserRoles(user.getId(), userDTO.getRoleIds());
        }

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(String id, UserDTO userDTO) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果更新用户名，检查是否重复
        if (StringUtils.hasText(userDTO.getUsername()) && !userDTO.getUsername().equals(user.getUsername())) {
            long count = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getUsername, userDTO.getUsername())
                            .ne(SysUser::getId, id)
            );
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(userDTO.getUsername());
        }

        // 更新密码（如果提供了新密码）
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // 更新真实姓名（必填）
        if (StringUtils.hasText(userDTO.getRealName())) {
            user.setRealName(userDTO.getRealName());
        }

        // 更新昵称（必填）
        if (StringUtils.hasText(userDTO.getNickname())) {
            user.setNickname(userDTO.getNickname());
        }

        // 更新手机号（必填，需要检查是否重复）
        if (StringUtils.hasText(userDTO.getPhone())) {
            if (!userDTO.getPhone().equals(user.getPhone())) {
                long phoneCount = userMapper.selectCount(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getPhone, userDTO.getPhone())
                                .ne(SysUser::getId, id)
                );
                if (phoneCount > 0) {
                    throw new BusinessException("手机号已存在");
                }
            }
            user.setPhone(userDTO.getPhone());
        }

        // 更新性别（必填）
        if (userDTO.getGender() != null) {
            user.setGender(userDTO.getGender());
        }

        // 更新工号（必填，需要检查是否重复）
        if (StringUtils.hasText(userDTO.getEmployeeNo())) {
            if (!userDTO.getEmployeeNo().equals(user.getEmployeeNo())) {
                long employeeNoCount = userMapper.selectCount(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getEmployeeNo, userDTO.getEmployeeNo())
                                .ne(SysUser::getId, id)
                );
                if (employeeNoCount > 0) {
                    throw new BusinessException("工号已存在");
                }
            }
            user.setEmployeeNo(userDTO.getEmployeeNo());
        }

        // 更新部门ID（必填）
        if (StringUtils.hasText(userDTO.getDeptId())) {
            user.setDeptId(userDTO.getDeptId());
        }

        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        userMapper.updateById(user);

        // 处理角色关联：如果传了 roleIds，则更新；如果传了空数组，则清空；如果不传，则保持原样
        if (userDTO.getRoleIds() != null) {
            // 删除原有关联
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));

            // 保存新的关联（如果角色列表不为空）
            if (!userDTO.getRoleIds().isEmpty()) {
                saveUserRoles(id, userDTO.getRoleIds());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));

        // 逻辑删除用户
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效，必须为0或1");
        }

        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String id, String password) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }

        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPassword, String newPassword) {
        // 获取当前用户ID
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        // 查询用户
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 验证新密码
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }

        // 检查新旧密码是否相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUserInfo(UserDTO userDTO) {
        // 获取当前用户ID
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        // 查询用户
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新真实姓名（如果提供了）
        if (StringUtils.hasText(userDTO.getRealName())) {
            user.setRealName(userDTO.getRealName());
        }

        // 更新昵称（如果提供了）
        if (StringUtils.hasText(userDTO.getNickname())) {
            user.setNickname(userDTO.getNickname());
        }

        // 更新手机号（如果提供了，需要检查是否重复）
        if (StringUtils.hasText(userDTO.getPhone())) {
            if (!userDTO.getPhone().equals(user.getPhone())) {
                long phoneCount = userMapper.selectCount(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getPhone, userDTO.getPhone())
                                .ne(SysUser::getId, userId)
                );
                if (phoneCount > 0) {
                    throw new BusinessException("手机号已存在");
                }
            }
            user.setPhone(userDTO.getPhone());
        }

        // 更新性别（如果提供了）
        if (userDTO.getGender() != null) {
            user.setGender(userDTO.getGender());
        }

        userMapper.updateById(user);
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(String userId, List<String> roleIds) {
        for (String roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    /**
     * 转换为VO
     */
    private UserVO convertToVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        // 查询用户关联的角色ID列表
        List<String> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId())
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        vo.setRoles(roleIds);

        // 查询角色名称列表
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
            List<String> roleNames = roles.stream()
                    .map(SysRole::getName)
                    .collect(Collectors.toList());
            vo.setRoleNames(roleNames);
        } else {
            vo.setRoleNames(List.of());
        }

        // 查询部门名称
        if (StringUtils.hasText(user.getDeptId())) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getName());
            }
        }

        // 查询创建人名称
        if (StringUtils.hasText(user.getCreateBy())) {
            SysUser creator = userMapper.selectById(user.getCreateBy());
            if (creator != null) {
                vo.setCreateByName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
            }
        }

        // 查询更新人名称
        if (StringUtils.hasText(user.getUpdateBy())) {
            SysUser updater = userMapper.selectById(user.getUpdateBy());
            if (updater != null) {
                vo.setUpdateByName(updater.getRealName() != null ? updater.getRealName() : updater.getUsername());
            }
        }

        return vo;
    }

    @Override
    public PageResult<UserVO> getUserOptions(UserOptionQueryDTO queryDTO) {
        // 构建查询条件（查询所有字段）
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getDeleted, 0);

        // 搜索关键词处理（优先级高于 username/realName）
        QueryHelper.applySearch(
                queryWrapper,
                SearchQueryConfig.<SysUser>of(queryDTO.getSearch())
                        .searchField(SysUser::getUsername)
                        .searchField(SysUser::getRealName)
                        .fallbackField(SysUser::getUsername, queryDTO.getUsername())
                        .fallbackField(SysUser::getRealName, queryDTO.getRealName())
        );

        // 其他查询条件
        if (StringUtils.hasText(queryDTO.getDeptId())) {
            queryWrapper.eq(SysUser::getDeptId, queryDTO.getDeptId());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }

        // 时间范围查询
        QueryHelper.applyTimeRange(queryWrapper, queryDTO.getStartTime(), queryDTO.getEndTime(), SysUser::getCreateTime);

        queryWrapper.orderByAsc(SysUser::getUsername);

        // 查询数据（应用 limit 限制）
        List<SysUser> users = userMapper.selectList(queryWrapper);
        long total = users.size();

        // 应用 limit 限制（防止数据量过大）
        users = QueryHelper.applyLimit(users, queryDTO.getLimit());

        List<UserVO> options = users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // total 表示实际总数（可能 > items.length，如果被 limit 截断）
        return new PageResult<>(options, total);
    }
}
