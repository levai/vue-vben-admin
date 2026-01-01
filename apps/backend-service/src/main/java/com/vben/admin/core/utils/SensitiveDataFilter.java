package com.vben.admin.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 敏感信息过滤工具
 *
 * @author vben
 */
@Slf4j
public class SensitiveDataFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 敏感字段列表
     */
    private static final String[] SENSITIVE_FIELDS = {
            "password", "pwd", "passwd", "pass",
            "token", "accessToken", "refreshToken",
            "secret", "secretKey", "apiKey", "apikey",
            "authorization"
    };

    /**
     * 最大内容长度（字符数）
     */
    private static final int MAX_CONTENT_LENGTH = 5000;

    /**
     * 过滤敏感信息
     *
     * @param content 原始内容（JSON字符串）
     * @return 过滤后的内容
     */
    public static String filterSensitiveData(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }

        // 如果内容过长，直接截断
        if (content.length() > MAX_CONTENT_LENGTH) {
            return content.substring(0, MAX_CONTENT_LENGTH) + "...(内容过长，已截断)";
        }

        // 尝试解析JSON并过滤敏感字段
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            filterSensitiveFields(jsonNode);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            // 如果不是JSON格式，直接返回（可能是普通字符串）
            log.debug("内容不是JSON格式，跳过敏感信息过滤: {}", e.getMessage());
            return content;
        }
    }

    /**
     * 递归过滤JSON节点中的敏感字段
     */
    private static void filterSensitiveFields(JsonNode node) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey().toLowerCase();
                JsonNode value = entry.getValue();

                // 检查是否是敏感字段
                for (String sensitiveField : SENSITIVE_FIELDS) {
                    if (fieldName.contains(sensitiveField.toLowerCase())) {
                        // 替换为***
                        if (value.isTextual()) {
                            ((com.fasterxml.jackson.databind.node.ObjectNode) node).put(entry.getKey(), "***");
                        } else {
                            ((com.fasterxml.jackson.databind.node.ObjectNode) node).put(entry.getKey(), "***");
                        }
                        break;
                    }
                }

                // 递归处理子节点
                if (value.isObject() || value.isArray()) {
                    filterSensitiveFields(value);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                filterSensitiveFields(arrayItem);
            }
        }
    }

    /**
     * 限制内容长度
     *
     * @param content 原始内容
     * @return 限制长度后的内容
     */
    public static String limitLength(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }
        if (content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_CONTENT_LENGTH) + "...(内容过长，已截断)";
    }
}
