package com.vben.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 菜单DTO
 *
 * @author vben
 */
@Data
@Schema(description = "菜单DTO")
public class MenuDTO {

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称", example = "用户管理")
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /**
     * 路由路径
     * 注意：button 类型不需要 path
     */
    @Schema(description = "路由路径（注意：button 类型不需要 path）", example = "/system/user")
    private String path;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID", example = "menu001")
    private String pid;

    /**
     * 菜单类型
     */
    @Schema(description = "菜单类型（menu/directory/button）", example = "menu")
    @NotBlank(message = "菜单类型不能为空")
    private String type;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "views/system/user/list.vue")
    private String component;

    /**
     * 重定向路径
     */
    @Schema(description = "重定向路径", example = "/system/user/list")
    private String redirect;

    /**
     * 后端权限标识
     */
    @Schema(description = "后端权限标识", example = "ac:system:user:view")
    private String authCode;

    /**
     * 菜单元数据
     */
    @Schema(description = "菜单元数据（包含 title、icon、order 等）")
    private Map<String, Object> meta;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}
