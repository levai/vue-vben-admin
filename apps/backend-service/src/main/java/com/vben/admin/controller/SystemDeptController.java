package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.model.dto.DeptDTO;
import com.vben.admin.model.vo.DeptVO;
import com.vben.admin.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统部门管理控制器
 *
 * @author vben
 */
@Tag(name = "系统部门管理", description = "系统部门管理接口")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SystemDeptController {

    private final DeptService deptService;

    @Operation(summary = "获取部门列表", description = "获取部门列表（树形结构）")
    @GetMapping
    public BaseResult<List<DeptVO>> getList() {
        List<DeptVO> depts = deptService.getDeptList();
        return new BaseResult<>(depts);
    }

    @Operation(summary = "创建部门", description = "创建新部门")
    @PostMapping
    public BaseResult<String> create(@Valid @RequestBody DeptDTO deptDTO) {
        String id = deptService.createDept(deptDTO);
        return new BaseResult<>(id);
    }

    @Operation(summary = "更新部门", description = "更新部门信息")
    @PutMapping("/{id}")
    public BaseResult<Boolean> update(@PathVariable String id, @Valid @RequestBody DeptDTO deptDTO) {
        deptService.updateDept(id, deptDTO);
        return new BaseResult<>(true);
    }

    @Operation(summary = "删除部门", description = "删除部门")
    @DeleteMapping("/{id}")
    public BaseResult<Boolean> delete(@PathVariable String id) {
        deptService.deleteDept(id);
        return new BaseResult<>(true);
    }
}
