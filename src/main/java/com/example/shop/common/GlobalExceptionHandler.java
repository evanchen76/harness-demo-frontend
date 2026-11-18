package com.example.shop.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Object>> handleBiz(BizException e) {
        return ResponseEntity.status(e.errorCode().httpStatus())
                .body(ApiResponse.error(e.errorCode(), e.getMessage(), e.data()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingHeader(MissingRequestHeaderException e) {
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.httpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST, "缺少 header：" + e.getHeaderName(), null));
    }
}
