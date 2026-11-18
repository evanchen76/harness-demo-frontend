package com.example.shop.product;

import com.example.shop.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 商品調整庫存 API。對應 docs/test-cases/product-stock-adjust.md。
 */
class ProductStockAdjustApiTest extends ApiTestBase {

    private static final long PRODUCT_ID = 100;
    private static final String URL = "/api/admin/products/{id}/stock-adjustments";
    private static final String REASON = "盤點修正";
    private static final LocalDateTime OLD_ON_SHELF_TIME = LocalDateTime.of(2026, 1, 1, 10, 0, 0);

    @Test
    @DisplayName("TC01 上架中的商品加庫存")
    @Sql("/fixtures/product-stock-adjust/TC01_on_shelf_add.sql")
    void tc01_onShelfAdd() throws Exception {
        adjust(PRODUCT_ID, "alice", body(10, REASON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.id").value(PRODUCT_ID))
                .andExpect(jsonPath("$.data.stock").value(60))
                .andExpect(jsonPath("$.data.status").value("ON_SHELF"));

        assertProduct(60, "ON_SHELF", OLD_ON_SHELF_TIME);
        Map<String, Object> log = onlyLog();
        assertLog(log, 10, 50, 60, "ON_SHELF", "ON_SHELF");
        assertThat(log.get("reason")).isEqualTo(REASON);
        assertThat(log.get("operator")).isEqualTo("alice");
        assertThat(log.get("create_time")).isNotNull();
    }

    @Test
    @DisplayName("TC02 上架中的商品扣庫存")
    @Sql("/fixtures/product-stock-adjust/TC02_on_shelf_deduct.sql")
    void tc02_onShelfDeduct() throws Exception {
        adjust(PRODUCT_ID, "alice", body(-20, REASON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.stock").value(30))
                .andExpect(jsonPath("$.data.status").value("ON_SHELF"));

        assertProduct(30, "ON_SHELF", OLD_ON_SHELF_TIME);
        assertLog(onlyLog(), -20, 50, 30, "ON_SHELF", "ON_SHELF");
    }

    @Test
    @DisplayName("TC03 上架中的商品扣到 0 變售罄")
    @Sql("/fixtures/product-stock-adjust/TC03_on_shelf_deduct_to_zero.sql")
    void tc03_onShelfDeductToZeroBecomesSoldOut() throws Exception {
        adjust(PRODUCT_ID, "alice", body(-50, REASON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.stock").value(0))
                .andExpect(jsonPath("$.data.status").value("SOLD_OUT"));

        assertProduct(0, "SOLD_OUT", OLD_ON_SHELF_TIME);
        assertLog(onlyLog(), -50, 50, 0, "ON_SHELF", "SOLD_OUT");
    }

    @Test
    @DisplayName("TC04 售罄商品補貨回到上架中")
    @Sql("/fixtures/product-stock-adjust/TC04_sold_out_restock.sql")
    void tc04_soldOutRestockBackOnShelf() throws Exception {
        adjust(PRODUCT_ID, "alice", body(5, REASON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.stock").value(5))
                .andExpect(jsonPath("$.data.status").value("ON_SHELF"));

        assertProduct(5, "ON_SHELF", OLD_ON_SHELF_TIME);
        assertLog(onlyLog(), 5, 0, 5, "SOLD_OUT", "ON_SHELF");
    }

    @Test
    @DisplayName("TC05 草稿商品扣到 0，狀態不變")
    @Sql("/fixtures/product-stock-adjust/TC05_draft_deduct_to_zero.sql")
    void tc05_draftDeductToZeroKeepsStatus() throws Exception {
        adjust(PRODUCT_ID, "alice", body(-50, REASON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.stock").value(0))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        assertProduct(0, "DRAFT", null);
        assertLog(onlyLog(), -50, 50, 0, "DRAFT", "DRAFT");
    }

    @Test
    @DisplayName("TC06 已下架商品加庫存，狀態不變")
    @Sql("/fixtures/product-stock-adjust/TC06_off_shelf_add.sql")
    void tc06_offShelfAddKeepsStatus() throws Exception {
        adjust(PRODUCT_ID, "alice", body(30, REASON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.stock").value(30))
                .andExpect(jsonPath("$.data.status").value("OFF_SHELF"));

        assertProduct(30, "OFF_SHELF", OLD_ON_SHELF_TIME);
        assertLog(onlyLog(), 30, 0, 30, "OFF_SHELF", "OFF_SHELF");
    }

    @Test
    @DisplayName("TC07 原因剛好 100 個中文字")
    @Sql("/fixtures/product-stock-adjust/TC07_reason_100_chars.sql")
    void tc07_reasonExactly100Chars() throws Exception {
        String reason = "調".repeat(100);
        adjust(PRODUCT_ID, "alice", body(1, reason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.stock").value(51));

        assertThat(onlyLog().get("reason")).isEqualTo(reason);
    }

    @Test
    @DisplayName("TC08 缺少操作人，回傳 400 INVALID_REQUEST")
    @Sql("/fixtures/product-stock-adjust/TC08_missing_operator.sql")
    void tc08_missingOperator() throws Exception {
        mockMvc.perform(post(URL, PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(10, REASON)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertUnchanged();
    }

    @Test
    @DisplayName("TC09 調整數量為 0，回傳 400 STOCK_QUANTITY_INVALID")
    @Sql("/fixtures/product-stock-adjust/TC09_quantity_zero.sql")
    void tc09_quantityZero() throws Exception {
        assertRejected(body(0, REASON), 400, "STOCK_QUANTITY_INVALID");
        assertUnchanged();
    }

    @Test
    @DisplayName("TC10 缺少調整數量，回傳 400 STOCK_QUANTITY_INVALID")
    @Sql("/fixtures/product-stock-adjust/TC10_quantity_missing.sql")
    void tc10_quantityMissing() throws Exception {
        assertRejected("{\"reason\":\"" + REASON + "\"}", 400, "STOCK_QUANTITY_INVALID");
        assertUnchanged();
    }

    @Test
    @DisplayName("TC11 缺少原因，回傳 400 STOCK_REASON_INVALID")
    @Sql("/fixtures/product-stock-adjust/TC11_reason_missing.sql")
    void tc11_reasonMissing() throws Exception {
        assertRejected("{\"quantity\":10}", 400, "STOCK_REASON_INVALID");
        assertUnchanged();
    }

    @Test
    @DisplayName("TC12 原因只有空白，回傳 400 STOCK_REASON_INVALID")
    @Sql("/fixtures/product-stock-adjust/TC12_reason_blank.sql")
    void tc12_reasonBlank() throws Exception {
        assertRejected(body(10, "   "), 400, "STOCK_REASON_INVALID");
        assertUnchanged();
    }

    @Test
    @DisplayName("TC13 原因超過 100 字，回傳 400 STOCK_REASON_INVALID")
    @Sql("/fixtures/product-stock-adjust/TC13_reason_101_chars.sql")
    void tc13_reasonOver100Chars() throws Exception {
        assertRejected(body(10, "調".repeat(101)), 400, "STOCK_REASON_INVALID");
        assertUnchanged();
    }

    @Test
    @DisplayName("TC14 商品不存在，回傳 404 PRODUCT_NOT_FOUND")
    @Sql("/fixtures/product-stock-adjust/TC14_product_not_found.sql")
    void tc14_productNotFound() throws Exception {
        adjust(999, "alice", body(10, REASON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));

        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("TC15 扣超過現有庫存，回傳 422 PRODUCT_STOCK_INSUFFICIENT")
    @Sql("/fixtures/product-stock-adjust/TC15_deduct_over_stock.sql")
    void tc15_deductOverStock() throws Exception {
        assertRejected(body(-51, REASON), 422, "PRODUCT_STOCK_INSUFFICIENT");
        assertUnchanged();
    }

    @Test
    @DisplayName("TC16 加庫存超過上限，回傳 422 PRODUCT_STOCK_OVERFLOW")
    @Sql("/fixtures/product-stock-adjust/TC16_stock_overflow.sql")
    void tc16_stockOverflow() throws Exception {
        assertRejected(body(100, REASON), 422, "PRODUCT_STOCK_OVERFLOW");
        assertProduct(2147483600, "ON_SHELF", OLD_ON_SHELF_TIME);
        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("TC17 兩個人同時扣同一商品，不能超扣")
    @Sql("/fixtures/product-stock-adjust/TC17_concurrent_deduct.sql")
    void tc17_concurrentDeductNoOversell() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<MvcResult>> futures = new ArrayList<>();
        try {
            for (String operator : List.of("alice", "bob")) {
                Callable<MvcResult> call = () -> {
                    start.await();
                    return adjust(PRODUCT_ID, operator, body(-6, REASON)).andReturn();
                };
                futures.add(pool.submit(call));
            }
            start.countDown();

            List<Integer> statuses = new ArrayList<>();
            String winner = null;
            for (int i = 0; i < futures.size(); i++) {
                MvcResult result = futures.get(i).get();
                int httpStatus = result.getResponse().getStatus();
                statuses.add(httpStatus);
                String content = result.getResponse().getContentAsString();
                if (httpStatus == 200) {
                    winner = i == 0 ? "alice" : "bob";
                    assertThat(content).contains("\"code\":\"OK\"");
                } else {
                    assertThat(content).contains("\"code\":\"PRODUCT_STOCK_INSUFFICIENT\"");
                }
            }
            assertThat(statuses).containsExactlyInAnyOrder(200, 422);

            assertProduct(4, "ON_SHELF", OLD_ON_SHELF_TIME);
            Map<String, Object> log = onlyLog();
            assertLog(log, -6, 10, 4, "ON_SHELF", "ON_SHELF");
            assertThat(log.get("operator")).isEqualTo(winner);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("TC18 同時違反多條規則，回傳第一個")
    @Sql("/fixtures/product-stock-adjust/TC18_multiple_violations.sql")
    void tc18_multipleViolationsReturnsFirst() throws Exception {
        adjust(999, "alice", body(0, "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("STOCK_QUANTITY_INVALID"));

        assertThat(logCount()).isZero();
    }

    private ResultActions adjust(long id, String operator, String json) throws Exception {
        return mockMvc.perform(post(URL, id)
                .header("X-Operator", operator)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private void assertRejected(String json, int httpStatus, String code) throws Exception {
        adjust(PRODUCT_ID, "alice", json)
                .andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.code").value(code));
    }

    private static String body(int quantity, String reason) {
        return "{\"quantity\":" + quantity + ",\"reason\":\"" + reason + "\"}";
    }

    private void assertProduct(int stock, String status, LocalDateTime onShelfTime) {
        Map<String, Object> product = jdbcTemplate.queryForMap(
                "SELECT stock, status, on_shelf_time FROM product WHERE id = ?", PRODUCT_ID);
        assertThat(product.get("stock")).isEqualTo(stock);
        assertThat(product.get("status")).isEqualTo(status);
        assertThat(product.get("on_shelf_time")).isEqualTo(onShelfTime);
    }

    /** 失敗案例：商品與 fixture 相同（庫存 50、ON_SHELF、on_shelf_time 2026-01-01 10:00:00），沒有紀錄。 */
    private void assertUnchanged() {
        assertProduct(50, "ON_SHELF", OLD_ON_SHELF_TIME);
        assertThat(logCount()).isZero();
    }

    private int logCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product_stock_log", Integer.class);
    }

    private Map<String, Object> onlyLog() {
        assertThat(logCount()).isEqualTo(1);
        return jdbcTemplate.queryForMap("SELECT * FROM product_stock_log");
    }

    private void assertLog(Map<String, Object> log, int quantity, int stockBefore, int stockAfter,
                           String statusBefore, String statusAfter) {
        assertThat(((Number) log.get("product_id")).longValue()).isEqualTo(PRODUCT_ID);
        assertThat(log.get("quantity")).isEqualTo(quantity);
        assertThat(log.get("stock_before")).isEqualTo(stockBefore);
        assertThat(log.get("stock_after")).isEqualTo(stockAfter);
        assertThat(log.get("status_before")).isEqualTo(statusBefore);
        assertThat(log.get("status_after")).isEqualTo(statusAfter);
    }
}
