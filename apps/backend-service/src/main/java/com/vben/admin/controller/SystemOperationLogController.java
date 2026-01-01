package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.model.dto.OperationLogQueryDTO;
import com.vben.admin.model.vo.TreeOptionVO;
import com.vben.admin.model.vo.OperationLogVO;
import com.vben.admin.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统操作日志管理控制器
 *
 * @author vben
 */
@Tag(name = "系统操作日志管理", description = "系统操作日志管理接口")
@RestController
@RequestMapping("/system/operation-log")
@RequiredArgsConstructor
public class SystemOperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "获取操作日志列表", description = "获取操作日志列表（支持分页、筛选）")
    @GetMapping
    public BaseResult<PageResult<OperationLogVO>> getList(OperationLogQueryDTO queryDTO) {
        PageResult<OperationLogVO> result = operationLogService.getOperationLogList(queryDTO);
        return new BaseResult<>(result);
    }

    @Operation(summary = "获取操作日志详情", description = "根据ID获取操作日志详细信息")
    @GetMapping("/{id}")
    public BaseResult<OperationLogVO> getById(@PathVariable String id) {
        OperationLogVO operationLog = operationLogService.getOperationLogDetail(id);
        return new BaseResult<>(operationLog);
    }

    @Operation(summary = "删除操作日志", description = "删除操作日志")
    @DeleteMapping("/{id}")
    public BaseResult<Boolean> delete(@PathVariable String id) {
        operationLogService.deleteOperationLog(id);
        return new BaseResult<>(true);
    }

    @Operation(summary = "批量删除操作日志", description = "批量删除操作日志")
    @DeleteMapping("/batch")
    public BaseResult<Boolean> batchDelete(@RequestBody List<String> ids) {
        operationLogService.batchDeleteOperationLog(ids);
        return new BaseResult<>(true);
    }

    @Operation(summary = "获取操作模块列表", description = "获取操作模块列表（用于下拉选项，返回全部数据，支持搜索关键词过滤）")
    @GetMapping("/modules")
    public BaseResult<PageResult<TreeOptionVO>> getModuleList(
            @RequestParam(required = false) String search
    ) {
        PageResult<TreeOptionVO> result = operationLogService.getOperationModuleList(search);
        return new BaseResult<>(result);
    }

    @Operation(summary = "获取操作类型列表", description = "获取操作类型列表（用于下拉选项，返回全部数据，支持搜索关键词过滤）")
    @GetMapping("/types")
    public BaseResult<PageResult<TreeOptionVO>> getTypeList(
            @RequestParam(required = false) String search
    ) {
        PageResult<TreeOptionVO> result = operationLogService.getOperationTypeList(search);
        return new BaseResult<>(result);
    }
}
