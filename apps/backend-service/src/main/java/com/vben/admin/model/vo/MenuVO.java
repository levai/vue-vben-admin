package com.vben.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 菜单VO
 *
 * @author vben
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuVO {

    /**
     * 菜单ID
     */
    private String id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 父级ID
     */
    private String pid;

    /**
     * 菜单类型
     */
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

    /**
     * 子菜单
     */
    private List<MenuVO> children;
}
