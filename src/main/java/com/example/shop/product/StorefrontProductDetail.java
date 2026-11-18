package com.example.shop.product;

import java.math.BigDecimal;

/** 前台詳情：列表欄位加 description。 */
public record StorefrontProductDetail(
        Long id,
        String name,
        BigDecimal price,
        String mainImageUrl,
        String categoryName,
        StockStatus stockStatus,
        Integer remaining,
        String description) {

    static StorefrontProductDetail from(StorefrontProductRow row) {
        StorefrontProductItem item = StorefrontProductItem.from(row);
        return new StorefrontProductDetail(item.id(), item.name(), item.price(), item.mainImageUrl(),
                item.categoryName(), item.stockStatus(), item.remaining(), row.getDescription());
    }
}
