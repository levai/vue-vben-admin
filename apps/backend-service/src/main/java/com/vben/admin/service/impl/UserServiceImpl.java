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
        // 构建基础查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = buildBaseQueryWrapper(
                search, username, realName, deptId, status, startTime, endTime
        );

        // List 接口：按创建时间倒序
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
        SysUser user = getUserByIdOrThrow(id);
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createUser(UserDTO userDTO) {
        // 验证用户唯一性
        validateUserUniqueness(userDTO, null);

        // 创建用户实体
        SysUser user = buildUserEntity(userDTO, null);
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
        // 查询用户
        SysUser user = getUserByIdOrThrow(id);

        // 验证用户唯一性（只验证变更的字段）
        validateUserUniquenessForUpdate(user, userDTO, id);

        // 更新用户基本信息
        updateUserBasicInfo(user, userDTO);
        userMapper.updateById(user);

        // 更新用户角色关联
        updateUserRoles(id, userDTO.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        // 查询用户（验证存在性）
        getUserByIdOrThrow(id);

        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));

        // 逻辑删除用户
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String id, Integer status) {
        // 查询用户
        SysUser user = getUserByIdOrThrow(id);

        // 验证状态值
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效，必须为0或1");
        }

        // 更新状态
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String id, String password) {
        // 查询用户
        SysUser user = getUserByIdOrThrow(id);

        // 验证密码
        validatePassword(password);

        // 更新密码
        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPassword, String newPassword) {
        // 获取当前用户
        String userId = getCurrentUserIdOrThrow();
        SysUser user = getUserByIdOrThrow(userId);

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 验证新密码
        validatePassword(newPassword);

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
        // 获取当前用户
        String userId = getCurrentUserIdOrThrow();
        SysUser user = getUserByIdOrThrow(userId);

        // 更新用户基本信息（只允许更新部分字段）
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
        // 检查用户名
        if (StringUtils.hasText(userDTO.getUsername())) {
            checkUsernameNotExists(userDTO.getUsername(), id);
        }
        // 检查手机号
        if (StringUtils.hasText(userDTO.getPhone())) {
            checkPhoneNotExists(userDTO.getPhone(), id);
        }
        // 检查工号
        if (StringUtils.hasText(userDTO.getEmployeeNo())) {
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
        // 检查用户名（如果变更了）
        if (StringUtils.hasText(userDTO.getUsername()) && !userDTO.getUsername().equals(user.getUsername())) {
            checkUsernameNotExists(userDTO.getUsername(), id);
        }
        // 检查手机号（如果变更了）
        if (StringUtils.hasText(userDTO.getPhone()) && !userDTO.getPhone().equals(user.getPhone())) {
            checkPhoneNotExists(userDTO.getPhone(), id);
        }
        // 检查工号（如果变更了）
        if (StringUtils.hasText(userDTO.getEmployeeNo()) && !userDTO.getEmployeeNo().equals(user.getEmployeeNo())) {
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
        if (!StringUtils.hasText(password) || password.length() < 6) {
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
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : 1); // 默认启用
        return user;
    }

    /**
     * 更新用户基本信息
     *
     * @param user    用户实体
     * @param userDTO 用户DTO
     */
    private void updateUserBasicInfo(SysUser user, UserDTO userDTO) {
        // 更新用户名
        if (StringUtils.hasText(userDTO.getUsername())) {
            user.setUsername(userDTO.getUsername());
        }

        // 更新密码（如果提供了新密码）
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // 更新真实姓名
        if (StringUtils.hasText(userDTO.getRealName())) {
            user.setRealName(userDTO.getRealName());
        }

        // 更新昵称
        if (StringUtils.hasText(userDTO.getNickname())) {
            user.setNickname(userDTO.getNickname());
        }

        // 更新手机号
        if (StringUtils.hasText(userDTO.getPhone())) {
            user.setPhone(userDTO.getPhone());
        }

        // 更新性别
        if (userDTO.getGender() != null) {
            user.setGender(userDTO.getGender());
        }

        // 更新工号
        if (StringUtils.hasText(userDTO.getEmployeeNo())) {
            user.setEmployeeNo(userDTO.getEmployeeNo());
        }

        // 更新部门ID
        if (StringUtils.hasText(userDTO.getDeptId())) {
            user.setDeptId(userDTO.getDeptId());
        }

        // 更新状态
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
        // 更新真实姓名
        if (StringUtils.hasText(userDTO.getRealName())) {
            user.setRealName(userDTO.getRealName());
        }

        // 更新昵称
        if (StringUtils.hasText(userDTO.getNickname())) {
            user.setNickname(userDTO.getNickname());
        }

        // 更新手机号（需要检查是否重复）
        if (StringUtils.hasText(userDTO.getPhone())) {
            if (!userDTO.getPhone().equals(user.getPhone())) {
                checkPhoneNotExists(userDTO.getPhone(), user.getId());
            }
            user.setPhone(userDTO.getPhone());
        }

        // 更新性别
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
        // 如果传了 roleIds，则更新；如果传了空数组，则清空；如果不传，则保持原样
        if (roleIds != null) {
            // 删除原有关联
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

            // 保存新的关联（如果角色列表不为空）
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
            vo.setCreateByName(getUserName(user.getCreateBy()));
        }

        // 查询更新人名称
        if (StringUtils.hasText(user.getUpdateBy())) {
            vo.setUpdateByName(getUserName(user.getUpdateBy()));
        }

        return vo;
    }

    @Override
    public PageResult<UserVO> getUserOptions(UserOptionQueryDTO queryDTO) {
        // 构建基础查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = buildBaseQueryWrapper(
                queryDTO.getSearch(), queryDTO.getUsername(), queryDTO.getRealName(),
                queryDTO.getDeptId(), queryDTO.getStatus(), queryDTO.getStartTime(), queryDTO.getEndTime()
        );

        // Options 接口：明确过滤已删除的记录，按用户名正序
        queryWrapper.eq(SysUser::getDeleted, 0);
        queryWrapper.orderByAsc(SysUser::getUsername);

        // 判断是否使用分页查询
        if (queryDTO.getPage() != null && queryDTO.getPageSize() != null) {
            // 使用分页查询
            Page<SysUser> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
            IPage<SysUser> pageResult = userMapper.selectPage(pageParam, queryWrapper);

            // 转换为VO
            List<UserVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return PageResult.of(voList, pageResult.getTotal());
        } else {
            // 使用 limit 限制（默认行为）
            int maxLimit = QueryHelper.getValidLimit(queryDTO.getLimit());
            queryWrapper.last("LIMIT " + maxLimit);

            // 查询数据（已限制数量）
            List<SysUser> users = userMapper.selectList(queryWrapper);

            // 转换为VO
            List<UserVO> options = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // total 表示实际返回的数量（由于使用了 LIMIT，total = options.size()）
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
