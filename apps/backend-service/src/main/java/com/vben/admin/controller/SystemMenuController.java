package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.model.dto.BatchMenuOrderDTO;
import com.vben.admin.model.dto.MenuDTO;
import com.vben.admin.model.vo.MenuVO;
import com.vben.admin.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单管理控制器
 *
 * @author vben
 */
@Tag(name = "系统菜单管理", description = "系统菜单管理接口")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SystemMenuController {

    private final MenuService menuService;

    @Operation(summary = "获取菜单列表", description = "获取菜单列表")
    @GetMapping("/list")
    public BaseResult<List<MenuVO>> getList() {
        List<MenuVO> menus = menuService.getMenuList();
        return new BaseResult<>(menus);
    }

    @Operation(summary = "检查菜单名称是否存在", description = "检查菜单名称是否存在")
    @GetMapping("/name-exists")
    public BaseResult<Boolean> isNameExists(@RequestParam String name,
                                            @RequestParam(required = false) String id) {
        boolean exists = menuService.isNameExists(name, id);
        return new BaseResult<>(exists);
    }

    @Operation(summary = "检查菜单路径是否存在", description = "检查菜单路径是否存在")
    @GetMapping("/path-exists")
    public BaseResult<Boolean> isPathExists(@RequestParam String path,
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
