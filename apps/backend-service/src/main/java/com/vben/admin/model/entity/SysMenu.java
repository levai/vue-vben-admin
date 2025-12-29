package com.vben.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单实体
 *
 * @author vben
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    /**
     * 菜单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 菜单类型：catalog-目录，menu-菜单，embedded-内嵌，link-外链，button-按钮
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
     * 菜单元数据（JSON格式）
     */
    private String meta;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
