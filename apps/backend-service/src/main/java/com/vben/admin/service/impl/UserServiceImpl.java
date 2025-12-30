package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.mapper.DeptMapper;
import com.vben.admin.mapper.RoleMapper;
import com.vben.admin.mapper.UserMapper;
import com.vben.admin.mapper.UserRoleMapper;
import com.vben.admin.model.dto.UserDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    public PageResult<UserVO> getUserList(Integer page, Integer pageSize, String username, String realName, String deptId, Integer status, String startTime, String endTime) {
        // 构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(username)) {
            queryWrapper.like(SysUser::getUsername, username);
        }
        if (StringUtils.hasText(realName)) {
            queryWrapper.like(SysUser::getRealName, realName);
        }
        if (StringUtils.hasText(deptId)) {
            queryWrapper.eq(SysUser::getDeptId, deptId);
        }
        if (status != null) {
            queryWrapper.eq(SysUser::getStatus, status);
        }

        // 时间范围查询
        if (StringUtils.hasText(startTime)) {
            try {
                LocalDate startDate = LocalDate.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDateTime startDateTime = startDate.atStartOfDay();
                queryWrapper.ge(SysUser::getCreateTime, startDateTime);
            } catch (DateTimeParseException e) {
                // 忽略无效的日期格式
            }
        }
        if (StringUtils.hasText(endTime)) {
            try {
                LocalDate endDate = LocalDate.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                queryWrapper.le(SysUser::getCreateTime, endDateTime);
            } catch (DateTimeParseException e) {
                // 忽略无效的日期格式
            }
        }

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

        return vo;
    }
}
