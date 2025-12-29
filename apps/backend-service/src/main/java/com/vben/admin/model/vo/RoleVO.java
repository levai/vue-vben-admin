package com.vben.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色VO
 *
 * @author vben
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
    private String id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 权限列表（权限码列表）
     */
    private List<String> permissions;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
