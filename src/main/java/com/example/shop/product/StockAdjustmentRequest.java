package com.example.shop.product;

/** 調整庫存的 request body。必填與長度由 ProductService 檢查，以回傳指定的錯誤碼。 */
public class StockAdjustmentRequest {

    private Integer quantity;
    private String reason;

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
