package com.example.shop.product;

import com.example.shop.ApiTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 新增商品 API。對應 docs/test-cases/product-create.md。
 */
class ProductCreateApiTest extends ApiTestBase {

    private static final String URL = "/api/admin/products";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String SKU = "SKU-NEW";
    private static final String DUP_SKU = "SKU-DUP";
    private static final long ENABLED_CATEGORY = 100;
    private static final long DISABLED_CATEGORY = 101;

    @Test
    @DisplayName("TC01 只填必填欄位新增成功")
    @Sql("/fixtures/product-create/TC01_required_only.sql")
    void tc01_requiredOnly() throws Exception {
        MvcResult result = create("alice", required())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.sku").value(SKU))
                .andExpect(jsonPath("$.data.categoryId").value(ENABLED_CATEGORY))
                .andExpect(jsonPath("$.data.price").value(1290))
                .andExpect(jsonPath("$.data.costPrice").value(0))
                .andExpect(jsonPath("$.data.stock").value(0))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        JsonNode data = JSON.readTree(result.getResponse().getContentAsString()).path("data");
        for (String field : new String[] {"name", "mainImageUrl", "description", "onShelfTime"}) {
            assertThat(data.path(field).isNull() || data.path(field).isMissingNode()).as(field).isTrue();
        }

        assertThat(productCount()).isEqualTo(1);
        Map<String, Object> row = onlyProduct();
        assertThat(row.get("id")).isEqualTo(data.path("id").asLong());
        assertThat(row.get("sku")).isEqualTo(SKU);
        assertThat(row.get("name")).isNull();
        assertThat(row.get("main_image_url")).isNull();
        assertThat(row.get("description")).isNull();
        assertThat(row.get("on_shelf_time")).isNull();
        assertThat(row.get("status")).isEqualTo("DRAFT");
        assertThat(row.get("stock")).isEqualTo(0);
        assertThat((BigDecimal) row.get("cost_price")).isEqualByComparingTo("0");
        assertThat((BigDecimal) row.get("price")).isEqualByComparingTo("1290");
        assertThat(((Number) row.get("category_id")).longValue()).isEqualTo(ENABLED_CATEGORY);
    }

    @Test
    @DisplayName("TC02 填全部欄位新增成功")
    @Sql("/fixtures/product-create/TC02_all_fields.sql")
    void tc02_allFields() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sku", "SKU-FULL");
        body.put("name", "無線滑鼠");
        body.put("categoryId", ENABLED_CATEGORY);
        body.put("price", new BigDecimal("590"));
        body.put("costPrice", new BigDecimal("300.5"));
        body.put("mainImageUrl", "https://img.example.com/mouse.jpg");
        body.put("description", "靜音、2.4G");

        create("alice", json(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.sku").value("SKU-FULL"))
                .andExpect(jsonPath("$.data.name").value("無線滑鼠"))
                .andExpect(jsonPath("$.data.categoryId").value(ENABLED_CATEGORY))
                .andExpect(jsonPath("$.data.price").value(590))
                .andExpect(jsonPath("$.data.costPrice").value(300.5))
                .andExpect(jsonPath("$.data.mainImageUrl").value("https://img.example.com/mouse.jpg"))
                .andExpect(jsonPath("$.data.description").value("靜音、2.4G"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.stock").value(0));

        assertThat(productCount()).isEqualTo(1);
        Map<String, Object> row = onlyProduct();
        assertThat(row.get("sku")).isEqualTo("SKU-FULL");
        assertThat(row.get("name")).isEqualTo("無線滑鼠");
        assertThat((BigDecimal) row.get("price")).isEqualByComparingTo("590");
        assertThat((BigDecimal) row.get("cost_price")).isEqualByComparingTo("300.5");
        assertThat(row.get("main_image_url")).isEqualTo("https://img.example.com/mouse.jpg");
        assertThat(row.get("description")).isEqualTo("靜音、2.4G");
        assertThat(row.get("status")).isEqualTo("DRAFT");
        assertThat(row.get("stock")).isEqualTo(0);
    }

    @Test
    @DisplayName("TC03 售價與成本為 0 可以新增")
    @Sql("/fixtures/product-create/TC03_price_zero.sql")
    void tc03_priceZero() throws Exception {
        Map<String, Object> body = requiredMap();
        body.put("sku", "SKU-ZERO");
        body.put("price", 0);
        body.put("costPrice", 0);

        create("alice", json(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.price").value(0))
                .andExpect(jsonPath("$.data.costPrice").value(0));

        assertThat(productCount()).isEqualTo(1);
        assertThat((BigDecimal) onlyProduct().get("price")).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("TC04 各欄位剛好在上限可以新增")
    @Sql("/fixtures/product-create/TC04_max_length.sql")
    void tc04_maxLength() throws Exception {
        Map<String, Object> body = requiredMap();
        body.put("sku", "s".repeat(64));
        body.put("name", "n".repeat(200));
        body.put("mainImageUrl", "u".repeat(500));
        body.put("price", new BigDecimal("99999999.99"));
        body.put("costPrice", new BigDecimal("0.01"));

        create("alice", json(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("OK"));

        assertThat(productCount()).isEqualTo(1);
        Map<String, Object> row = onlyProduct();
        assertThat((String) row.get("sku")).hasSize(64);
        assertThat((String) row.get("name")).hasSize(200);
        assertThat((String) row.get("main_image_url")).hasSize(500);
        assertThat((BigDecimal) row.get("price")).isEqualByComparingTo("99999999.99");
    }

    @Test
    @DisplayName("TC05 缺少操作人，回傳 400 INVALID_REQUEST")
    @Sql("/fixtures/product-create/TC05_missing_operator.sql")
    void tc05_missingOperator() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(required()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertNothingCreated();
    }

    @Test
    @DisplayName("TC06 缺少 sku，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC06_missing_sku.sql")
    void tc06_missingSku() throws Exception {
        Map<String, Object> body = requiredMap();
        body.remove("sku");
        assertRejected(json(body), 400, "PRODUCT_FIELD_INVALID");
        assertNothingCreated();
    }

    @Test
    @DisplayName("TC07 缺少 categoryId，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC07_missing_category_id.sql")
    void tc07_missingCategoryId() throws Exception {
        Map<String, Object> body = requiredMap();
        body.remove("categoryId");
        assertRejected(json(body), 400, "PRODUCT_FIELD_INVALID");
        assertNothingCreated();
    }

    @Test
    @DisplayName("TC08 缺少 price，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC08_missing_price.sql")
    void tc08_missingPrice() throws Exception {
        Map<String, Object> body = requiredMap();
        body.remove("price");
        assertRejected(json(body), 400, "PRODUCT_FIELD_INVALID");
        assertNothingCreated();
    }

    @Test
    @DisplayName("TC09 sku 只有空白，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC09_sku_blank.sql")
    void tc09_skuBlank() throws Exception {
        assertInvalidField("sku", "   ");
    }

    @Test
    @DisplayName("TC10 sku 超過 64 個字，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC10_sku_too_long.sql")
    void tc10_skuTooLong() throws Exception {
        assertInvalidField("sku", "s".repeat(65));
    }

    @Test
    @DisplayName("TC11 name 超過 200 個字，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC11_name_too_long.sql")
    void tc11_nameTooLong() throws Exception {
        assertInvalidField("name", "n".repeat(201));
    }

    @Test
    @DisplayName("TC12 mainImageUrl 超過 500 個字，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC12_image_url_too_long.sql")
    void tc12_imageUrlTooLong() throws Exception {
        assertInvalidField("mainImageUrl", "u".repeat(501));
    }

    @Test
    @DisplayName("TC13 price 為負，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC13_price_negative.sql")
    void tc13_priceNegative() throws Exception {
        assertInvalidField("price", new BigDecimal("-1"));
    }

    @Test
    @DisplayName("TC14 price 超過兩位小數，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC14_price_three_decimals.sql")
    void tc14_priceThreeDecimals() throws Exception {
        assertInvalidField("price", new BigDecimal("10.123"));
    }

    @Test
    @DisplayName("TC15 costPrice 為負，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC15_cost_price_negative.sql")
    void tc15_costPriceNegative() throws Exception {
        assertInvalidField("costPrice", new BigDecimal("-0.01"));
    }

    @Test
    @DisplayName("TC16 costPrice 超過兩位小數，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-create/TC16_cost_price_three_decimals.sql")
    void tc16_costPriceThreeDecimals() throws Exception {
        assertInvalidField("costPrice", new BigDecimal("5.555"));
    }

    @Test
    @DisplayName("TC17 sku 已存在，回傳 409 PRODUCT_SKU_DUPLICATE")
    @Sql("/fixtures/product-create/TC17_sku_duplicate.sql")
    void tc17_skuDuplicate() throws Exception {
        Map<String, Object> body = requiredMap();
        body.put("sku", DUP_SKU);
        assertRejected(json(body), 409, "PRODUCT_SKU_DUPLICATE");
        assertOnlyExistingProduct();
    }

    @Test
    @DisplayName("TC18 分類不存在，回傳 422 CATEGORY_NOT_FOUND")
    @Sql("/fixtures/product-create/TC18_category_not_found.sql")
    void tc18_categoryNotFound() throws Exception {
        Map<String, Object> body = requiredMap();
        body.put("categoryId", 999);
        assertRejected(json(body), 422, "CATEGORY_NOT_FOUND");
        assertNothingCreated();
    }

    @Test
    @DisplayName("TC19 分類已停用，回傳 422 CATEGORY_DISABLED")
    @Sql("/fixtures/product-create/TC19_category_disabled.sql")
    void tc19_categoryDisabled() throws Exception {
        Map<String, Object> body = requiredMap();
        body.put("categoryId", DISABLED_CATEGORY);
        assertRejected(json(body), 422, "CATEGORY_DISABLED");
        assertNothingCreated();
    }

    @Test
    @DisplayName("TC20 缺少操作人又缺少 sku，回傳操作人的錯")
    @Sql("/fixtures/product-create/TC20_missing_operator_and_sku.sql")
    void tc20_missingOperatorAndSku() throws Exception {
        Map<String, Object> body = requiredMap();
        body.remove("sku");
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertNothingCreated();
    }

    @Test
    @DisplayName("TC21 請求內容不合又分類不存在，回傳請求內容的錯")
    @Sql("/fixtures/product-create/TC21_invalid_and_category_not_found.sql")
    void tc21_invalidAndCategoryNotFound() throws Exception {
        Map<String, Object> body = requiredMap();
        body.put("categoryId", 999);
        body.put("price", -1);
        assertRejected(json(body), 400, "PRODUCT_FIELD_INVALID");
        assertNothingCreated();
    }

    @Test
    @DisplayName("TC22 分類已停用又 sku 重複，回傳分類的錯")
    @Sql("/fixtures/product-create/TC22_category_disabled_and_sku_duplicate.sql")
    void tc22_categoryDisabledAndSkuDuplicate() throws Exception {
        Map<String, Object> body = requiredMap();
        body.put("sku", DUP_SKU);
        body.put("categoryId", DISABLED_CATEGORY);
        assertRejected(json(body), 422, "CATEGORY_DISABLED");
        assertOnlyExistingProduct();
    }

    // ---- helpers ----

    private ResultActions create(String operator, String json) throws Exception {
        return mockMvc.perform(post(URL)
                .header("X-Operator", operator)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private void assertRejected(String json, int httpStatus, String code) throws Exception {
        create("alice", json)
                .andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.code").value(code));
    }

    /** 只有一個欄位的值不合法，其他欄位合法：回 400 PRODUCT_FIELD_INVALID，DB 不變。 */
    private void assertInvalidField(String field, Object value) throws Exception {
        Map<String, Object> body = requiredMap();
        body.put(field, value);
        assertRejected(json(body), 400, "PRODUCT_FIELD_INVALID");
        assertNothingCreated();
    }

    /** 三個必填欄位都合法：sku、categoryId（啟用中）、price。 */
    private static Map<String, Object> requiredMap() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sku", SKU);
        body.put("categoryId", ENABLED_CATEGORY);
        body.put("price", 1290);
        return body;
    }

    private static String required() {
        return json(requiredMap());
    }

    private static String json(Map<String, Object> body) {
        try {
            return JSON.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private int productCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product", Integer.class);
    }

    private Map<String, Object> onlyProduct() {
        assertThat(productCount()).isEqualTo(1);
        return jdbcTemplate.queryForMap("SELECT * FROM product");
    }

    /** fixture 沒有商品的失敗案例：product 表仍是空的。 */
    private void assertNothingCreated() {
        assertThat(productCount()).isZero();
    }

    /** fixture 有一個 SKU-DUP 商品的失敗案例：表裡還是只有它，欄位沒變。 */
    private void assertOnlyExistingProduct() {
        Map<String, Object> row = onlyProduct();
        assertThat(row.get("sku")).isEqualTo(DUP_SKU);
        assertThat(row.get("name")).isEqualTo("既有商品");
        assertThat(((Number) row.get("category_id")).longValue()).isEqualTo(ENABLED_CATEGORY);
        assertThat((BigDecimal) row.get("price")).isEqualByComparingTo("500");
        assertThat((BigDecimal) row.get("cost_price")).isEqualByComparingTo("300");
        assertThat(row.get("stock")).isEqualTo(10);
        assertThat(row.get("description")).isEqualTo("既有描述");
        assertThat(row.get("status")).isEqualTo("DRAFT");
    }
}
