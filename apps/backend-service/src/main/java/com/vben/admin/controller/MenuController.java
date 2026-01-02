package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.model.dto.BatchMenuOrderDTO;
import com.vben.admin.model.dto.MenuDTO;
import com.vben.admin.model.vo.MenuVO;
import com.vben.admin.service.MenuService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单管理控制器（管理端）
 *
 * @author vben
 */
@Api(tags = "系统菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取菜单列表", description = "获取菜单列表")
    @GetMapping
    public BaseResult<List<MenuVO>> getList() {
        List<MenuVO> menus = menuService.getMenuList();
        return new BaseResult<>(menus);
    }

    @Operation(summary = "检查菜单名称是否存在", description = "检查菜单名称是否存在")
    @GetMapping("/name-exists")
    public BaseResult<Boolean> isNameExists(
            @Parameter(description = "菜单名称", required = true)
            @RequestParam String name,
            @Parameter(description = "菜单ID（更新时传入，用于排除自己）")
            @RequestParam(required = false) String id) {
        boolean exists = menuService.isNameExists(name, id);
        return new BaseResult<>(exists);
    }

    @Operation(summary = "检查菜单路径是否存在", description = "检查菜单路径是否存在")
    @GetMapping("/path-exists")
    public BaseResult<Boolean> isPathExists(
            @Parameter(description = "菜单路径", required = true)
            @RequestParam String path,
            @Parameter(description = "菜单ID（更新时传入，用于排除自己）")
            @RequestParam(required = false) String id) {
        boolean exists = menuService.isPathExists(path, id);
        return new BaseResult<>(exists);
    }

    @Operation(summary = "创建菜单", description = "创建新菜单")
    @PostMapping
    public BaseResult<String> create(@Valid @RequestBody MenuDTO menuDTO) {
        String id = menuService.createMenu(menuDTO);
        return new BaseResult<>(id);
    }

    @Operation(summary = "更新菜单", description = "更新菜单信息")
    @PutMapping("/{id}")
    public BaseResult<Boolean> update(@PathVariable String id, @Valid @RequestBody MenuDTO menuDTO) {
        menuService.updateMenu(id, menuDTO);
        return new BaseResult<>(true);
    }

    @Operation(summary = "删除菜单", description = "删除菜单")
    @DeleteMapping("/{id}")
    public BaseResult<Boolean> delete(@PathVariable String id) {
        menuService.deleteMenu(id);
        return new BaseResult<>(true);
    }

    @Operation(summary = "批量更新菜单排序", description = "批量更新菜单的排序和父级关系（用于拖拽排序）")
    @PutMapping("/batch-order")
    public BaseResult<Boolean> batchUpdateOrder(@Valid @RequestBody BatchMenuOrderDTO request) {
        menuService.batchUpdateMenuOrder(request.getMenus());
        return new BaseResult<>(true);
    }
}
