package com.vben.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 树形选项VO（用于下拉选择，支持树形结构）
 *
 * @author vben
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "树形选项VO（用于下拉选择，支持树形结构）")
public class TreeOptionVO {

    /**
     * 显示标签
     */
    @Schema(description = "显示标签")
    private String label;

    /**
     * 选项值
     */
    @Schema(description = "选项值")
    private String value;

    /**
     * 子选项（用于树形结构）
     */
    @Schema(description = "子选项（用于树形结构）")
    private List<TreeOptionVO> children;
}
