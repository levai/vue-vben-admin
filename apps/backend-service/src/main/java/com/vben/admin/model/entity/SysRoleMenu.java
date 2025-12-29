package com.vben.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单关联实体
 *
 * @author vben
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 角色ID
     */
    private String roleId;

    /**
     * 菜单ID
     */
    private String menuId;
}
