package com.example.shop.product;

import com.example.shop.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** 新增商品，成功回 201。X-Operator 必填（缺少時回 400 INVALID_REQUEST）；其餘欄位檢查在 Service。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Product> create(@RequestHeader("X-Operator") String operator,
                                       @RequestBody(required = false) ProductCreateRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    /** 編輯商品，只改有傳的欄位。X-Operator 必填；其餘檢查在 Service。 */
    @PatchMapping("/{id}")
    public ApiResponse<Product> update(@PathVariable Long id,
                                       @RequestHeader("X-Operator") String operator,
                                       @RequestBody(required = false) ProductUpdateRequest request) {
        return ApiResponse.ok(productService.update(id, request));
    }

    /** 商品列表：不需要 X-Operator，不接受篩選或分頁參數（有帶也忽略）。 */
    @GetMapping
    public ApiResponse<List<ProductListItem>> list() {
        return ApiResponse.ok(productService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.get(id));
    }

    /** X-Operator 必填（openapi.yaml），缺少時由 GlobalExceptionHandler 回 400 INVALID_REQUEST。 */
    @PostMapping("/{id}/on-shelf")
    public ApiResponse<Product> onShelf(@PathVariable Long id,
                                        @RequestHeader("X-Operator") String operator) {
        return ApiResponse.ok(productService.onShelf(id));
    }

    /** 調整庫存。quantity 正數加、負數扣；必填與長度由 Service 檢查。 */
    @PostMapping("/{id}/stock-adjustments")
    public ApiResponse<Product> adjustStock(@PathVariable Long id,
                                            @RequestHeader("X-Operator") String operator,
                                            @RequestBody(required = false) StockAdjustmentRequest request) {
        return ApiResponse.ok(productService.adjustStock(id, operator, request));
    }
}
