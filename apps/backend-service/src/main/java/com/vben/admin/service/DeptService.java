package com.vben.admin.service;

import com.vben.admin.model.dto.DeptDTO;
import com.vben.admin.model.vo.DeptVO;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author vben
 */
public interface DeptService {

    /**
     * 获取部门列表
     *
     * @return 部门列表（树形结构）
     */
    List<DeptVO> getDeptList();

    /**
     * 创建部门
     *
     * @param deptDTO 部门信息
     * @return 部门ID
     */
    String createDept(DeptDTO deptDTO);

    /**
     * 更新部门
     *
     * @param id      部门ID
     * @param deptDTO 部门信息
     */
    void updateDept(String id, DeptDTO deptDTO);

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void deleteDept(String id);

    /**
     * 检查部门名称是否存在
     *
     * @param name 部门名称
     * @param id   部门ID（更新时排除自己）
     * @return 是否存在
     */
    boolean isNameExists(String name, String id);
}
