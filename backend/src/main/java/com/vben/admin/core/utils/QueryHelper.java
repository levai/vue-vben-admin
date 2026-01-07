package com.vben.admin.core.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 查询辅助工具类
 * 提供公共的查询逻辑，减少代码重复
 *
 * @author vben
 */
@Slf4j
public class QueryHelper {

    /**
     * 默认的 limit 值（下拉场景推荐）
     */
    public static final int DEFAULT_LIMIT = 300;

    /**
     * 最大允许的 limit 值（防止恶意请求）
     */
    public static final int MAX_ALLOWED_LIMIT = 10000;

    /**
     * 特殊值：表示获取全部数据（实际会被限制为 MAX_ALLOWED_LIMIT）
     */
    public static final int UNLIMITED = -1;

    /**
     * 应用时间范围查询条件
     *
     * @param queryWrapper 查询包装器
     * @param startTime   开始时间（格式：yyyy-MM-dd）
     * @param endTime     结束时间（格式：yyyy-MM-dd）
     * @param timeField   时间字段的 getter 方法引用（MyBatis Plus SFunction）
     * @param <T>         实体类型
     */
    public static <T> void applyTimeRange(
            LambdaQueryWrapper<T> queryWrapper,
            String startTime,
            String endTime,
            SFunction<T, ?> timeField
    ) {
        // 开始时间查询
        if (StringUtils.hasText(startTime)) {
            try {
                LocalDate startDate = LocalDate.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDateTime startDateTime = startDate.atStartOfDay();
                queryWrapper.ge(timeField, startDateTime);
            } catch (DateTimeParseException e) {
                log.warn("无效的开始时间格式: {}, 错误: {}", startTime, e.getMessage());
            }
        }

        // 结束时间查询
        if (StringUtils.hasText(endTime)) {
            try {
                LocalDate endDate = LocalDate.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                queryWrapper.le(timeField, endDateTime);
            } catch (DateTimeParseException e) {
                log.warn("无效的结束时间格式: {}, 错误: {}", endTime, e.getMessage());
            }
        }
    }

    /**
     * 应用 limit 限制，防止数据量过大
     *
     * @param list  数据列表
     * @param limit 最大返回数量（null 或 <= 0 时使用默认值）
     * @param <T>   数据类型
     * @return 处理后的列表（如果超过 limit，只返回前 limit 条）
     */
    public static <T> List<T> applyLimit(List<T> list, Integer limit) {
        if (list == null || list.isEmpty()) {
            return list;
        }

        int maxLimit = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
        if (list.size() > maxLimit) {
            return list.subList(0, maxLimit);
        }
        return list;
    }

    /**
     * 获取有效的 limit 值
     *
     * @param limit 输入的 limit 值
     * @return 有效的 limit 值
     *         - null 或 <= 0：返回默认值 DEFAULT_LIMIT
     *         - -1：返回 MAX_ALLOWED_LIMIT（表示获取全部，但有上限保护）
     *         - 其他：返回原值（但不超过 MAX_ALLOWED_LIMIT）
     */
    public static int getValidLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        // -1 表示获取全部数据（但有上限保护）
        if (limit == UNLIMITED) {
            return MAX_ALLOWED_LIMIT;
        }
        // 限制最大值为 MAX_ALLOWED_LIMIT
        return Math.min(limit, MAX_ALLOWED_LIMIT);
    }

    /**
     * 应用搜索关键词查询条件（支持多字段模糊查询）
     * 如果 search 有值，则使用 search 进行多字段 OR 查询（优先级高）
     * 如果 search 为空，则使用具体字段进行查询
     *
     * @param queryWrapper 查询包装器
     * @param search       搜索关键词
     * @param field1       第一个搜索字段
     * @param field2       第二个搜索字段（可选）
     * @param <T>          实体类型
     */
    public static <T> void applySearchKeyword(
            LambdaQueryWrapper<T> queryWrapper,
            String search,
            SFunction<T, ?> field1,
            SFunction<T, ?> field2
    ) {
        if (StringUtils.hasText(search)) {
            // 使用 search 进行多字段 OR 查询
            queryWrapper.and(wrapper -> {
                wrapper.like(field1, search);
                if (field2 != null) {
                    wrapper.or().like(field2, search);
                }
            });
        }
    }

    /**
     * 应用搜索关键词查询条件
     *
     * <p>统一方法，支持以下场景：</p>
     * <ul>
     *   <li>普通搜索：所有字段模糊匹配</li>
     *   <li>ID 精确匹配：ID 字段精确匹配，其他字段模糊匹配</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * // 普通搜索（链式调用，推荐）
     * QueryHelper.applySearch(
     *     queryWrapper,
     *     SearchQueryConfig.of(search)
     *         .searchField(SysUser::getUsername)
     *         .searchField(SysUser::getRealName)
     *         .fallbackField(SysUser::getUsername, username)
     *         .fallbackField(SysUser::getRealName, realName)
     * );
     *
     * // ID 精确匹配
     * QueryHelper.applySearch(
     *     queryWrapper,
     *     SearchQueryConfig.of(search)
     *         .searchField(SysRole::getName)
     *         .exactIdField(SysRole::getId, id)
     *         .fallbackField(SysRole::getName, name)
     * );
     * }</pre>
     *
     * @param queryWrapper 查询包装器
     * @param config       搜索配置对象
     * @param <T>          实体类型
     * @throws IllegalArgumentException 如果 queryWrapper 或 config 为空，或 searchFields 为空
     */
    public static <T> void applySearch(
            LambdaQueryWrapper<T> queryWrapper,
            SearchQueryConfig<T> config
    ) {
        // 参数校验
        if (queryWrapper == null) {
            throw new IllegalArgumentException("queryWrapper 不能为空");
        }
        if (config == null) {
            throw new IllegalArgumentException("config 不能为空");
        }
        // searchFields 可以为空（如果只使用 fallbackFields 或 idField）
        // 但如果 search 有值且 searchFields 为空，则无法进行搜索
        if (StringUtils.hasText(config.getSearch())
                && (config.getSearchFields() == null || config.getSearchFields().isEmpty())
                && config.getIdField() == null) {
            throw new IllegalArgumentException("当 search 有值时，searchFields 或 idField 至少需要一个");
        }

        if (StringUtils.hasText(config.getSearch())) {
            // 使用 search 进行多字段 OR 查询
            queryWrapper.and(wrapper -> {
                boolean first = true;
                // 搜索字段（模糊查询）
                if (config.getSearchFields() != null) {
                    for (SFunction<T, ?> field : config.getSearchFields()) {
                        if (field != null) {
                            if (first) {
                                wrapper.like(field, config.getSearch());
                                first = false;
                            } else {
                                wrapper.or().like(field, config.getSearch());
                            }
                        }
                    }
                }
                // 如果提供了 ID 字段，也进行精确匹配（适用于角色管理等场景）
                if (config.getIdField() != null) {
                    if (first) {
                        wrapper.eq(config.getIdField(), config.getSearch());
                    } else {
                        wrapper.or().eq(config.getIdField(), config.getSearch());
                    }
                }
            });
        } else {
            // 使用具体字段进行查询
            // ID 精确匹配（如果提供了 ID 字段）
            if (StringUtils.hasText(config.getIdValue()) && config.getIdField() != null) {
                queryWrapper.eq(config.getIdField(), config.getIdValue());
            }
            // 其他字段模糊查询
            if (config.getFallbackFields() != null) {
                config.getFallbackFields().forEach((field, value) -> {
                    if (field != null && StringUtils.hasText(value)) {
                        queryWrapper.like(field, value);
                    }
                });
            }
        }
    }
}
