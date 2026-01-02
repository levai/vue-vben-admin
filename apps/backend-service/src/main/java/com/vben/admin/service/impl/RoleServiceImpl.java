package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.core.utils.QueryHelper;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author vben
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    /**
     * 超级管理员角色ID（不允许删除、修改名称、禁用）
     */
    private static final String ADMIN_ROLE_ID = "1";

    /**
     * 超级管理员角色名称
     */
    private static final String ADMIN_ROLE_NAME = "超级管理员";

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public PageResult<RoleVO> getRoleList(Integer page, Integer pageSize, String search, String name, String id, String remark, Integer status, String startTime, String endTime) {
        // 构建查询条件
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();

        // 搜索关键词（优先级高于 name/id）
        if (StringUtils.hasText(search)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysRole::getName, search)
                    .or()
                    .like(SysRole::getId, search)
            );
        } else {
            // 如果没有 search，使用 name 和 id
            if (StringUtils.hasText(name)) {
                queryWrapper.like(SysRole::getName, name);
            }
            if (StringUtils.hasText(id)) {
                queryWrapper.like(SysRole::getId, id);
            }
        }
        if (StringUtils.hasText(remark)) {
            queryWrapper.like(SysRole::getRemark, remark);
        }
        if (status != null) {
            queryWrapper.eq(SysRole::getStatus, status);
        }

        // 时间范围查询
        QueryHelper.applyTimeRange(queryWrapper, startTime, endTime, SysRole::getCreateTime);

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
        // 先查询数据库获取角色信息
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 基于数据库查询到的实际角色ID判断是否是超级管理员
        boolean isAdminRole = ADMIN_ROLE_ID.equals(role.getId());

        // 只更新非空字段
        if (StringUtils.hasText(roleDTO.getName())) {
            // 超级管理员角色不允许修改名称
            if (isAdminRole) {
                throw new BusinessException("超级管理员角色名称不能修改");
            }
            role.setName(roleDTO.getName());
        }
        if (roleDTO.getStatus() != null) {
            // 超级管理员角色不允许禁用
            if (isAdminRole && roleDTO.getStatus() == 0) {
                throw new BusinessException("超级管理员角色不能禁用");
            }
            role.setStatus(roleDTO.getStatus());
        }
        if (StringUtils.hasText(roleDTO.getRemark())) {
            role.setRemark(roleDTO.getRemark());
        } else if (roleDTO.getRemark() != null) {
            // 允许清空备注
            role.setRemark(null);
        }

        roleMapper.updateById(role);

        // 处理权限关联：如果传了 permissions，则更新；如果传了空数组，则清空；如果不传，则保持原样
        if (roleDTO.getPermissions() != null) {
            // 删除原有关联
            roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));

            // 保存新的关联（如果权限列表不为空）
            if (!roleDTO.getPermissions().isEmpty()) {
                saveRoleMenus(id, roleDTO.getPermissions());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String id) {
        // 参数校验
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException("角色ID不能为空");
        }

        String trimmedId = id.trim();

        // 先查询数据库获取角色信息
        SysRole role = roleMapper.selectById(trimmedId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 基于数据库查询到的实际角色ID判断是否是超级管理员（不允许删除）
        if (ADMIN_ROLE_ID.equals(role.getId())) {
            throw new BusinessException("超级管理员角色不能删除");
        }

        // 检查是否有关联用户
        long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<com.vben.admin.model.entity.SysUserRole>()
                        .eq(com.vben.admin.model.entity.SysUserRole::getRoleId, trimmedId)
        );
        if (userCount > 0) {
            throw new BusinessException("角色已关联用户，无法删除");
        }

        // 删除角色菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, trimmedId));

        // 删除角色（逻辑删除）
        roleMapper.deleteById(trimmedId);
    }

    /**
     * 保存角色菜单关联
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
        // 构建查询条件（查询所有字段）
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();

        // 搜索关键词（优先级高于 name/id）
        if (StringUtils.hasText(queryDTO.getSearch())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysRole::getName, queryDTO.getSearch())
                    .or()
                    .like(SysRole::getId, queryDTO.getSearch())
            );
        } else {
            // 如果没有 search，使用 name 和 id
            if (StringUtils.hasText(queryDTO.getName())) {
                queryWrapper.like(SysRole::getName, queryDTO.getName());
            }
            if (StringUtils.hasText(queryDTO.getId())) {
                queryWrapper.like(SysRole::getId, queryDTO.getId());
            }
        }

        // 备注筛选
        if (StringUtils.hasText(queryDTO.getRemark())) {
            queryWrapper.like(SysRole::getRemark, queryDTO.getRemark());
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(SysRole::getStatus, queryDTO.getStatus());
        }

        // 时间范围查询
        QueryHelper.applyTimeRange(queryWrapper, queryDTO.getStartTime(), queryDTO.getEndTime(), SysRole::getCreateTime);

        queryWrapper.orderByAsc(SysRole::getName);

        // 查询数据（应用 limit 限制）
        List<SysRole> roles = roleMapper.selectList(queryWrapper);
        long total = roles.size();

        // 应用 limit 限制（防止数据量过大）
        roles = QueryHelper.applyLimit(roles, queryDTO.getLimit());

        List<RoleVO> options = roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // total 表示实际总数（可能 > items.length，如果被 limit 截断）
        return new PageResult<>(options, total);
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
