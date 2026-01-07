package com.vben.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vben.admin.model.entity.SysUserRole;

import java.util.List;

/**
 * 用户角色关联 Mapper
 *
 * @author vben
 */
public interface UserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户ID查询角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<String> selectRoleIdsByUserId(String userId);
}
