package com.example.shop.common;

/**
 * 業務規則不通過時拋出，由 {@link GlobalExceptionHandler} 轉成對應的 HTTP 狀態與錯誤碼。
 * data 可帶額外資訊（例如缺少的欄位清單），會放進回應的 data。
 */
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object data;

    public BizException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage(), null);
    }

    public BizException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Object data() {
        return data;
    }
}
