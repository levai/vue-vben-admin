package com.vben.admin.model.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部门DTO
 *
 * @author vben
 */
@Data
public class DeptDTO {

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    private String name;

    /**
     * 父级ID
     */
    private String pid;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
