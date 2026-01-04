package com.vben.admin.service;

import com.vben.admin.core.model.PageResult;
import com.vben.admin.core.validation.ValidId;
import com.vben.admin.model.dto.RoleDTO;
import com.vben.admin.model.dto.RoleOptionQueryDTO;
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
     * @param search    搜索关键词（模糊查询角色名称和ID，优先级高于 name/id）
     * @param name      角色名称（模糊查询，与 search 互斥）
     * @param id        角色ID（模糊查询，与 search 互斥）
     * @param remark    备注（模糊查询）
     * @param status    状态（0-禁用，1-启用）
     * @param startTime 开始时间（格式：yyyy-MM-dd）
     * @param endTime   结束时间（格式：yyyy-MM-dd）
     * @return 分页结果
     */
    PageResult<RoleVO> getRoleList(Integer page, Integer pageSize, String search, String name, String id, String remark, Integer status, String startTime, String endTime);

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
    void updateRole(@ValidId(message = "角色ID不能为空或无效值") String id, RoleDTO roleDTO);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(@ValidId(message = "角色ID不能为空或无效值") String id);

    /**
     * 获取角色选项列表（用于下拉选项，支持分页或 limit 限制）
     *
     * @param queryDTO 查询条件
     * @return 角色选项列表（包含所有角色字段，前端自行处理 label 和 value）
     *         如果传了 page 和 pageSize，则使用分页查询；否则使用 limit 限制（默认 1000）
     */
    PageResult<RoleVO> getRoleOptions(RoleOptionQueryDTO queryDTO);
}
