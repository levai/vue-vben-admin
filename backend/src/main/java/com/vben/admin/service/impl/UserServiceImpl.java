package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vben.admin.core.constants.SystemConstants;
import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.core.utils.QueryHelper;
import com.vben.admin.core.utils.SearchQueryConfig;
import com.vben.admin.core.utils.SecurityUtils;
import com.vben.admin.core.utils.ValidationUtils;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author vben
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final DeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> getUserList(Integer page, Integer pageSize, String search, String username, String realName, String deptId, Integer status, String startTime, String endTime) {
        LambdaQueryWrapper<SysUser> queryWrapper = buildBaseQueryWrapper(
                search, username, realName, deptId, status, startTime, endTime
        );
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> pageParam = new Page<>(page, pageSize);
        IPage<SysUser> pageResult = userMapper.selectPage(pageParam, queryWrapper);

        List<UserVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal());
    }

    @Override
    public UserVO getUserById(String id) {
        SysUser user = getUserByIdOrThrow(id);
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createUser(UserDTO userDTO) {
        validateUserUniqueness(userDTO, null);
        SysUser user = buildUserEntity(userDTO, null);
        userMapper.insert(user);

        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            saveUserRoles(user.getId(), userDTO.getRoleIds());
        }

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(String id, UserDTO userDTO) {
        SysUser user = getUserByIdOrThrow(id);

        validateUserUniquenessForUpdate(user, userDTO, id);
        updateUserBasicInfo(user, userDTO);
        userMapper.updateById(user);
        updateUserRoles(id, userDTO.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        getUserByIdOrThrow(id);

        List<String> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, id)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.contains(SystemConstants.ADMIN_ROLE_ID)) {
            throw new BusinessException("超级管理员不能被删除");
        }

        // 不删除用户角色关联，保留关联数据以支持恢复用户
        // 查询时通过用户状态过滤，已删除或已禁用的用户无法访问菜单和权限
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String id, Integer status) {
        SysUser user = getUserByIdOrThrow(id);

        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效，必须为0或1");
        }

        if (status == 0) {
            List<String> roleIds = userRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserId, id)
            ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

            if (roleIds.contains(SystemConstants.ADMIN_ROLE_ID)) {
                throw new BusinessException("超级管理员不能被禁用");
            }
        }

        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String id, String password) {
        SysUser user = getUserByIdOrThrow(id);
        validatePassword(password);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPassword, String newPassword) {
        String userId = getCurrentUserIdOrThrow();
        SysUser user = getUserByIdOrThrow(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        validatePassword(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUserInfo(UserDTO userDTO) {
        String userId = getCurrentUserIdOrThrow();
        SysUser user = getUserByIdOrThrow(userId);
        updateCurrentUserBasicInfo(user, userDTO);
        userMapper.updateById(user);
    }

    /**
     * 根据ID获取用户，如果不存在则抛出异常
     *
     * @param id 用户ID
     * @return 用户实体
     * @throws BusinessException 如果用户不存在
     */
    private SysUser getUserByIdOrThrow(String id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
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
     * 验证用户唯一性（创建时）
     *
     * @param userDTO 用户DTO
     * @param id      用户ID（创建时传入null）
     * @throws BusinessException 如果用户名、手机号或工号已存在
     */
    private void validateUserUniqueness(UserDTO userDTO, String id) {
        if (ValidationUtils.isValidString(userDTO.getUsername())) {
            checkUsernameNotExists(userDTO.getUsername(), id);
        }
        if (ValidationUtils.isValidString(userDTO.getPhone())) {
            checkPhoneNotExists(userDTO.getPhone(), id);
        }
        if (ValidationUtils.isValidString(userDTO.getEmployeeNo())) {
            checkEmployeeNoNotExists(userDTO.getEmployeeNo(), id);
        }
    }

    /**
     * 验证用户唯一性（更新时，只验证变更的字段）
     *
     * @param user    当前用户实体
     * @param userDTO 用户DTO
     * @param id      用户ID
     * @throws BusinessException 如果用户名、手机号或工号已存在
     */
    private void validateUserUniquenessForUpdate(SysUser user, UserDTO userDTO, String id) {
        if (ValidationUtils.isValidString(userDTO.getUsername()) && !userDTO.getUsername().equals(user.getUsername())) {
            checkUsernameNotExists(userDTO.getUsername(), id);
        }
        if (ValidationUtils.isValidString(userDTO.getPhone()) && !userDTO.getPhone().equals(user.getPhone())) {
            checkPhoneNotExists(userDTO.getPhone(), id);
        }
        if (ValidationUtils.isValidString(userDTO.getEmployeeNo()) && !userDTO.getEmployeeNo().equals(user.getEmployeeNo())) {
            checkEmployeeNoNotExists(userDTO.getEmployeeNo(), id);
        }
    }

    /**
     * 检查用户名不存在
     *
     * @param username 用户名
     * @param id       用户ID（更新时传入，创建时传入null）
     * @throws BusinessException 如果用户名已存在
     */
    private void checkUsernameNotExists(String username, String id) {
        if (ValidationUtils.isInvalidString(username)) {
            throw new BusinessException("用户名不能为空或无效值");
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username);
        if (id != null) {
            queryWrapper.ne(SysUser::getId, id);
        }
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    /**
     * 检查手机号不存在
     *
     * @param phone 手机号
     * @param id    用户ID（更新时传入，创建时传入null）
     * @throws BusinessException 如果手机号已存在
     */
    private void checkPhoneNotExists(String phone, String id) {
        if (ValidationUtils.isInvalidString(phone)) {
            throw new BusinessException("手机号不能为空或无效值");
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone);
        if (id != null) {
            queryWrapper.ne(SysUser::getId, id);
        }
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("手机号已存在");
        }
    }

    /**
     * 检查工号不存在
     *
     * @param employeeNo 工号
     * @param id         用户ID（更新时传入，创建时传入null）
     * @throws BusinessException 如果工号已存在
     */
    private void checkEmployeeNoNotExists(String employeeNo, String id) {
        if (ValidationUtils.isInvalidString(employeeNo)) {
            throw new BusinessException("工号不能为空或无效值");
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmployeeNo, employeeNo);
        if (id != null) {
            queryWrapper.ne(SysUser::getId, id);
        }
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("工号已存在");
        }
    }

    /**
     * 验证密码格式
     *
     * @param password 密码
     * @throws BusinessException 如果密码格式不正确
     */
    private void validatePassword(String password) {
        if (ValidationUtils.isInvalidString(password)) {
            throw new BusinessException("密码不能为空或无效值");
        }
        String cleanedPassword = ValidationUtils.cleanString(password);
        if (cleanedPassword == null || cleanedPassword.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
    }

    /**
     * 构建用户实体
     *
     * @param userDTO 用户DTO
     * @param id      用户ID（更新时传入，创建时传入null）
     * @return 用户实体
     */
    private SysUser buildUserEntity(UserDTO userDTO, String id) {
        SysUser user = new SysUser();
        user.setUsername(userDTO.getUsername());
        String password = ValidationUtils.isValidString(userDTO.getPassword())
                ? userDTO.getPassword()
                : "88888888";
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(userDTO.getRealName());
        user.setNickname(userDTO.getNickname());
        user.setPhone(userDTO.getPhone());
        user.setGender(userDTO.getGender());
        user.setEmployeeNo(userDTO.getEmployeeNo());
        user.setDeptId(userDTO.getDeptId());
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : 1);
        return user;
    }

    /**
     * 更新用户基本信息
     *
     * @param user    用户实体
     * @param userDTO 用户DTO
     */
    private void updateUserBasicInfo(SysUser user, UserDTO userDTO) {
        if (ValidationUtils.isValidString(userDTO.getUsername())) {
            user.setUsername(ValidationUtils.cleanString(userDTO.getUsername()));
        }

        if (ValidationUtils.isValidString(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (ValidationUtils.isValidString(userDTO.getRealName())) {
            user.setRealName(ValidationUtils.cleanString(userDTO.getRealName()));
        }

        if (ValidationUtils.isValidString(userDTO.getNickname())) {
            user.setNickname(ValidationUtils.cleanString(userDTO.getNickname()));
        }

        if (ValidationUtils.isValidString(userDTO.getPhone())) {
            user.setPhone(ValidationUtils.cleanString(userDTO.getPhone()));
        }

        if (userDTO.getGender() != null) {
            user.setGender(userDTO.getGender());
        }

        if (ValidationUtils.isValidString(userDTO.getEmployeeNo())) {
            user.setEmployeeNo(ValidationUtils.cleanString(userDTO.getEmployeeNo()));
        }

        if (ValidationUtils.isValidId(userDTO.getDeptId())) {
            user.setDeptId(userDTO.getDeptId());
        }

        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }
    }

    /**
     * 更新当前用户基本信息（只允许更新部分字段）
     *
     * @param user    用户实体
     * @param userDTO 用户DTO
     */
    private void updateCurrentUserBasicInfo(SysUser user, UserDTO userDTO) {
        if (ValidationUtils.isValidString(userDTO.getRealName())) {
            user.setRealName(ValidationUtils.cleanString(userDTO.getRealName()));
        }

        if (ValidationUtils.isValidString(userDTO.getNickname())) {
            user.setNickname(ValidationUtils.cleanString(userDTO.getNickname()));
        }

        if (ValidationUtils.isValidString(userDTO.getPhone())) {
            String cleanedPhone = ValidationUtils.cleanString(userDTO.getPhone());
            if (!cleanedPhone.equals(user.getPhone())) {
                checkPhoneNotExists(cleanedPhone, user.getId());
            }
            user.setPhone(cleanedPhone);
        }

        if (userDTO.getGender() != null) {
            user.setGender(userDTO.getGender());
        }
    }

    /**
     * 更新用户角色关联
     *
     * @param userId   用户ID
     * @param roleIds  角色ID列表
     */
    private void updateUserRoles(String userId, List<String> roleIds) {
        if (roleIds != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
            if (!roleIds.isEmpty()) {
                saveUserRoles(userId, roleIds);
            }
        }
    }

    /**
     * 保存用户角色关联
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
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

        List<String> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId())
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        vo.setRoles(roleIds);

        if (!roleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
            List<String> roleNames = roles.stream()
                    .map(SysRole::getName)
                    .collect(Collectors.toList());
            vo.setRoleNames(roleNames);
        } else {
            vo.setRoleNames(List.of());
        }

        if (StringUtils.hasText(user.getDeptId())) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getName());
            }
        }

        if (StringUtils.hasText(user.getCreateBy())) {
            vo.setCreateByName(getUserName(user.getCreateBy()));
        }

        if (StringUtils.hasText(user.getUpdateBy())) {
            vo.setUpdateByName(getUserName(user.getUpdateBy()));
        }

        return vo;
    }

    @Override
    public PageResult<UserVO> getUserOptions(UserOptionQueryDTO queryDTO) {
        LambdaQueryWrapper<SysUser> queryWrapper = buildBaseQueryWrapper(
                queryDTO.getSearch(), queryDTO.getUsername(), queryDTO.getRealName(),
                queryDTO.getDeptId(), queryDTO.getStatus(), queryDTO.getStartTime(), queryDTO.getEndTime()
        );

        queryWrapper.eq(SysUser::getDeleted, 0);
        queryWrapper.orderByAsc(SysUser::getUsername);

        if (queryDTO.getPage() != null && queryDTO.getPageSize() != null) {
            Page<SysUser> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
            IPage<SysUser> pageResult = userMapper.selectPage(pageParam, queryWrapper);

            List<UserVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return PageResult.of(voList, pageResult.getTotal());
        } else {
            int maxLimit = QueryHelper.getValidLimit(queryDTO.getLimit());
            queryWrapper.last("LIMIT " + maxLimit);

            List<SysUser> users = userMapper.selectList(queryWrapper);

            List<UserVO> options = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return new PageResult<>(options, (long) options.size());
        }
    }

    /**
     * 构建基础查询条件（公共逻辑）
     * 用于 List 和 Options 接口
     *
     * @param search    搜索关键词
     * @param username  用户名
     * @param realName  真实姓名
     * @param deptId    部门ID
     * @param status    状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<SysUser> buildBaseQueryWrapper(
            String search, String username, String realName,
            String deptId, Integer status, String startTime, String endTime
    ) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        QueryHelper.applySearch(
                queryWrapper,
                SearchQueryConfig.<SysUser>of(search)
                        .searchField(SysUser::getUsername)
                        .searchField(SysUser::getRealName)
                        .fallbackField(SysUser::getUsername, username)
                        .fallbackField(SysUser::getRealName, realName)
        );

        if (ValidationUtils.isValidId(deptId)) {
            queryWrapper.eq(SysUser::getDeptId, deptId);
        }
        if (status != null) {
            queryWrapper.eq(SysUser::getStatus, status);
        }

        QueryHelper.applyTimeRange(queryWrapper, startTime, endTime, SysUser::getCreateTime);

        return queryWrapper;
    }

    /**
     * 获取用户名称（优先使用真实姓名，否则使用用户名）
     *
     * @param userId 用户ID
     * @return 用户名称
     */
    private String getUserName(String userId) {
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            return user.getRealName() != null ? user.getRealName() : user.getUsername();
        }
        return null;
    }
}
