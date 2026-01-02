package com.vben.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户VO
 *
 * @author vben
 */
@Data
@Schema(description = "用户VO")
public class UserVO {

    /**
     * 显示标签（用于下拉选项显示，可选）
     */
    @Schema(description = "显示标签（用于下拉选项显示，可选）")
    private String label;

    /**
     * 选项值（用于下拉选项值，可选）
     */
    @Schema(description = "选项值（用于下拉选项值，可选）")
    private String value;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String id;

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
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    /**
     * 工号
     */
    @Schema(description = "工号")
    private String employeeNo;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private String deptId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 角色列表（角色ID列表）
     */
    @Schema(description = "角色列表（角色ID列表）")
    private List<String> roles;

    /**
     * 角色名称列表
     */
    @Schema(description = "角色名称列表")
    private List<String> roleNames;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private String createBy;

    /**
     * 创建人名称
     */
    @Schema(description = "创建人名称")
    private String createByName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private String updateBy;

    /**
     * 更新人名称
     */
    @Schema(description = "更新人名称")
    private String updateByName;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
}
