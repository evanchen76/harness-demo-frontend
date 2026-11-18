package com.example.shop.product;

import java.math.BigDecimal;

/** 前台列表的每一筆：不含 sku、costPrice、stock、description 與其他欄位。 */
public record StorefrontProductItem(
        Long id,
        String name,
        BigDecimal price,
        String mainImageUrl,
        String categoryName,
        StockStatus stockStatus,
        Integer remaining) {

    static StorefrontProductItem from(StorefrontProductRow row) {
        boolean low = row.getStock() < StorefrontProductService.LOW_STOCK_BELOW;
        return new StorefrontProductItem(row.getId(), row.getName(), row.getPrice(), row.getMainImageUrl(),
                row.getCategoryName(), low ? StockStatus.LOW_STOCK : StockStatus.IN_STOCK,
                low ? row.getStock() : null);
    }
}
