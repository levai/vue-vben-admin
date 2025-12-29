package com.vben.admin.service;

import com.vben.admin.core.model.PageResult;
import com.vben.admin.model.dto.RoleDTO;
import com.vben.admin.model.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author vben
 */
public interface RoleService {

    /**
     * 获取角色列表（支持分页）
     *
     * @param page      页码（从1开始）
     * @param pageSize  每页大小
     * @param name      角色名称（模糊查询）
     * @param id        角色ID（模糊查询）
     * @param remark    备注（模糊查询）
     * @param status    状态（0-禁用，1-启用）
     * @param startTime 开始时间（格式：yyyy-MM-dd）
     * @param endTime   结束时间（格式：yyyy-MM-dd）
     * @return 分页结果
     */
    PageResult<RoleVO> getRoleList(Integer page, Integer pageSize, String name, String id, String remark, Integer status, String startTime, String endTime);

    /**
     * 创建角色
     *
     * @param roleDTO 角色信息
     * @return 角色ID
     */
    String createRole(RoleDTO roleDTO);

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param roleDTO 角色信息
     */
    void updateRole(String id, RoleDTO roleDTO);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(String id);
}
