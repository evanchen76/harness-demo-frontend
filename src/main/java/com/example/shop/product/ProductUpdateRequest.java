package com.example.shop.product;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 編輯商品的 request body。PATCH 要分得出「沒傳」和「傳了 null」，所以每個 setter 都記下這個欄位有傳；
 * 不認得的欄位（sku、stock、status 等）由 {@link #unknownField} 收集。
 * 必填、格式、長度與數值限制由 ProductService 檢查，以回傳指定的錯誤碼。
 */
public class ProductUpdateRequest {

    private final Set<String> provided = new LinkedHashSet<>();
    private final Set<String> unknown = new LinkedHashSet<>();

    private String name;
    private Long categoryId;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String mainImageUrl;
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; provided.add("name"); }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; provided.add("categoryId"); }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; provided.add("price"); }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; provided.add("costPrice"); }
    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; provided.add("mainImageUrl"); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; provided.add("description"); }

    @JsonAnySetter
    public void unknownField(String key, Object value) { unknown.add(key); }

    public boolean has(String field) { return provided.contains(field); }
    public boolean hasAnyEditable() { return !provided.isEmpty(); }
    public Set<String> unknownFields() { return unknown; }
}
