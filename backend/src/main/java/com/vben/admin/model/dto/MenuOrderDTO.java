package com.vben.admin.model.dto;

import com.vben.admin.core.validation.ValidId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 菜单排序DTO（用于批量更新排序）
 *
 * @author vben
 */
@Data
@Schema(description = "菜单排序DTO（用于批量更新排序）")
public class MenuOrderDTO {

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID", example = "menu001")
    @ValidId(message = "菜单ID不能为空或无效值")
    private String id;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID", example = "menu000")
    private String pid;

    /**
     * 菜单元数据（包含 order 字段）
     */
    @Schema(description = "菜单元数据（包含 order 字段）", example = "{\"order\": 1}")
    @NotNull(message = "菜单元数据不能为空")
    private Map<String, Object> meta;
}
