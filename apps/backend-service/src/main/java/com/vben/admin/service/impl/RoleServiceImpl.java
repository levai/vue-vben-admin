package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.mapper.RoleMapper;
import com.vben.admin.mapper.RoleMenuMapper;
import com.vben.admin.mapper.UserRoleMapper;
import com.vben.admin.model.dto.RoleDTO;
import com.vben.admin.model.entity.SysRole;
import com.vben.admin.model.entity.SysRoleMenu;
import com.vben.admin.model.vo.RoleVO;
import com.vben.admin.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
 * 角色服务实现
 *
 * @author vben
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public PageResult<RoleVO> getRoleList(Integer page, Integer pageSize, String name, String id, String remark, Integer status, String startTime, String endTime) {
        // 构建查询条件
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like(SysRole::getName, name);
        }
        if (StringUtils.hasText(id)) {
            queryWrapper.like(SysRole::getId, id);
        }
        if (StringUtils.hasText(remark)) {
            queryWrapper.like(SysRole::getRemark, remark);
        }
        if (status != null) {
            queryWrapper.eq(SysRole::getStatus, status);
        }

        // 时间范围查询
        if (StringUtils.hasText(startTime)) {
            try {
                LocalDate startDate = LocalDate.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDateTime startDateTime = startDate.atStartOfDay();
                queryWrapper.ge(SysRole::getCreateTime, startDateTime);
            } catch (DateTimeParseException e) {
                // 忽略无效的日期格式
            }
        }
        if (StringUtils.hasText(endTime)) {
            try {
                LocalDate endDate = LocalDate.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                queryWrapper.le(SysRole::getCreateTime, endDateTime);
            } catch (DateTimeParseException e) {
                // 忽略无效的日期格式
            }
        }

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
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 只更新非空字段
        if (StringUtils.hasText(roleDTO.getName())) {
            role.setName(roleDTO.getName());
        }
        if (roleDTO.getStatus() != null) {
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
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 检查是否有关联用户
        long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<com.vben.admin.model.entity.SysUserRole>()
                        .eq(com.vben.admin.model.entity.SysUserRole::getRoleId, id)
        );
        if (userCount > 0) {
            throw new BusinessException("角色已关联用户，无法删除");
        }

        // 删除角色菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));

        // 删除角色
        roleMapper.deleteById(id);
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
