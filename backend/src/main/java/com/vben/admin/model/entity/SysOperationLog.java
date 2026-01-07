package com.vben.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * @author vben
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 操作类型：view/add/edit/delete/export/import/login/logout/download/upload
     */
    private String operationType;

    /**
     * 操作模块：system/user/menu/dept/role等
     */
    private String operationModule;

    /**
     * 操作页面路径（前端页面URL）
     */
    private String operationPage;

    /**
     * 页面名称（从菜单表查询）
     */
    private String pageName;

    /**
     * 请求方法：GET/POST/PUT/DELETE
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 响应状态码
     */
    private Integer responseCode;

    /**
     * 响应数据（JSON格式，可选）
     */
    private String responseData;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理（浏览器信息）
     */
    private String userAgent;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 操作耗时（毫秒）
     */
    private Integer duration;

    /**
     * 状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
