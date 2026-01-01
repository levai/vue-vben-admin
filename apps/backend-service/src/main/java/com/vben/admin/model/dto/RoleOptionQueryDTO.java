package com.vben.admin.model.dto;

import lombok.Data;

/**
 * 角色选项查询DTO
 *
 * @author vben
 */
@Data
public class RoleOptionQueryDTO {

    /**
     * 搜索关键词（模糊查询角色名称和ID，优先级高于 name/id）
     */
    private String search;

    /**
     * 角色名称（模糊查询，与 search 互斥）
     */
    private String name;

    /**
     * 角色ID（模糊查询，与 search 互斥）
     */
    private String id;

    /**
     * 备注（模糊查询）
     */
    private String remark;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 开始时间（格式：yyyy-MM-dd）
     */
    private String startTime;

    /**
     * 结束时间（格式：yyyy-MM-dd）
     */
    private String endTime;

    /**
     * 最大返回数量（默认 1000，防止数据量过大）
     */
    private Integer limit = 1000;
}
