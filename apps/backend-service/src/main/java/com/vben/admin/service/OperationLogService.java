package com.vben.admin.service;

import com.vben.admin.core.model.PageResult;
import com.vben.admin.core.validation.ValidId;
import com.vben.admin.model.dto.OperationLogQueryDTO;
import com.vben.admin.model.entity.SysOperationLog;
import com.vben.admin.model.vo.TreeOptionVO;
import com.vben.admin.model.vo.OperationLogVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口
 *
 * @author vben
 */
public interface OperationLogService {

    /**
     * 保存操作日志
     *
     * @param operationLog 操作日志实体
     */
    void saveOperationLog(SysOperationLog operationLog);

    /**
     * 异步保存操作日志
     *
     * @param operationLog 操作日志实体
     */
    void saveOperationLogAsync(SysOperationLog operationLog);

    /**
     * 获取操作日志列表（支持分页、筛选）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<OperationLogVO> getOperationLogList(OperationLogQueryDTO queryDTO);

    /**
     * 获取操作日志详情
     *
     * @param id 日志ID
     * @return 操作日志VO
     */
    OperationLogVO getOperationLogDetail(@ValidId(message = "操作日志ID不能为空或无效值") String id);

    /**
     * 删除操作日志
     *
     * @param id 日志ID
     */
    void deleteOperationLog(@ValidId(message = "操作日志ID不能为空或无效值") String id);

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     */
    void batchDeleteOperationLog(List<String> ids);

    /**
     * 清理过期日志
     *
     * @param expireTime 过期时间
     * @return 删除的记录数
     */
    int cleanExpiredLogs(LocalDateTime expireTime);

    /**
     * 获取操作模块列表（用于下拉选项，返回全部数据）
     *
     * @param search  搜索关键词（模糊搜索，可选）
     * @return 操作模块选项列表
     */
    PageResult<TreeOptionVO> getOperationModuleList(String search);

    /**
     * 获取操作类型列表（用于下拉选项，返回全部数据）
     *
     * @param search  搜索关键词（模糊搜索，可选）
     * @return 操作类型选项列表
     */
    PageResult<TreeOptionVO> getOperationTypeList(String search);
}
