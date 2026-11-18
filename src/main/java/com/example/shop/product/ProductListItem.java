package com.example.shop.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品列表的每一筆：只含列表欄位，不含 description、costPrice、createTime。 */
public record ProductListItem(
        Long id,
        String sku,
        String name,
        Long categoryId,
        BigDecimal price,
        Integer stock,
        ProductStatus status,
        String mainImageUrl,
        LocalDateTime onShelfTime,
        LocalDateTime updateTime) {

    static ProductListItem from(Product p) {
        return new ProductListItem(p.getId(), p.getSku(), p.getName(), p.getCategoryId(), p.getPrice(),
                p.getStock(), p.getStatus(), p.getMainImageUrl(), p.getOnShelfTime(), p.getUpdateTime());
    }
}
