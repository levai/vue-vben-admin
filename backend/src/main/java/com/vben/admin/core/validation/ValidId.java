package com.vben.admin.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ID 有效性校验注解
 * 专门用于校验 ID 字段，拦截 "null"、"undefined"、空格等无效字符串
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @ValidId(message = "用户ID不能为空或无效值")
 * private String userId;
 * }
 * </pre>
 *
 * @author vben
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidIdValidator.class)
public @interface ValidId {

    /**
     * 错误消息
     */
    String message() default "ID不能为空或无效值";

    /**
     * 校验分组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};
}
