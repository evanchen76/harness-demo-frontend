package com.example.shop.product;

import com.example.shop.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 商品上架 API。對應 docs/test-cases/product-on-shelf.md。
 */
class ProductOnShelfApiTest extends ApiTestBase {

    private static final long PRODUCT_ID = 100;
    private static final String URL = "/api/admin/products/{id}/on-shelf";
    private static final LocalDateTime OLD_ON_SHELF_TIME = LocalDateTime.of(2026, 1, 1, 10, 0, 0);

    @Test
    @DisplayName("TC01 草稿商品上架成功")
    @Sql("/fixtures/product-on-shelf/TC01_draft_product.sql")
    void tc01_draftProductOnShelf() throws Exception {
        onShelf(PRODUCT_ID, "alice")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.id").value(PRODUCT_ID))
                .andExpect(jsonPath("$.data.status").value("ON_SHELF"))
                .andExpect(jsonPath("$.data.onShelfTime").isNotEmpty());

        Map<String, Object> product = product(PRODUCT_ID);
        assertThat(product.get("status")).isEqualTo("ON_SHELF");
        assertThat(product.get("on_shelf_time")).isNotNull();
    }

    @Test
    @DisplayName("TC02 已下架商品重新上架成功")
    @Sql("/fixtures/product-on-shelf/TC02_off_shelf_product.sql")
    void tc02_offShelfProductOnShelfAgain() throws Exception {
        onShelf(PRODUCT_ID, "alice")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.status").value("ON_SHELF"));

        Map<String, Object> product = product(PRODUCT_ID);
        assertThat(product.get("status")).isEqualTo("ON_SHELF");
        assertThat((LocalDateTime) product.get("on_shelf_time")).isAfter(OLD_ON_SHELF_TIME);
    }

    @Test
    @DisplayName("TC03 售價等於成本可以上架")
    @Sql("/fixtures/product-on-shelf/TC03_price_equals_cost.sql")
    void tc03_priceEqualsCost() throws Exception {
        onShelf(PRODUCT_ID, "alice")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.status").value("ON_SHELF"));

        assertThat(product(PRODUCT_ID).get("status")).isEqualTo("ON_SHELF");
    }

    @Test
    @DisplayName("TC04 缺少操作人，回傳 400 INVALID_REQUEST")
    @Sql("/fixtures/product-on-shelf/TC04_ready_product.sql")
    void tc04_missingOperator() throws Exception {
        mockMvc.perform(post(URL, PRODUCT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertUnchanged("DRAFT", null);
    }

    @Test
    @DisplayName("TC05 商品不存在，回傳 404 PRODUCT_NOT_FOUND")
    void tc05_productNotFound() throws Exception {
        onShelf(999, "alice")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("TC06 商品已經上架，回傳 409 PRODUCT_ALREADY_ON_SHELF")
    @Sql("/fixtures/product-on-shelf/TC06_already_on_shelf.sql")
    void tc06_alreadyOnShelf() throws Exception {
        assertRejected(409, "PRODUCT_ALREADY_ON_SHELF");
        assertUnchanged("ON_SHELF", OLD_ON_SHELF_TIME);
    }

    @Test
    @DisplayName("TC07 缺少商品名稱，回傳 422 PRODUCT_INFO_INCOMPLETE")
    @Sql("/fixtures/product-on-shelf/TC07_name_null.sql")
    void tc07_nameMissing() throws Exception {
        assertRejected(422, "PRODUCT_INFO_INCOMPLETE");
        assertUnchanged("DRAFT", null);
    }

    @Test
    @DisplayName("TC08 主圖網址只有空白，回傳 422 PRODUCT_INFO_INCOMPLETE")
    @Sql("/fixtures/product-on-shelf/TC08_image_blank.sql")
    void tc08_mainImageBlank() throws Exception {
        assertRejected(422, "PRODUCT_INFO_INCOMPLETE");
        assertUnchanged("DRAFT", null);
    }

    @Test
    @DisplayName("TC09 缺少商品描述，回傳 422 PRODUCT_INFO_INCOMPLETE")
    @Sql("/fixtures/product-on-shelf/TC09_description_null.sql")
    void tc09_descriptionMissing() throws Exception {
        assertRejected(422, "PRODUCT_INFO_INCOMPLETE");
        assertUnchanged("DRAFT", null);
    }

    @Test
    @DisplayName("TC10 售價為 0，回傳 422 PRODUCT_PRICE_INVALID")
    @Sql("/fixtures/product-on-shelf/TC10_price_zero.sql")
    void tc10_priceZero() throws Exception {
        assertRejected(422, "PRODUCT_PRICE_INVALID");
        assertUnchanged("DRAFT", null);
    }

    @Test
    @DisplayName("TC11 售價低於成本，回傳 422 PRODUCT_PRICE_INVALID")
    @Sql("/fixtures/product-on-shelf/TC11_price_below_cost.sql")
    void tc11_priceBelowCost() throws Exception {
        assertRejected(422, "PRODUCT_PRICE_INVALID");
        assertUnchanged("DRAFT", null);
    }

    @Test
    @DisplayName("TC12 庫存為 0，回傳 422 PRODUCT_STOCK_INVALID")
    @Sql("/fixtures/product-on-shelf/TC12_stock_zero.sql")
    void tc12_stockZero() throws Exception {
        assertRejected(422, "PRODUCT_STOCK_INVALID");
        assertUnchanged("DRAFT", null);
    }

    @Test
    @DisplayName("TC13 分類不存在，回傳 422 CATEGORY_NOT_FOUND")
    @Sql("/fixtures/product-on-shelf/TC13_category_not_found.sql")
    void tc13_categoryNotFound() throws Exception {
        assertRejected(422, "CATEGORY_NOT_FOUND");
        assertUnchanged("DRAFT", null);
    }

    @Test
    @DisplayName("TC14 分類已停用，回傳 422 CATEGORY_DISABLED")
    @Sql("/fixtures/product-on-shelf/TC14_category_disabled.sql")
    void tc14_categoryDisabled() throws Exception {
        assertRejected(422, "CATEGORY_DISABLED");
        assertUnchanged("DRAFT", null);
    }

    @Test
    @DisplayName("TC15 同時違反多條規則，回傳第一個 PRODUCT_INFO_INCOMPLETE")
    @Sql("/fixtures/product-on-shelf/TC15_multiple_violations.sql")
    void tc15_multipleViolationsReturnsFirst() throws Exception {
        assertRejected(422, "PRODUCT_INFO_INCOMPLETE");
        assertUnchanged("DRAFT", null);
    }

    private ResultActions onShelf(long id, String operator) throws Exception {
        return mockMvc.perform(post(URL, id).header("X-Operator", operator));
    }

    private void assertRejected(int httpStatus, String code) throws Exception {
        onShelf(PRODUCT_ID, "alice")
                .andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.code").value(code));
    }

    private Map<String, Object> product(long id) {
        return jdbcTemplate.queryForMap("SELECT status, on_shelf_time FROM product WHERE id = ?", id);
    }

    /** 失敗案例：status、on_shelf_time 與 fixture 相同。 */
    private void assertUnchanged(String status, LocalDateTime onShelfTime) {
        Map<String, Object> product = product(PRODUCT_ID);
        assertThat(product.get("status")).isEqualTo(status);
        assertThat(product.get("on_shelf_time")).isEqualTo(onShelfTime);
    }
}
