package com.example.shop.common;

/**
 * 所有 API 的統一回應格式。成功時 code 為 "OK"；失敗時 code 為 {@link ErrorCode} 的名稱。
 */
public record ApiResponse<T>(String code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", "success", data);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, T data) {
        return new ApiResponse<>(errorCode.name(), message, data);
    }
}
