package com.vben.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "菜单VO")
public class MenuVO {

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private String id;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String name;

    /**
     * 路由路径
     */
    @Schema(description = "路由路径")
    private String path;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID")
    private String pid;

    /**
     * 菜单类型
     */
    @Schema(description = "菜单类型（menu/directory/button）")
    private String type;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径")
    private String component;

    /**
     * 重定向路径
     */
    @Schema(description = "重定向路径")
    private String redirect;

    /**
     * 后端权限标识
     */
    @Schema(description = "后端权限标识")
    private String authCode;

    /**
     * 菜单元数据
     */
    @Schema(description = "菜单元数据（包含 title、icon、order 等）")
    private Map<String, Object> meta;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 子菜单
     */
    @Schema(description = "子菜单")
    private List<MenuVO> children;
}
