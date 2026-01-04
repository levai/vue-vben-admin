package com.vben.admin.core.exception;

import com.vben.admin.core.model.BaseResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author vben
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 500 未知异常
     */
    @ExceptionHandler(value = Exception.class)
    public BaseResult<?> exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        this.logError(request, e);
        // 开发环境返回详细错误信息，生产环境返回通用错误信息
        String message = "服务器异常";
        if (log.isDebugEnabled()) {
            message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        }
        return new BaseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }

    /**
     * 500 自定义业务异常
     */
    @ExceptionHandler(value = BusinessException.class)
    public BaseResult<?> businessHandler(HttpServletRequest request, HttpServletResponse response, BusinessException e) {
        this.logDebug(request, e);
        return new BaseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

    /**
     * 401 认证类异常
     */
    @ExceptionHandler(value = AuthenticationException.class)
    public BaseResult<?> authenticationHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
        this.logDebug(request, e);
        return new BaseResult<>(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    /**
     * 400 参数类型不匹配
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public BaseResult<?> methodArgumentTypeMismatchHandler(HttpServletRequest request, HttpServletResponse response, MethodArgumentTypeMismatchException e) {
        this.logDebug(request, e);
        return new BaseResult<>(HttpStatus.BAD_REQUEST.value(), "参数类型错误" + "[" + e.getName() + "]");
    }

    /**
     * 400 缺少参数
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public BaseResult<?> missingServletRequestParameterHandler(HttpServletRequest request, HttpServletResponse response, MissingServletRequestParameterException e) {
        this.logDebug(request, e);
        return new BaseResult<>(HttpStatus.BAD_REQUEST.value(), "缺少必要参数" + "[" + e.getParameterName() + "]");
    }

    /**
     * 403 没有访问权限
     */
    @ExceptionHandler(AccessDeniedException.class)
    public BaseResult<?> handleAccessDeniedException(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) {
        this.logDebug(request, e);
        return new BaseResult<>(HttpStatus.FORBIDDEN.value(), "没有访问权限");
    }

    /**
     * 404 请求资源不存在
     */
    @ExceptionHandler({NoHandlerFoundException.class})
    public BaseResult<?> noHandlerFoundHandler(HttpServletRequest request, HttpServletResponse response, NoHandlerFoundException e) {
        this.logDebug(request, e);
        return new BaseResult<>(HttpStatus.NOT_FOUND.value(), "资源不存在");
    }

    /**
     * 405 请求 method 不匹配
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public BaseResult<?> httpRequestMethodNotSupportedHandler(HttpServletRequest request, HttpServletResponse response, HttpRequestMethodNotSupportedException e) {
        this.logDebug(request, e);
        return new BaseResult<>(HttpStatus.METHOD_NOT_ALLOWED.value(), "方法不允许");
    }

    /**
     * 413 请求包超出限制大小
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public BaseResult<?> maxUploadSizeExceededExceptionHandler(HttpServletRequest request, MaxUploadSizeExceededException e) {
        this.logDebug(request, e);
        return new BaseResult<>(HttpStatus.PAYLOAD_TOO_LARGE.value(), "请求包超出限制大小");
    }

    /**
     * 处理请求参数格式错误 @RequestBody上使用@Valid 实体上使用@NotNull等，验证失败后抛出的异常是MethodArgumentNotValidException异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public BaseResult<Boolean> methodArgumentNotValidExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException e) {
        this.logDebug(request, e);
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return new BaseResult<>(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 处理方法参数验证异常 @PathVariable、@RequestParam 上使用验证注解失败后抛出的异常是ConstraintViolationException异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public BaseResult<Boolean> constraintViolationExceptionHandler(HttpServletRequest request, ConstraintViolationException e) {
        this.logDebug(request, e);
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return new BaseResult<>(HttpStatus.BAD_REQUEST.value(), message);
    }

    private void logError(HttpServletRequest request, Throwable throwable) {
        log.error("服务请求时发生异常；服务接口: {}", request.getRequestURI(), throwable);
    }

    private void logDebug(HttpServletRequest request, Throwable throwable) {
        log.debug("服务请求时发生异常；服务接口: {}", request.getRequestURI(), throwable);
    }
}
