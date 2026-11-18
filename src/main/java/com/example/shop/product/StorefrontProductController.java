package com.example.shop.product;

import com.example.shop.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 前台商品瀏覽（消費者）：不需登入、不需 X-Operator，唯讀。與後台 /api/admin 分開。 */
@RestController
@RequestMapping("/api/storefront/products")
public class StorefrontProductController {

    private final StorefrontProductService service;

    public StorefrontProductController(StorefrontProductService service) {
        this.service = service;
    }

    /** 參數用字串收進來，由 Service 檢查並回 400 INVALID_REQUEST（不讓 Spring 的型別轉換自己回錯誤格式）。 */
    @GetMapping
    public ApiResponse<StorefrontProductPage> list(@RequestParam(required = false) String page,
                                                   @RequestParam(required = false) String size,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(required = false) String categoryId,
                                                   @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(service.list(page, size, sort, categoryId, keyword));
    }

    /** id 也用字串：不是數字的 id 一律回 404 找不到商品，不回 400。 */
    @GetMapping("/{id}")
    public ApiResponse<StorefrontProductDetail> get(@PathVariable String id) {
        return ApiResponse.ok(service.get(id));
    }
}
