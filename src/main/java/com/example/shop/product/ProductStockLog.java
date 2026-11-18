package com.example.shop.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/** 商品庫存調整紀錄，每次成功調整一筆。create_time 由 DB 預設值寫入。 */
@TableName("product_stock_log")
public class ProductStockLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Integer quantity;
    private Integer stockBefore;
    private Integer stockAfter;
    private ProductStatus statusBefore;
    private ProductStatus statusAfter;
    private String reason;
    private String operator;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getStockBefore() { return stockBefore; }
    public void setStockBefore(Integer stockBefore) { this.stockBefore = stockBefore; }
    public Integer getStockAfter() { return stockAfter; }
    public void setStockAfter(Integer stockAfter) { this.stockAfter = stockAfter; }
    public ProductStatus getStatusBefore() { return statusBefore; }
    public void setStatusBefore(ProductStatus statusBefore) { this.statusBefore = statusBefore; }
    public ProductStatus getStatusAfter() { return statusAfter; }
    public void setStatusAfter(ProductStatus statusAfter) { this.statusAfter = statusAfter; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
