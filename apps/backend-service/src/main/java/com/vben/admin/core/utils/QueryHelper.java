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
     * 默认的 limit 值
     */
    public static final int DEFAULT_LIMIT = 1000;

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
     * @return 有效的 limit 值（如果输入无效，返回默认值）
     */
    public static int getValidLimit(Integer limit) {
        return (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
    }
}
