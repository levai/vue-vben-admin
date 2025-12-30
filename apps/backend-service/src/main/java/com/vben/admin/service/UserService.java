package com.vben.admin.service;

import com.vben.admin.core.model.PageResult;
import com.vben.admin.model.dto.UserDTO;
import com.vben.admin.model.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author vben
 */
public interface UserService {

    /**
     * 获取用户列表（支持分页）
     *
     * @param page      页码（从1开始）
     * @param pageSize  每页大小
     * @param username  用户名（模糊查询）
     * @param realName  真实姓名（模糊查询）
     * @param deptId    部门ID
     * @param status    状态（0-禁用，1-启用）
     * @param startTime 开始时间（格式：yyyy-MM-dd）
     * @param endTime   结束时间（格式：yyyy-MM-dd）
     * @return 分页结果
     */
    PageResult<UserVO> getUserList(Integer page, Integer pageSize, String username, String realName, String deptId, Integer status, String startTime, String endTime);

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserVO getUserById(String id);

    /**
     * 创建用户
     *
     * @param userDTO 用户信息
     * @return 用户ID
     */
    String createUser(UserDTO userDTO);

    /**
     * 更新用户
     *
     * @param id      用户ID
     * @param userDTO 用户信息
     */
    void updateUser(String id, UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(String id);

    /**
     * 启用/禁用用户
     *
     * @param id     用户ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateUserStatus(String id, Integer status);

    /**
     * 重置用户密码
     *
     * @param id       用户ID
     * @param password 新密码
     */
    void resetPassword(String id, String password);
}
