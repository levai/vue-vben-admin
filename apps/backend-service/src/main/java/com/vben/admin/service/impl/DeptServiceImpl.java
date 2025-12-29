package com.vben.admin.service.impl;

import com.vben.admin.core.exception.BusinessException;
import com.vben.admin.mapper.DeptMapper;
import com.vben.admin.model.dto.DeptDTO;
import com.vben.admin.model.entity.SysDept;
import com.vben.admin.model.vo.DeptVO;
import com.vben.admin.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<DeptVO> getDeptList() {
        List<SysDept> depts = deptMapper.selectList(null);
        return buildDeptTree(depts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDept(DeptDTO deptDTO) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(deptDTO, dept);
        if (dept.getPid() == null) {
            dept.setPid("0");
        }
        if (dept.getStatus() == null) {
            dept.setStatus(1);
        }

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

        BeanUtils.copyProperties(deptDTO, dept, "id");
        if (dept.getPid() == null) {
            dept.setPid("0");
        }

        deptMapper.updateById(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(String id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查是否有子部门
        int childCount = deptMapper.countByPid(id);
        if (childCount > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }

        deptMapper.deleteById(id);
    }

    /**
     * 构建部门树
     */
    private List<DeptVO> buildDeptTree(List<SysDept> depts) {
        if (depts == null || depts.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为VO
        List<DeptVO> deptVOs = depts.stream().map(this::convertToVO).collect(Collectors.toList());

        // 构建树形结构
        List<DeptVO> rootDepts = new ArrayList<>();
        Map<String, DeptVO> deptMap = deptVOs.stream()
                .collect(Collectors.toMap(DeptVO::getId, dept -> dept));

        for (DeptVO dept : deptVOs) {
            if ("0".equals(dept.getPid()) || dept.getPid() == null) {
                rootDepts.add(dept);
            } else {
                DeptVO parent = deptMap.get(dept.getPid());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dept);
                }
            }
        }

        return rootDepts;
    }

    /**
     * 转换为VO
     */
    private DeptVO convertToVO(SysDept dept) {
        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(dept, vo);
        // 确保 createTime 被正确复制
        vo.setCreateTime(dept.getCreateTime());
        return vo;
    }
}
