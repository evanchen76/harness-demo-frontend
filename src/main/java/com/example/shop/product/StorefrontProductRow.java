package com.example.shop.product;

import java.math.BigDecimal;

/** {@link StorefrontProductMapper} 的查詢結果（商品加分類名稱）；只在 product 領域內部使用。 */
public class StorefrontProductRow {

    private Long id;
    private String name;
    private BigDecimal price;
    private String mainImageUrl;
    private String categoryName;
    private Integer stock;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
