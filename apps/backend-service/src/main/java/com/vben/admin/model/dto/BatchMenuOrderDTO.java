package com.vben.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量更新菜单排序请求DTO
 *
 * @author vben
 */
@Data
@Schema(description = "批量更新菜单排序请求DTO")
public class BatchMenuOrderDTO {

    /**
     * 菜单列表
     */
    @Schema(description = "菜单列表")
    @NotEmpty(message = "菜单列表不能为空")
    @Valid
    private List<MenuOrderDTO> menus;
}
