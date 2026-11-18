package com.example.shop.product;

import java.math.BigDecimal;

/** 新增商品的 request body。必填、格式、長度與數值限制由 ProductService 檢查，以回傳指定的錯誤碼。 */
public class ProductCreateRequest {

    private String sku;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String mainImageUrl;
    private String description;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
