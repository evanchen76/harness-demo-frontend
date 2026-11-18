package com.example.shop.product;

/** 商品狀態，DB 以名稱字串存放（product.status）。 */
public enum ProductStatus {
    DRAFT,
    ON_SHELF,
    OFF_SHELF,
    /** 上架中的商品庫存扣到 0 時自動變成；補貨後自動回到 ON_SHELF。 */
    SOLD_OUT
}
