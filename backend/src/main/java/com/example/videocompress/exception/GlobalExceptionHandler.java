package com.example.videocompress.exception;

import com.example.videocompress.model.ApiResult;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResult.fail(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraint(ConstraintViolationException ex) {
        return ApiResult.fail(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Void> handleBadJson(HttpMessageNotReadableException ex) {
        return ApiResult.fail("请求内容格式错误");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ApiResult<Void> handleBusiness(Exception ex) {
        return ApiResult.fail(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception ex) {
        return ApiResult.fail(ex.getMessage() == null ? "系统异常" : ex.getMessage());
    }
}
