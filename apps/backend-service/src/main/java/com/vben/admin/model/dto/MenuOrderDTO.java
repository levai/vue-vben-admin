package com.vben.admin.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 菜单排序DTO（用于批量更新排序）
 *
 * @author vben
 */
@Data
public class MenuOrderDTO {

    /**
     * 菜单ID
     */
    @NotBlank(message = "菜单ID不能为空")
    private String id;

    /**
     * 父级ID
     */
    private String pid;

    /**
     * 菜单元数据（包含 order 字段）
     */
    @NotNull(message = "菜单元数据不能为空")
    private Map<String, Object> meta;
}
