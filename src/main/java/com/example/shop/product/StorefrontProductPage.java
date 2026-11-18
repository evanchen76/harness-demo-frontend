package com.example.shop.product;

import java.util.List;

/** 前台列表的分頁結果。total 是符合條件的總數，不受分頁影響。 */
public record StorefrontProductPage(List<StorefrontProductItem> items, long page, int size, long total) {
}
