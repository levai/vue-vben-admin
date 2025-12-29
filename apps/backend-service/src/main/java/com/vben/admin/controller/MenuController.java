package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.model.vo.MenuVO;
import com.vben.admin.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器（路由菜单）
 *
 * @author vben
 */
@Tag(name = "菜单管理", description = "菜单相关接口")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取所有菜单", description = "获取所有菜单（用于前端路由生成）")
    @GetMapping("/all")
    public BaseResult<List<MenuVO>> getAllMenus() {
        List<MenuVO> menus = menuService.getAllMenus();
        return new BaseResult<>(menus);
    }
}
