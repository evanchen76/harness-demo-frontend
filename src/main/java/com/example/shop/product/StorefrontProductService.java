package com.example.shop.product;

import com.example.shop.common.BizException;
import com.example.shop.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** 前台（消費者）商品瀏覽：列表與詳情，唯讀。 */
@Service
public class StorefrontProductService {

    /** 庫存少於這個數字時，前台提示「只剩 N」。 */
    static final int LOW_STOCK_BELOW = 10;

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;
    private static final Pattern POSITIVE_INTEGER = Pattern.compile("[1-9][0-9]*");
    private static final String NOT_FOUND_MESSAGE = "找不到商品";

    private final StorefrontProductMapper mapper;

    public StorefrontProductService(StorefrontProductMapper mapper) {
        this.mapper = mapper;
    }

    /** 參數都是原始字串：不合格的值（不是正整數、size 超過上限、sort 不認得）一律 400 INVALID_REQUEST。 */
    public StorefrontProductPage list(String pageParam, String sizeParam, String sortParam,
                                      String categoryIdParam, String keywordParam) {
        long page = pageParam == null ? 1 : positiveLong(pageParam);
        long sizeValue = sizeParam == null ? DEFAULT_SIZE : positiveLong(sizeParam);
        if (sizeValue > MAX_SIZE) {
            throw new BizException(ErrorCode.INVALID_REQUEST);
        }
        int size = (int) sizeValue;
        String orderBy = orderBy(sortParam);
        Long categoryId = categoryIdParam == null ? null : positiveLong(categoryIdParam);
        String keyword = keyword(keywordParam);

        long total = mapper.count(categoryId, keyword);
        long offset = offset(page, size);
        List<StorefrontProductItem> items = offset >= total ? List.of()
                : mapper.selectPage(categoryId, keyword, orderBy, size, offset)
                        .stream().map(StorefrontProductItem::from).toList();
        return new StorefrontProductPage(items, page, size, total);
    }

    /** 看不到的商品（不存在、id 不是數字、未上架、已售完、分類停用）一律回同一個 404，不透露原因。 */
    public StorefrontProductDetail get(String idParam) {
        StorefrontProductRow row = null;
        if (POSITIVE_INTEGER.matcher(idParam).matches()) {
            try {
                row = mapper.selectVisibleById(Long.parseLong(idParam));
            } catch (NumberFormatException e) {
                row = null;
            }
        }
        if (row == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, NOT_FOUND_MESSAGE, null);
        }
        return StorefrontProductDetail.from(row);
    }

    private static long positiveLong(String value) {
        if (!POSITIVE_INTEGER.matcher(value).matches()) {
            throw new BizException(ErrorCode.INVALID_REQUEST);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BizException(ErrorCode.INVALID_REQUEST);
        }
    }

    /** 排序：同值一律依 id 新到舊，讓分頁順序穩定。 */
    private static String orderBy(String sort) {
        if (sort == null || sort.equals("latest")) {
            return "p.on_shelf_time DESC, p.id DESC";
        }
        return switch (sort) {
            case "price_asc" -> "p.price ASC, p.id DESC";
            case "price_desc" -> "p.price DESC, p.id DESC";
            default -> throw new BizException(ErrorCode.INVALID_REQUEST);
        };
    }

    /** 空白視為沒帶；轉小寫並跳脫 LIKE 的特殊字元（\、%、_），讓使用者輸入只被當成純文字比對。 */
    private static String keyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.strip().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** (page - 1) * size；頁碼大到會溢位時，視為遠超過最後一頁。 */
    private static long offset(long page, int size) {
        try {
            return Math.multiplyExact(page - 1, (long) size);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }
}
