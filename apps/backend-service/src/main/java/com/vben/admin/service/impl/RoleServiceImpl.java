package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vben.admin.core.constants.SystemConstants;
import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.core.utils.QueryHelper;
import com.vben.admin.core.utils.SearchQueryConfig;
import com.vben.admin.core.utils.ValidationUtils;
import com.vben.admin.mapper.RoleMapper;
import com.vben.admin.mapper.RoleMenuMapper;
import com.vben.admin.mapper.UserRoleMapper;
import com.vben.admin.model.dto.RoleDTO;
import com.vben.admin.model.dto.RoleOptionQueryDTO;
import com.vben.admin.model.entity.SysRole;
import com.vben.admin.model.entity.SysRoleMenu;
import com.vben.admin.model.vo.RoleVO;
import com.vben.admin.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author vben
 */
@Service
@Validated
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public PageResult<RoleVO> getRoleList(Integer page, Integer pageSize, String search, String name, String id, String remark, Integer status, String startTime, String endTime) {
        // 构建基础查询条件
        LambdaQueryWrapper<SysRole> queryWrapper = buildBaseQueryWrapper(
                search, name, id, remark, status, startTime, endTime
        );

        // List 接口：按创建时间倒序
        queryWrapper.orderByDesc(SysRole::getCreateTime);

        // 分页查询
        Page<SysRole> pageParam = new Page<>(page, pageSize);
        IPage<SysRole> pageResult = roleMapper.selectPage(pageParam, queryWrapper);

        // 转换为VO
        List<RoleVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createRole(RoleDTO roleDTO) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        if (role.getStatus() == null) {
            role.setStatus(1);
        }

        roleMapper.insert(role);

        // 保存角色菜单关联
        if (roleDTO.getPermissions() != null && !roleDTO.getPermissions().isEmpty()) {
            saveRoleMenus(role.getId(), roleDTO.getPermissions());
        }

        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(String id, RoleDTO roleDTO) {
        SysRole role = getRoleByIdOrThrow(id);
        boolean isAdminRole = isAdminRole(role.getId());

        // 更新角色基本信息
        updateRoleBasicInfo(role, roleDTO, isAdminRole);
        roleMapper.updateById(role);

        // 更新权限关联
        updateRolePermissions(id, roleDTO.getPermissions());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String id) {
        SysRole role = getRoleByIdOrThrow(id);

        // 检查是否是超级管理员
        if (isAdminRole(role.getId())) {
            throw new BusinessException("超级管理员角色不能删除");
        }

        // 检查是否有关联用户
        checkRoleHasUsers(id);

        // 删除角色菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));

        // 删除角色（逻辑删除）
        roleMapper.deleteById(id);
    }

    /**
     * 判断是否是超级管理员角色
     *
     * @param roleId 角色ID
     * @return 是否是超级管理员
     */
    private boolean isAdminRole(String roleId) {
        return SystemConstants.ADMIN_ROLE_ID.equals(roleId);
    }

    /**
     * 根据ID获取角色，如果不存在则抛出异常
     *
     * @param id 角色ID
     * @return 角色实体
     * @throws BusinessException 如果角色不存在
     */
    private SysRole getRoleByIdOrThrow(String id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    /**
     * 更新角色基本信息
     *
     * @param role      角色实体
     * @param roleDTO   角色DTO
     * @param isAdminRole 是否是超级管理员
     */
    private void updateRoleBasicInfo(SysRole role, RoleDTO roleDTO, boolean isAdminRole) {
        // 更新名称（超级管理员不允许修改，统一使用 ValidationUtils 校验）
        if (ValidationUtils.isValidString(roleDTO.getName())) {
            if (isAdminRole) {
                throw new BusinessException("超级管理员角色不能修改");
            }
            role.setName(ValidationUtils.cleanString(roleDTO.getName()));
        }

        // 更新状态（超级管理员不允许禁用）
        if (roleDTO.getStatus() != null) {
            if (isAdminRole && roleDTO.getStatus() == 0) {
                throw new BusinessException("超级管理员角色不能禁用");
            }
            role.setStatus(roleDTO.getStatus());
        }

        // 更新备注（允许修改，统一使用 ValidationUtils 校验）
        if (ValidationUtils.isValidString(roleDTO.getRemark())) {
            role.setRemark(ValidationUtils.cleanString(roleDTO.getRemark()));
        } else if (roleDTO.getRemark() != null) {
            // 允许清空备注
            role.setRemark(null);
        }
    }

    /**
     * 更新角色权限关联
     *
     * @param roleId       角色ID
     * @param permissions  权限列表（菜单ID列表）
     */
    private void updateRolePermissions(String roleId, List<String> permissions) {
        // 如果传了 permissions，则更新；如果传了空数组，则清空；如果不传，则保持原样
        if (permissions != null) {
            // 删除原有关联
            roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));

            // 保存新的关联（如果权限列表不为空）
            if (!permissions.isEmpty()) {
                saveRoleMenus(roleId, permissions);
            }
        }
    }

    /**
     * 检查角色是否有关联用户
     *
     * @param roleId 角色ID
     * @throws BusinessException 如果角色已关联用户
     */
    private void checkRoleHasUsers(String roleId) {
        long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<com.vben.admin.model.entity.SysUserRole>()
                        .eq(com.vben.admin.model.entity.SysUserRole::getRoleId, roleId)
        );
        if (userCount > 0) {
            throw new BusinessException("角色已关联用户，无法删除");
        }
    }

    /**
     * 保存角色菜单关联
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    private void saveRoleMenus(String roleId, List<String> menuIds) {
        for (String menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }

    @Override
    public PageResult<RoleVO> getRoleOptions(RoleOptionQueryDTO queryDTO) {
        // 构建基础查询条件
        LambdaQueryWrapper<SysRole> queryWrapper = buildBaseQueryWrapper(
                queryDTO.getSearch(), queryDTO.getName(), queryDTO.getId(),
                queryDTO.getRemark(), queryDTO.getStatus(), queryDTO.getStartTime(), queryDTO.getEndTime()
        );

        // Options 接口：按角色名称正序
        queryWrapper.orderByAsc(SysRole::getName);

        // 判断是否使用分页查询
        if (queryDTO.getPage() != null && queryDTO.getPageSize() != null) {
            // 使用分页查询
            Page<SysRole> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
            IPage<SysRole> pageResult = roleMapper.selectPage(pageParam, queryWrapper);

            // 转换为VO
            List<RoleVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return PageResult.of(voList, pageResult.getTotal());
        } else {
            // 使用 limit 限制（默认行为）
            int maxLimit = QueryHelper.getValidLimit(queryDTO.getLimit());
            queryWrapper.last("LIMIT " + maxLimit);

            // 查询数据（已限制数量）
            List<SysRole> roles = roleMapper.selectList(queryWrapper);

            // 转换为VO
            List<RoleVO> options = roles.stream()
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
     * @param name      角色名称
     * @param id        角色ID
     * @param remark    备注
     * @param status    状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<SysRole> buildBaseQueryWrapper(
            String search, String name, String id, String remark,
            Integer status, String startTime, String endTime
    ) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();

        // 搜索关键词处理（优先级高于 name/id，ID 使用精确匹配）
        QueryHelper.applySearch(
                queryWrapper,
                SearchQueryConfig.<SysRole>of(search)
                        .searchField(SysRole::getName)
                        .exactIdField(SysRole::getId, id)
                        .fallbackField(SysRole::getName, name)
        );

        // 其他查询条件（统一使用 ValidationUtils 校验）
        if (ValidationUtils.isValidString(remark)) {
            queryWrapper.like(SysRole::getRemark, remark);
        }
        if (status != null) {
            queryWrapper.eq(SysRole::getStatus, status);
        }

        // 时间范围查询
        QueryHelper.applyTimeRange(queryWrapper, startTime, endTime, SysRole::getCreateTime);

        return queryWrapper;
    }

    /**
     * 转换为VO
     */
    private RoleVO convertToVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);

        // 查询角色关联的菜单ID列表（作为权限）
        List<String> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, role.getId())
        ).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        vo.setPermissions(menuIds);

        // 确保 createTime 被正确复制
        vo.setCreateTime(role.getCreateTime());

        return vo;
    }
}
