package com.example.shop.product;

import com.example.shop.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 查詢商品 API。也是測試寫法的範本：一個案例一個方法、一份 fixture，Given / When / Then 對應測試案例文件。
 */
class ProductQueryApiTest extends ApiTestBase {

    @Test
    @DisplayName("TC01 查詢存在的商品，回傳商品資料")
    @Sql("/fixtures/product-query/TC01_existing_product.sql")
    void tc01_existingProduct() throws Exception {
        mockMvc.perform(get("/api/admin/products/{id}", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.sku").value("TEST-Q-001"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @DisplayName("TC02 查詢不存在的商品，回傳 404 PRODUCT_NOT_FOUND")
    void tc02_productNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/products/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }
}
