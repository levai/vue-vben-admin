package com.vben.admin.model.dto;

import lombok.Data;

/**
 * 操作日志查询DTO
 *
 * @author vben
 */
@Data
public class OperationLogQueryDTO {

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作类型：view/add/edit/delete/export/import/login/logout/download/upload
     */
    private String operationType;

    /**
     * 操作模块：system/user/menu/dept/role等
     */
    private String operationModule;

    /**
     * 状态：0-失败，1-成功
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
     * 搜索关键词（模糊查询操作标题、请求URL等）
     */
    private String search;
}
