package com.example.shop.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.shop.category.Category;
import com.example.shop.category.CategoryMapper;
import com.example.shop.common.BizException;
import com.example.shop.common.ErrorCode;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Taipei");

    private static final int REASON_MAX_LENGTH = 100;
    private static final int SKU_MAX_LENGTH = 64;
    private static final int NAME_MAX_LENGTH = 200;
    private static final int IMAGE_URL_MAX_LENGTH = 500;
    /** price、cost_price 是 DECIMAL(10,2)，超過會寫入失敗，所以當成格式不合。 */
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductStockLogMapper stockLogMapper;

    public ProductService(ProductMapper productMapper, CategoryMapper categoryMapper,
                          ProductStockLogMapper stockLogMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.stockLogMapper = stockLogMapper;
    }

    /** 全部商品，依 id 由小到大；不分頁、不篩選。只查列表需要的欄位。 */
    public List<ProductListItem> list() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                        .select(Product::getId, Product::getSku, Product::getName, Product::getCategoryId,
                                Product::getPrice, Product::getStock, Product::getStatus,
                                Product::getMainImageUrl, Product::getOnShelfTime, Product::getUpdateTime)
                        .orderByAsc(Product::getId))
                .stream().map(ProductListItem::from).toList();
    }

    public Product get(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    /**
     * 新增商品，固定是 DRAFT、庫存 0。檢查順序見 docs/test-cases/product-create.md「規則檢查順序」，
     * 回傳第一個不通過的（X-Operator 已在 Controller 檢查）。
     */
    @Transactional
    public Product create(ProductCreateRequest request) {
        checkCreateRequest(request);
        checkCategory(request.getCategoryId());
        if (productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getSku, request.getSku())) > 0) {
            throw new BizException(ErrorCode.PRODUCT_SKU_DUPLICATE);
        }

        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice() == null ? BigDecimal.ZERO : request.getCostPrice());
        product.setStock(0);
        product.setMainImageUrl(request.getMainImageUrl());
        product.setDescription(request.getDescription());
        product.setStatus(ProductStatus.DRAFT);
        try {
            productMapper.insert(product);
        } catch (DuplicateKeyException e) {
            // 兩個請求同時用同一個 sku：先查再寫擋不住，由 DB 的唯一鍵擋下
            throw new BizException(ErrorCode.PRODUCT_SKU_DUPLICATE);
        }
        return get(product.getId());
    }

    /**
     * 編輯商品，只改有傳的欄位。檢查順序見 docs/test-cases/product-update.md「規則檢查順序」，
     * 回傳第一個不通過的（X-Operator 已在 Controller 檢查）。
     * 先鎖住商品這一列再檢查：編輯和上架、調整庫存同時發生時，看到的狀態不會在檢查之後被改掉。
     */
    @Transactional
    public Product update(Long id, ProductUpdateRequest request) {
        Product product = productMapper.selectByIdForUpdate(id);
        if (product == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        ProductUpdateRequest req = request == null ? new ProductUpdateRequest() : request;
        checkUpdateRequest(req);
        if (req.has("categoryId")) {
            checkCategory(req.getCategoryId());
        }

        // 套上這次要改的欄位，得到編輯後的樣子；ON_SHELF 的商品要先確認這個樣子仍然可以上架
        Product edited = new Product();
        edited.setName(req.has("name") ? blankToNull(req.getName()) : product.getName());
        edited.setCategoryId(req.has("categoryId") ? req.getCategoryId() : product.getCategoryId());
        edited.setPrice(req.has("price") ? req.getPrice() : product.getPrice());
        edited.setCostPrice(req.has("costPrice") ? req.getCostPrice() : product.getCostPrice());
        edited.setMainImageUrl(req.has("mainImageUrl") ? blankToNull(req.getMainImageUrl()) : product.getMainImageUrl());
        edited.setDescription(req.has("description") ? blankToNull(req.getDescription()) : product.getDescription());
        if (product.getStatus() == ProductStatus.ON_SHELF) {
            checkInfoComplete(edited);
            checkPrice(edited);
        }

        LambdaUpdateWrapper<Product> update = new LambdaUpdateWrapper<Product>().eq(Product::getId, id);
        if (req.has("name")) {
            update.set(Product::getName, edited.getName());
        }
        if (req.has("categoryId")) {
            update.set(Product::getCategoryId, edited.getCategoryId());
        }
        if (req.has("price")) {
            update.set(Product::getPrice, edited.getPrice());
        }
        if (req.has("costPrice")) {
            update.set(Product::getCostPrice, edited.getCostPrice());
        }
        if (req.has("mainImageUrl")) {
            update.set(Product::getMainImageUrl, edited.getMainImageUrl());
        }
        if (req.has("description")) {
            update.set(Product::getDescription, edited.getDescription());
        }
        productMapper.update(null, update);
        return get(id);
    }

    /** 不可編輯欄位、至少一個欄位、格式、長度、數值、null 限制；不合的欄位全部列在 message。 */
    private void checkUpdateRequest(ProductUpdateRequest req) {
        if (!req.unknownFields().isEmpty()) {
            throw new BizException(ErrorCode.PRODUCT_FIELD_INVALID,
                    "不可編輯的欄位：" + String.join("、", req.unknownFields()), null);
        }
        if (!req.hasAnyEditable()) {
            throw new BizException(ErrorCode.PRODUCT_FIELD_INVALID,
                    "沒有任何可編輯的欄位：name、categoryId、price、costPrice、mainImageUrl、description", null);
        }
        List<String> invalid = new ArrayList<>();
        if (tooLong(req.getName(), NAME_MAX_LENGTH)) {
            invalid.add("name");
        }
        if (req.has("categoryId") && req.getCategoryId() == null) {
            invalid.add("categoryId");
        }
        if (req.has("price") && !validAmount(req.getPrice(), true)) {
            invalid.add("price");
        }
        if (req.has("costPrice") && !validAmount(req.getCostPrice(), true)) {
            invalid.add("costPrice");
        }
        if (tooLong(req.getMainImageUrl(), IMAGE_URL_MAX_LENGTH)) {
            invalid.add("mainImageUrl");
        }
        if (!invalid.isEmpty()) {
            throw new BizException(ErrorCode.PRODUCT_FIELD_INVALID,
                    "欄位不合法：" + String.join("、", invalid), null);
        }
    }

    /**
     * 上架。檢查順序見 docs/test-cases/product-on-shelf.md「規則檢查順序」，回傳第一個不通過的。
     */
    @Transactional
    public Product onShelf(Long id) {
        Product product = get(id);
        ProductStatus fromStatus = product.getStatus();
        if (fromStatus == ProductStatus.ON_SHELF) {
            throw new BizException(ErrorCode.PRODUCT_ALREADY_ON_SHELF);
        }
        checkInfoComplete(product);
        checkPrice(product);
        checkStock(product);
        checkCategory(product.getCategoryId());

        LocalDateTime now = LocalDateTime.now(ZONE);
        // 帶上原狀態當條件：同時有兩個請求上架同一商品時，只有一個會更新成功
        int updated = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getStatus, fromStatus)
                .set(Product::getStatus, ProductStatus.ON_SHELF)
                .set(Product::getOnShelfTime, now));
        if (updated == 0) {
            throw new BizException(ErrorCode.PRODUCT_ALREADY_ON_SHELF);
        }
        return get(id);
    }

    /**
     * 調整庫存。檢查順序見 docs/test-cases/product-stock-adjust.md「規則檢查順序」，回傳第一個不通過的。
     * 先鎖住商品這一列再計算：同時調整同一商品的請求會排隊，後到的看到的是前一筆調整後的庫存，不會超扣。
     */
    @Transactional
    public Product adjustStock(Long id, String operator, StockAdjustmentRequest request) {
        Integer quantity = request == null ? null : request.getQuantity();
        String reason = request == null ? null : request.getReason();
        if (quantity == null || quantity == 0) {
            throw new BizException(ErrorCode.STOCK_QUANTITY_INVALID);
        }
        if (isBlank(reason) || reason.codePointCount(0, reason.length()) > REASON_MAX_LENGTH) {
            throw new BizException(ErrorCode.STOCK_REASON_INVALID);
        }

        Product product = productMapper.selectByIdForUpdate(id);
        if (product == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        int stockBefore = product.getStock();
        long stockAfter = (long) stockBefore + quantity;
        if (stockAfter < 0) {
            throw new BizException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT,
                    "庫存不足：現有 " + stockBefore + "，要扣 " + (-quantity), null);
        }
        if (stockAfter > Integer.MAX_VALUE) {
            throw new BizException(ErrorCode.PRODUCT_STOCK_OVERFLOW);
        }

        ProductStatus statusBefore = product.getStatus();
        ProductStatus statusAfter = statusAfterAdjust(statusBefore, (int) stockAfter);
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .set(Product::getStock, (int) stockAfter)
                .set(Product::getStatus, statusAfter));

        ProductStockLog log = new ProductStockLog();
        log.setProductId(id);
        log.setQuantity(quantity);
        log.setStockBefore(stockBefore);
        log.setStockAfter((int) stockAfter);
        log.setStatusBefore(statusBefore);
        log.setStatusAfter(statusAfter);
        log.setReason(reason);
        log.setOperator(operator);
        stockLogMapper.insert(log);

        return get(id);
    }

    /** 上架中扣到 0 變售罄；售罄補貨回上架中；其他狀態不變。 */
    private static ProductStatus statusAfterAdjust(ProductStatus status, int stockAfter) {
        if (status == ProductStatus.ON_SHELF && stockAfter == 0) {
            return ProductStatus.SOLD_OUT;
        }
        if (status == ProductStatus.SOLD_OUT && stockAfter > 0) {
            return ProductStatus.ON_SHELF;
        }
        return status;
    }

    /** 必填、格式、長度、數值；不合的欄位全部列在 message。 */
    private void checkCreateRequest(ProductCreateRequest request) {
        List<String> invalid = new ArrayList<>();
        if (request == null) {
            throw new BizException(ErrorCode.PRODUCT_FIELD_INVALID, "缺少欄位：sku、categoryId、price", null);
        }
        if (isBlank(request.getSku()) || tooLong(request.getSku(), SKU_MAX_LENGTH)) {
            invalid.add("sku");
        }
        if (tooLong(request.getName(), NAME_MAX_LENGTH)) {
            invalid.add("name");
        }
        if (request.getCategoryId() == null) {
            invalid.add("categoryId");
        }
        if (!validAmount(request.getPrice(), true)) {
            invalid.add("price");
        }
        if (!validAmount(request.getCostPrice(), false)) {
            invalid.add("costPrice");
        }
        if (tooLong(request.getMainImageUrl(), IMAGE_URL_MAX_LENGTH)) {
            invalid.add("mainImageUrl");
        }
        if (!invalid.isEmpty()) {
            throw new BizException(ErrorCode.PRODUCT_FIELD_INVALID,
                    "欄位不合法：" + String.join("、", invalid), null);
        }
    }

    /** 金額：不可為負、最多兩位小數、不超過 DECIMAL(10,2)；required 為 true 時不可為 null。 */
    private static boolean validAmount(BigDecimal amount, boolean required) {
        if (amount == null) {
            return !required;
        }
        return amount.signum() >= 0
                && amount.stripTrailingZeros().scale() <= 2
                && amount.compareTo(MAX_AMOUNT) <= 0;
    }

    private static boolean tooLong(String value, int max) {
        return value != null && value.codePointCount(0, value.length()) > max;
    }

    private void checkInfoComplete(Product product) {
        List<String> missing = new ArrayList<>();
        if (isBlank(product.getName())) {
            missing.add("name");
        }
        if (isBlank(product.getMainImageUrl())) {
            missing.add("mainImageUrl");
        }
        if (isBlank(product.getDescription())) {
            missing.add("description");
        }
        if (!missing.isEmpty()) {
            throw new BizException(ErrorCode.PRODUCT_INFO_INCOMPLETE,
                    "商品資料不完整，缺少：" + String.join("、", missing), null);
        }
    }

    private void checkPrice(Product product) {
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PRODUCT_PRICE_INVALID, "售價必須大於 0", null);
        }
        if (product.getPrice().compareTo(product.getCostPrice()) < 0) {
            throw new BizException(ErrorCode.PRODUCT_PRICE_INVALID, "售價不能低於成本價", null);
        }
    }

    private void checkStock(Product product) {
        if (product.getStock() <= 0) {
            throw new BizException(ErrorCode.PRODUCT_STOCK_INVALID);
        }
    }

    private void checkCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BizException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (!Boolean.TRUE.equals(category.getEnabled())) {
            throw new BizException(ErrorCode.CATEGORY_DISABLED);
        }
    }

    /** 清空：null、空字串、只有空白都存成 null。 */
    private static String blankToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
