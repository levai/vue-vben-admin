package com.vben.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门VO
 *
 * @author vben
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeptVO {

    /**
     * 部门ID
     */
    private String id;

    /**
     * 部门名称
     */
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

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 创建人名称
     */
    private String createByName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private String updateBy;

    /**
     * 更新人名称
     */
    private String updateByName;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

    /**
     * 子部门
     */
    private List<DeptVO> children;
}
