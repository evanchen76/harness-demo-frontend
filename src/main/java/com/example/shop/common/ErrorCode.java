package com.example.shop.common;

import org.springframework.http.HttpStatus;

/**
 * 錯誤碼。名稱即回應的 code；新增業務規則時在這裡加一筆。
 */
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "請求參數不正確"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "商品不存在"),
    PRODUCT_ALREADY_ON_SHELF(HttpStatus.CONFLICT, "商品已上架"),
    PRODUCT_INFO_INCOMPLETE(HttpStatus.UNPROCESSABLE_ENTITY, "商品資料不完整"),
    PRODUCT_PRICE_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "商品價格不合理"),
    PRODUCT_STOCK_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "商品庫存必須大於 0"),
    CATEGORY_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY, "分類不存在"),
    CATEGORY_DISABLED(HttpStatus.UNPROCESSABLE_ENTITY, "分類已停用"),
    STOCK_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "調整數量必填，且不可為 0"),
    STOCK_REASON_INVALID(HttpStatus.BAD_REQUEST, "調整原因必填，最多 100 字"),
    PRODUCT_STOCK_INSUFFICIENT(HttpStatus.UNPROCESSABLE_ENTITY, "庫存不足，不能扣成負數"),
    PRODUCT_STOCK_OVERFLOW(HttpStatus.UNPROCESSABLE_ENTITY, "調整後庫存超過上限"),
    PRODUCT_FIELD_INVALID(HttpStatus.BAD_REQUEST, "商品欄位不合法"),
    PRODUCT_SKU_DUPLICATE(HttpStatus.CONFLICT, "sku 已存在");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
