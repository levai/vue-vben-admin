package com.vben.admin.service.impl;

import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.core.utils.TreeHelper;
import com.vben.admin.mapper.DeptMapper;
import com.vben.admin.mapper.UserMapper;
import com.vben.admin.model.dto.DeptDTO;
import com.vben.admin.model.entity.SysDept;
import com.vben.admin.model.entity.SysUser;
import com.vben.admin.model.vo.DeptVO;
import com.vben.admin.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 *
 * @author vben
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;
    private final UserMapper userMapper;

    @Override
    public List<DeptVO> getDeptList() {
        List<SysDept> depts = deptMapper.selectList(null);
        return buildDeptTree(depts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDept(DeptDTO deptDTO) {
        // 检查名称是否已存在
        validateNameNotExists(deptDTO.getName(), null);

        // 创建部门实体
        SysDept dept = buildDeptEntity(deptDTO, null);
        deptMapper.insert(dept);
        return dept.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(String id, DeptDTO deptDTO) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 如果修改了名称，检查新名称是否已存在
        if (deptDTO.getName() != null && !deptDTO.getName().equals(dept.getName())) {
            validateNameNotExists(deptDTO.getName(), id);
        }

        BeanUtils.copyProperties(deptDTO, dept, "id");
        if (dept.getPid() == null) {
            dept.setPid(TreeHelper.ROOT_ID);
        }

        deptMapper.updateById(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(String id) {
        // 查询部门
        SysDept dept = getDeptByIdOrThrow(id);

        // 检查是否有子部门
        checkNoChildDepts(id);

        // 删除部门（逻辑删除）
        deptMapper.deleteById(id);
    }

    /**
     * 构建部门树
     */
    private List<DeptVO> buildDeptTree(List<SysDept> depts) {
        if (depts == null || depts.isEmpty()) {
            return List.of();
        }

        // 转换为VO
        List<DeptVO> deptVOs = depts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 使用 TreeHelper 构建树形结构
        return TreeHelper.buildTree(
                deptVOs,
                DeptVO::getId,
                DeptVO::getPid,
                DeptVO::getChildren,
                DeptVO::setChildren,
                TreeHelper.ROOT_ID
        );
    }

    @Override
    public boolean isNameExists(String name, String id) {
        return deptMapper.existsByName(name, id);
    }

    /**
     * 根据ID获取部门，如果不存在则抛出异常
     *
     * @param id 部门ID
     * @return 部门实体
     * @throws BusinessException 如果部门不存在
     */
    private SysDept getDeptByIdOrThrow(String id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    /**
     * 验证部门名称不存在
     *
     * @param name 部门名称
     * @param id   部门ID（更新时传入，创建时传入null）
     * @throws BusinessException 如果名称已存在
     */
    private void validateNameNotExists(String name, String id) {
        if (isNameExists(name, id)) {
            throw new BusinessException("部门名称已存在");
        }
    }

    /**
     * 检查部门是否有子部门
     *
     * @param id 部门ID
     * @throws BusinessException 如果存在子部门
     */
    private void checkNoChildDepts(String id) {
        int childCount = deptMapper.countByPid(id);
        if (childCount > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }
    }

    /**
     * 构建部门实体
     *
     * @param deptDTO 部门DTO
     * @param id      部门ID（更新时传入，创建时传入null）
     * @return 部门实体
     */
    private SysDept buildDeptEntity(DeptDTO deptDTO, String id) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(deptDTO, dept);
        if (id != null) {
            dept.setId(id);
        }
        if (dept.getPid() == null) {
            dept.setPid(TreeHelper.ROOT_ID);
        }
        if (dept.getStatus() == null) {
            dept.setStatus(1);
        }
        return dept;
    }

    /**
     * 转换为VO
     */
    private DeptVO convertToVO(SysDept dept) {
        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(dept, vo);
        // 确保 createTime 被正确复制
        vo.setCreateTime(dept.getCreateTime());

        // 查询创建人名称
        if (StringUtils.hasText(dept.getCreateBy())) {
            vo.setCreateByName(getUserName(dept.getCreateBy()));
        }

        // 查询更新人名称
        if (StringUtils.hasText(dept.getUpdateBy())) {
            vo.setUpdateByName(getUserName(dept.getUpdateBy()));
        }

        return vo;
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
