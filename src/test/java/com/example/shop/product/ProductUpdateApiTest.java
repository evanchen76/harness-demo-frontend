package com.example.shop.product;

import com.example.shop.ApiTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 編輯商品 API。對應 docs/test-cases/product-update.md。
 */
class ProductUpdateApiTest extends ApiTestBase {

    private static final String URL = "/api/admin/products/{id}";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final long ID = 100;
    private static final long MISSING_ID = 999;
    private static final String NEW_NAME = "新名稱";

    @Test
    @DisplayName("TC01 DRAFT 商品只改名稱")
    @Sql("/fixtures/product-update/TC01_draft_name.sql")
    void tc01_draftName() throws Exception {
        Map<String, Object> before = row();
        update(ID, "alice", body("name", NEW_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.id").value(ID))
                .andExpect(jsonPath("$.data.name").value(NEW_NAME))
                .andExpect(jsonPath("$.data.sku").value("SKU-BASE"))
                .andExpect(jsonPath("$.data.categoryId").value(100))
                .andExpect(jsonPath("$.data.price").value(1290))
                .andExpect(jsonPath("$.data.costPrice").value(800))
                .andExpect(jsonPath("$.data.stock").value(50))
                .andExpect(jsonPath("$.data.mainImageUrl").value("https://img.example.com/base.jpg"))
                .andExpect(jsonPath("$.data.description").value("舊描述"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        assertOnlyChanged(before, Map.of("name", NEW_NAME));
    }

    @Test
    @DisplayName("TC02 DRAFT 商品一次改全部可編輯欄位")
    @Sql("/fixtures/product-update/TC02_draft_all_fields.sql")
    void tc02_draftAllFields() throws Exception {
        Map<String, Object> before = row();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", NEW_NAME);
        body.put("categoryId", 102);
        body.put("price", new BigDecimal("1500.5"));
        body.put("costPrice", new BigDecimal("900"));
        body.put("mainImageUrl", "https://img.example.com/new.jpg");
        body.put("description", "新描述");

        update(ID, "alice", json(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.name").value(NEW_NAME))
                .andExpect(jsonPath("$.data.categoryId").value(102))
                .andExpect(jsonPath("$.data.price").value(1500.5))
                .andExpect(jsonPath("$.data.costPrice").value(900))
                .andExpect(jsonPath("$.data.mainImageUrl").value("https://img.example.com/new.jpg"))
                .andExpect(jsonPath("$.data.description").value("新描述"))
                .andExpect(jsonPath("$.data.sku").value("SKU-BASE"))
                .andExpect(jsonPath("$.data.stock").value(50))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        Map<String, Object> changes = new HashMap<>();
        changes.put("name", NEW_NAME);
        changes.put("category_id", 102L);
        changes.put("price", new BigDecimal("1500.5"));
        changes.put("cost_price", new BigDecimal("900"));
        changes.put("main_image_url", "https://img.example.com/new.jpg");
        changes.put("description", "新描述");
        assertOnlyChanged(before, changes);
    }

    @Test
    @DisplayName("TC03 DRAFT 商品把選填欄位清空")
    @Sql("/fixtures/product-update/TC03_draft_clear_optional.sql")
    void tc03_draftClearOptional() throws Exception {
        Map<String, Object> before = row();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", null);
        body.put("mainImageUrl", "");
        body.put("description", "   ");

        update(ID, "alice", json(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.name").doesNotExist())
                .andExpect(jsonPath("$.data.mainImageUrl").doesNotExist())
                .andExpect(jsonPath("$.data.description").doesNotExist());

        Map<String, Object> changes = new HashMap<>();
        changes.put("name", null);
        changes.put("main_image_url", null);
        changes.put("description", null);
        assertOnlyChanged(before, changes);
    }

    @Test
    @DisplayName("TC04 OFF_SHELF 商品可以編輯")
    @Sql("/fixtures/product-update/TC04_off_shelf_editable.sql")
    void tc04_offShelfEditable() throws Exception {
        Map<String, Object> before = row();
        update(ID, "alice", body("price", 1500))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.price").value(1500))
                .andExpect(jsonPath("$.data.status").value("OFF_SHELF"))
                .andExpect(jsonPath("$.data.stock").value(50));

        assertOnlyChanged(before, Map.of("price", new BigDecimal("1500")));
    }

    @Test
    @DisplayName("TC05 SOLD_OUT 商品可以編輯")
    @Sql("/fixtures/product-update/TC05_sold_out_editable.sql")
    void tc05_soldOutEditable() throws Exception {
        Map<String, Object> before = row();
        update(ID, "alice", body("description", "補貨中"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.description").value("補貨中"))
                .andExpect(jsonPath("$.data.status").value("SOLD_OUT"))
                .andExpect(jsonPath("$.data.stock").value(0));

        assertOnlyChanged(before, Map.of("description", "補貨中"));
    }

    @Test
    @DisplayName("TC06 ON_SHELF 商品改名稱與描述成功")
    @Sql("/fixtures/product-update/TC06_on_shelf_name_description.sql")
    void tc06_onShelfNameDescription() throws Exception {
        Map<String, Object> before = row();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", NEW_NAME);
        body.put("description", "新描述");

        update(ID, "alice", json(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.name").value(NEW_NAME))
                .andExpect(jsonPath("$.data.description").value("新描述"))
                .andExpect(jsonPath("$.data.status").value("ON_SHELF"));

        assertOnlyChanged(before, Map.of("name", NEW_NAME, "description", "新描述"));
    }

    @Test
    @DisplayName("TC07 ON_SHELF 商品改價格，仍符合上架條件")
    @Sql("/fixtures/product-update/TC07_on_shelf_price_ok.sql")
    void tc07_onShelfPriceOk() throws Exception {
        Map<String, Object> before = row();
        update(ID, "alice", body("price", 900))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.price").value(900))
                .andExpect(jsonPath("$.data.status").value("ON_SHELF"));

        assertOnlyChanged(before, Map.of("price", new BigDecimal("900")));
    }

    @Test
    @DisplayName("TC08 ON_SHELF 商品價格改成 0，回傳 422 PRODUCT_PRICE_INVALID")
    @Sql("/fixtures/product-update/TC08_on_shelf_price_zero.sql")
    void tc08_onShelfPriceZero() throws Exception {
        assertRejected(body("price", 0), 422, "PRODUCT_PRICE_INVALID");
    }

    @Test
    @DisplayName("TC09 ON_SHELF 商品價格改成低於成本，回傳 422 PRODUCT_PRICE_INVALID")
    @Sql("/fixtures/product-update/TC09_on_shelf_price_below_cost.sql")
    void tc09_onShelfPriceBelowCost() throws Exception {
        assertRejected(body("price", 799), 422, "PRODUCT_PRICE_INVALID");
    }

    @Test
    @DisplayName("TC10 ON_SHELF 商品把名稱清空，回傳 422 PRODUCT_INFO_INCOMPLETE")
    @Sql("/fixtures/product-update/TC10_on_shelf_name_null.sql")
    void tc10_onShelfNameNull() throws Exception {
        assertRejected(body("name", null), 422, "PRODUCT_INFO_INCOMPLETE");
    }

    @Test
    @DisplayName("TC11 ON_SHELF 商品主圖網址改成空白，回傳 422 PRODUCT_INFO_INCOMPLETE")
    @Sql("/fixtures/product-update/TC11_on_shelf_image_blank.sql")
    void tc11_onShelfImageBlank() throws Exception {
        assertRejected(body("mainImageUrl", "   "), 422, "PRODUCT_INFO_INCOMPLETE");
    }

    @Test
    @DisplayName("TC12 ON_SHELF 商品描述改成空字串，回傳 422 PRODUCT_INFO_INCOMPLETE")
    @Sql("/fixtures/product-update/TC12_on_shelf_description_empty.sql")
    void tc12_onShelfDescriptionEmpty() throws Exception {
        assertRejected(body("description", ""), 422, "PRODUCT_INFO_INCOMPLETE");
    }

    @Test
    @DisplayName("TC13 ON_SHELF 商品換成不存在的分類，回傳 422 CATEGORY_NOT_FOUND")
    @Sql("/fixtures/product-update/TC13_on_shelf_category_not_found.sql")
    void tc13_onShelfCategoryNotFound() throws Exception {
        assertRejected(body("categoryId", 999), 422, "CATEGORY_NOT_FOUND");
    }

    @Test
    @DisplayName("TC14 ON_SHELF 商品換成停用的分類，回傳 422 CATEGORY_DISABLED")
    @Sql("/fixtures/product-update/TC14_on_shelf_category_disabled.sql")
    void tc14_onShelfCategoryDisabled() throws Exception {
        assertRejected(body("categoryId", 101), 422, "CATEGORY_DISABLED");
    }

    @Test
    @DisplayName("TC15 DRAFT 商品換成不存在的分類，回傳 422 CATEGORY_NOT_FOUND")
    @Sql("/fixtures/product-update/TC15_draft_category_not_found.sql")
    void tc15_draftCategoryNotFound() throws Exception {
        assertRejected(body("categoryId", 999), 422, "CATEGORY_NOT_FOUND");
    }

    @Test
    @DisplayName("TC16 DRAFT 商品換成停用的分類，回傳 422 CATEGORY_DISABLED")
    @Sql("/fixtures/product-update/TC16_draft_category_disabled.sql")
    void tc16_draftCategoryDisabled() throws Exception {
        assertRejected(body("categoryId", 101), 422, "CATEGORY_DISABLED");
    }

    @Test
    @DisplayName("TC17 body 帶 sku，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-update/TC17_body_sku.sql")
    void tc17_bodySku() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sku", "SKU-OTHER");
        body.put("name", NEW_NAME);
        assertRejected(json(body), 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC18 body 帶 stock，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-update/TC18_body_stock.sql")
    void tc18_bodyStock() throws Exception {
        assertRejected(body("stock", 999), 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC19 body 帶 status，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-update/TC19_body_status.sql")
    void tc19_bodyStatus() throws Exception {
        assertRejected(body("status", "ON_SHELF"), 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC20 body 沒有任何欄位，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-update/TC20_body_empty.sql")
    void tc20_bodyEmpty() throws Exception {
        assertRejected("{}", 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC21 name 超過 200 個字，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-update/TC21_name_too_long.sql")
    void tc21_nameTooLong() throws Exception {
        assertRejected(body("name", "n".repeat(201)), 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC22 price 為負，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-update/TC22_price_negative.sql")
    void tc22_priceNegative() throws Exception {
        assertRejected(body("price", -1), 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC23 price 超過兩位小數，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-update/TC23_price_three_decimals.sql")
    void tc23_priceThreeDecimals() throws Exception {
        assertRejected(body("price", new BigDecimal("10.123")), 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC24 categoryId 傳 null，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-update/TC24_category_id_null.sql")
    void tc24_categoryIdNull() throws Exception {
        assertRejected(body("categoryId", null), 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC25 price 傳 null，回傳 400 PRODUCT_FIELD_INVALID")
    @Sql("/fixtures/product-update/TC25_price_null.sql")
    void tc25_priceNull() throws Exception {
        assertRejected(body("price", null), 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC26 缺少操作人，回傳 400 INVALID_REQUEST")
    @Sql("/fixtures/product-update/TC26_missing_operator.sql")
    void tc26_missingOperator() throws Exception {
        Map<String, Object> before = row();
        mockMvc.perform(patch(URL, ID).contentType(MediaType.APPLICATION_JSON).content(body("name", NEW_NAME)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertThat(row()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC27 商品不存在，回傳 404 PRODUCT_NOT_FOUND")
    @Sql("/fixtures/product-update/TC27_product_not_found.sql")
    void tc27_productNotFound() throws Exception {
        update(MISSING_ID, "alice", body("name", NEW_NAME))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));

        assertThat(productCount()).isZero();
    }

    @Test
    @DisplayName("TC28 商品不存在又帶不可編輯欄位，回傳商品不存在")
    @Sql("/fixtures/product-update/TC28_not_found_and_body_sku.sql")
    void tc28_notFoundAndBodySku() throws Exception {
        update(MISSING_ID, "alice", body("sku", "SKU-OTHER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));

        assertThat(productCount()).isZero();
    }

    @Test
    @DisplayName("TC29 請求內容不合又分類不存在，回傳請求內容的錯")
    @Sql("/fixtures/product-update/TC29_invalid_and_category_not_found.sql")
    void tc29_invalidAndCategoryNotFound() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("categoryId", 999);
        body.put("price", -1);
        assertRejected(json(body), 400, "PRODUCT_FIELD_INVALID");
    }

    @Test
    @DisplayName("TC30 ON_SHELF 商品換不存在的分類又清空名稱，回傳分類的錯")
    @Sql("/fixtures/product-update/TC30_category_not_found_and_name_null.sql")
    void tc30_categoryNotFoundAndNameNull() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("categoryId", 999);
        body.put("name", null);
        assertRejected(json(body), 422, "CATEGORY_NOT_FOUND");
    }

    // ---- helpers ----

    private ResultActions update(long id, String operator, String json) throws Exception {
        return mockMvc.perform(patch(URL, id)
                .header("X-Operator", operator)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    /** 失敗案例：回指定的 HTTP 狀態與 code，商品 100 的每個欄位都跟請求前相同。 */
    private void assertRejected(String json, int httpStatus, String code) throws Exception {
        Map<String, Object> before = row();
        update(ID, "alice", json)
                .andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.code").value(code));

        assertThat(row()).isEqualTo(before);
    }

    private static String body(String field, Object value) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(field, value);
        return json(body);
    }

    private static String json(Map<String, Object> body) {
        try {
            return JSON.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Object> row() {
        return jdbcTemplate.queryForMap("SELECT * FROM product WHERE id = ?", ID);
    }

    private int productCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product", Integer.class);
    }

    /** 成功案例：只有 changes 裡的欄位改變，其他欄位（含 sku、stock、status、on_shelf_time）與請求前相同；update_time 不比。 */
    private void assertOnlyChanged(Map<String, Object> before, Map<String, Object> changes) {
        Map<String, Object> after = row();
        for (Map.Entry<String, Object> column : before.entrySet()) {
            String name = column.getKey();
            if ("update_time".equals(name)) {
                continue;
            }
            Object expected = changes.containsKey(name) ? changes.get(name) : column.getValue();
            Object actual = after.get(name);
            if (expected instanceof BigDecimal) {
                assertThat((BigDecimal) actual).as(name).isEqualByComparingTo((BigDecimal) expected);
            } else if (expected instanceof Number) {
                assertThat(((Number) actual).longValue()).as(name).isEqualTo(((Number) expected).longValue());
            } else {
                assertThat(actual).as(name).isEqualTo(expected);
            }
        }
        assertThat(productCount()).isEqualTo(1);
    }
}
