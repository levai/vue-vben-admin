package com.vben.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vben.admin.model.entity.SysDept;
import org.apache.ibatis.annotations.Param;

/**
 * 部门 Mapper
 *
 * @author vben
 */
public interface DeptMapper extends BaseMapper<SysDept> {

    /**
     * 查询是否有子部门
     *
     * @param pid 父级ID
     * @return 子部门数量
     */
    int countByPid(@Param("pid") String pid);

    /**
     * 检查部门名称是否存在
     *
     * @param name 部门名称
     * @param id   部门ID（更新时排除自己）
     * @return 是否存在
     */
    boolean existsByName(@Param("name") String name, @Param("id") String id);
}
