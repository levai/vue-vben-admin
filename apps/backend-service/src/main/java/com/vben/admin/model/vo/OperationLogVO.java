package com.vben.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 *
 * @author vben
 */
@Data
@Schema(description = "操作日志VO")
public class OperationLogVO {

    /**
     * 日志ID
     */
    @Schema(description = "日志ID")
    private String id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 操作类型：view/add/edit/delete/export/import/login/logout/download/upload
     */
    @Schema(description = "操作类型：view/add/edit/delete/export/import/login/logout/download/upload")
    private String operationType;

    /**
     * 操作模块：system/user/menu/dept/role等
     */
    @Schema(description = "操作模块：system/user/menu/dept/role等")
    private String operationModule;

    /**
     * 操作页面路径（前端页面URL）
     */
    @Schema(description = "操作页面路径（前端页面URL）")
    private String operationPage;

    /**
     * 页面名称（从菜单表查询）
     */
    @Schema(description = "页面名称（从菜单表查询）")
    private String pageName;

    /**
     * 请求方法：GET/POST/PUT/DELETE
     */
    @Schema(description = "请求方法：GET/POST/PUT/DELETE")
    private String requestMethod;

    /**
     * 请求URL
     */
    @Schema(description = "请求URL")
    private String requestUrl;

    /**
     * 请求参数（JSON格式）
     */
    @Schema(description = "请求参数（JSON格式）")
    private String requestParams;

    /**
     * 响应状态码
     */
    @Schema(description = "响应状态码")
    private Integer responseCode;

    /**
     * 响应数据（JSON格式，可选）
     */
    @Schema(description = "响应数据（JSON格式，可选）")
    private String responseData;

    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ipAddress;

    /**
     * 用户代理（浏览器信息）
     */
    @Schema(description = "用户代理（浏览器信息）")
    private String userAgent;

    /**
     * 浏览器类型
     */
    @Schema(description = "浏览器类型")
    private String browser;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统")
    private String os;

    /**
     * 操作耗时（毫秒）
     */
    @Schema(description = "操作耗时（毫秒）")
    private Integer duration;

    /**
     * 状态：0-失败，1-成功
     */
    @Schema(description = "状态：0-失败，1-成功")
    private Integer status;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
