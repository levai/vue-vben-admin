package com.vben.admin.core.validation;

import com.vben.admin.core.utils.ValidationUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ID 有效性校验器
 * 使用 ValidationUtils 统一校验逻辑
 *
 * @author vben
 */
public class ValidIdValidator implements ConstraintValidator<ValidId, String> {

    private String message;

    @Override
    public void initialize(ValidId constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果值为 null，由 @NotNull 或 @NotBlank 处理
        if (value == null) {
            return true;
        }

        // 使用 ValidationUtils 统一校验逻辑
        if (ValidationUtils.isInvalidId(value)) {
            // 禁用默认的约束违反
            context.disableDefaultConstraintViolation();
            // 构建自定义错误消息
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
