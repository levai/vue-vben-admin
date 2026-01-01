package com.vben.admin.model.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 菜单DTO
 *
 * @author vben
 */
@Data
public class MenuDTO {

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /**
     * 路由路径
     * 注意：button 类型不需要 path
     */
    private String path;

    /**
     * 父级ID
     */
    private String pid;

    /**
     * 菜单类型
     */
    @NotBlank(message = "菜单类型不能为空")
    private String type;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 重定向路径
     */
    private String redirect;

    /**
     * 后端权限标识
     */
    private String authCode;

    /**
     * 菜单元数据
     */
    private Map<String, Object> meta;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
