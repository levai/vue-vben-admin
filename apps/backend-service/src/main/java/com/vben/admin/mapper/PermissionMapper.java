package com.vben.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vben.admin.model.entity.SysPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限码 Mapper
 *
 * @author vben
 */
public interface PermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据用户ID查询权限码列表
     *
     * @param userId 用户ID
     * @return 权限码列表
     */
    List<String> selectCodesByUserId(@Param("userId") String userId);
}
