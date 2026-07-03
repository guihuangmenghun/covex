package com.covex.common.exception;

import com.covex.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBizException(BizException e, HttpServletRequest request) {
        log.warn("BizException at {}: {}", request.getRequestURI(), e.getMessage());
        Result<Void> result = Result.fail(e.getCode(), e.getMessage());
        result.setTraceId(getTraceId());
        return result;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("Validation failed: {}", message);
        Result<Void> result = Result.fail(400, message);
        result.setTraceId(getTraceId());
        return result;
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        log.warn("Bind failed: {}", message);
        Result<Void> result = Result.fail(400, message);
        result.setTraceId(getTraceId());
        return result;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), e.getMessage(), e);
        Result<Void> result = Result.fail(500, "服务器内部错误");
        result.setTraceId(getTraceId());
        return result;
    }

    private String getTraceId() {
        // TraceId will be injected by Micrometer via MDC when available
        return org.slf4j.MDC.get("traceId");
    }
}
