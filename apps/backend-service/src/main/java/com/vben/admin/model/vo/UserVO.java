package com.vben.admin.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户VO
 *
 * @author vben
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 首页路径
     */
    private String homePath;
}
