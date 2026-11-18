package com.example.shop.product;

import com.example.shop.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 前台商品展示 API（列表、詳情）。對應 docs/test-cases/storefront-product-browse.md。
 * 所有請求都不帶 X-Operator。
 */
class StorefrontProductBrowseApiTest extends ApiTestBase {

    private static final String LIST = "/api/storefront/products";
    private static final String DETAIL = "/api/storefront/products/";
    private static final String NOT_FOUND_MESSAGE = "找不到商品";

    // ---------- 列表 ----------

    @Test
    @DisplayName("TC01 列表只含上架中且分類啟用的商品")
    @Sql("/fixtures/storefront-product-browse/TC01_visible_only.sql")
    void tc01_visibleOnly() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[*].id", containsInAnyOrder(101, 102)))
                .andExpect(jsonPath("$.data.total").value(2));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC02 列表每筆只含指定欄位")
    @Sql("/fixtures/storefront-product-browse/TC02_fields.sql")
    void tc02_fields() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id").value(100))
                .andExpect(jsonPath("$.data.items[0].name").value("電鍋"))
                .andExpect(jsonPath("$.data.items[0].price").value(199.5))
                .andExpect(jsonPath("$.data.items[0].mainImageUrl").value("https://img.example.com/100.jpg"))
                .andExpect(jsonPath("$.data.items[0].categoryName").value("家電"))
                .andExpect(jsonPath("$.data.items[0].stockStatus").value("IN_STOCK"))
                .andExpect(jsonPath("$.data.items[0].remaining").value(nullValue()))
                .andExpect(jsonPath("$.data.items[0].sku").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].costPrice").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].stock").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].description").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].status").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].categoryId").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].onShelfTime").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].createTime").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].updateTime").doesNotExist());

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC03 庫存提示的邊界")
    @Sql("/fixtures/storefront-product-browse/TC03_stock_hint.sql")
    void tc03_stockHint() throws Exception {
        Snapshot before = snapshot();

        // 上架時間相同，依 id 新到舊：104（庫存 500）、103（10）、102（9）、101（1）
        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(4)))
                .andExpect(jsonPath("$.data.items[0].id").value(104))
                .andExpect(jsonPath("$.data.items[0].stockStatus").value("IN_STOCK"))
                .andExpect(jsonPath("$.data.items[0].remaining").value(nullValue()))
                .andExpect(jsonPath("$.data.items[1].id").value(103))
                .andExpect(jsonPath("$.data.items[1].stockStatus").value("IN_STOCK"))
                .andExpect(jsonPath("$.data.items[1].remaining").value(nullValue()))
                .andExpect(jsonPath("$.data.items[2].id").value(102))
                .andExpect(jsonPath("$.data.items[2].stockStatus").value("LOW_STOCK"))
                .andExpect(jsonPath("$.data.items[2].remaining").value(9))
                .andExpect(jsonPath("$.data.items[3].id").value(101))
                .andExpect(jsonPath("$.data.items[3].stockStatus").value("LOW_STOCK"))
                .andExpect(jsonPath("$.data.items[3].remaining").value(1))
                .andExpect(jsonPath("$.data.items[*].stock").doesNotExist());

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC04 預設分頁")
    @Sql("/fixtures/storefront-product-browse/TC04_default_paging.sql")
    void tc04_defaultPaging() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.total").value(25))
                .andExpect(jsonPath("$.data.items", hasSize(20)));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC05 指定頁碼與每頁筆數")
    @Sql("/fixtures/storefront-product-browse/TC05_page_size.sql")
    void tc05_pageAndSize() throws Exception {
        Snapshot before = snapshot();

        // id 新到舊 125…101，第 21～25 筆是 105、104、103、102、101
        mockMvc.perform(get(LIST).param("page", "3").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.page").value(3))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").value(25))
                .andExpect(jsonPath("$.data.items", hasSize(5)))
                .andExpect(jsonPath("$.data.items[0].id").value(105))
                .andExpect(jsonPath("$.data.items[4].id").value(101));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC06 頁碼超出最後一頁回空列表")
    @Sql("/fixtures/storefront-product-browse/TC06_page_beyond.sql")
    void tc06_pageBeyondLast() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("page", "9").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.page").value(9))
                .andExpect(jsonPath("$.data.total").value(25));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC07 每頁筆數可用到上限 50")
    @Sql("/fixtures/storefront-product-browse/TC07_size_max.sql")
    void tc07_sizeMax() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.size").value(50))
                .andExpect(jsonPath("$.data.items", hasSize(50)))
                .andExpect(jsonPath("$.data.total").value(55));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC08 每頁筆數超過上限")
    @Sql("/fixtures/storefront-product-browse/TC08_size_over_max.sql")
    void tc08_sizeOverMax() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC09 page 不是正整數")
    @Sql("/fixtures/storefront-product-browse/TC09_page_invalid.sql")
    void tc09_pageInvalid() throws Exception {
        Snapshot before = snapshot();

        for (String page : List.of("0", "-1", "abc")) {
            mockMvc.perform(get(LIST).param("page", page))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC10 size 不是正整數")
    @Sql("/fixtures/storefront-product-browse/TC10_size_invalid.sql")
    void tc10_sizeInvalid() throws Exception {
        Snapshot before = snapshot();

        for (String size : List.of("0", "-5", "abc")) {
            mockMvc.perform(get(LIST).param("size", size))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC11 預設依最新上架排序，同時間依 id 新到舊")
    @Sql("/fixtures/storefront-product-browse/TC11_sort_latest.sql")
    void tc11_sortLatest() throws Exception {
        Snapshot before = snapshot();

        // 不帶 sort 與 sort=latest 結果相同
        for (var request : List.of(get(LIST), get(LIST).param("sort", "latest"))) {
            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("OK"))
                    .andExpect(jsonPath("$.data.items", hasSize(4)))
                    .andExpect(jsonPath("$.data.items[0].id").value(140))
                    .andExpect(jsonPath("$.data.items[1].id").value(120))
                    .andExpect(jsonPath("$.data.items[2].id").value(130))
                    .andExpect(jsonPath("$.data.items[3].id").value(110));
        }

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC12 價格由低到高")
    @Sql("/fixtures/storefront-product-browse/TC12_price_asc.sql")
    void tc12_priceAsc() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(4)))
                .andExpect(jsonPath("$.data.items[0].id").value(140))
                .andExpect(jsonPath("$.data.items[1].id").value(120))
                .andExpect(jsonPath("$.data.items[2].id").value(130))
                .andExpect(jsonPath("$.data.items[3].id").value(110));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC13 價格由高到低")
    @Sql("/fixtures/storefront-product-browse/TC13_price_desc.sql")
    void tc13_priceDesc() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("sort", "price_desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(4)))
                .andExpect(jsonPath("$.data.items[0].id").value(110))
                .andExpect(jsonPath("$.data.items[1].id").value(130))
                .andExpect(jsonPath("$.data.items[2].id").value(140))
                .andExpect(jsonPath("$.data.items[3].id").value(120));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC14 sort 不認得")
    @Sql("/fixtures/storefront-product-browse/TC14_sort_unknown.sql")
    void tc14_sortUnknown() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("sort", "random"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC15 依分類篩選")
    @Sql("/fixtures/storefront-product-browse/TC15_filter_category.sql")
    void tc15_filterByCategory() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("categoryId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[*].id", containsInAnyOrder(101, 102)))
                .andExpect(jsonPath("$.data.items[*].categoryName", containsInAnyOrder("分類甲", "分類甲")))
                .andExpect(jsonPath("$.data.total").value(2));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC16 篩選的分類已停用回空列表")
    @Sql("/fixtures/storefront-product-browse/TC16_category_disabled.sql")
    void tc16_categoryDisabled() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("categoryId", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.total").value(0));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC17 篩選的分類不存在回空列表")
    @Sql("/fixtures/storefront-product-browse/TC17_category_missing.sql")
    void tc17_categoryMissing() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("categoryId", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.total").value(0));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC18 categoryId 不是正整數")
    @Sql("/fixtures/storefront-product-browse/TC18_category_invalid.sql")
    void tc18_categoryIdInvalid() throws Exception {
        Snapshot before = snapshot();

        for (String categoryId : List.of("0", "abc")) {
            mockMvc.perform(get(LIST).param("categoryId", categoryId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC19 名稱搜尋：包含、不分大小寫")
    @Sql("/fixtures/storefront-product-browse/TC19_keyword.sql")
    void tc19_keyword() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("keyword", "apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(3)))
                .andExpect(jsonPath("$.data.items[*].id", containsInAnyOrder(101, 102, 104)))
                .andExpect(jsonPath("$.data.total").value(3));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC20 空白關鍵字視為沒帶")
    @Sql("/fixtures/storefront-product-browse/TC20_keyword_blank.sql")
    void tc20_keywordBlank() throws Exception {
        Snapshot before = snapshot();

        for (String keyword : List.of("", "  ")) {
            mockMvc.perform(get(LIST).param("keyword", keyword))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("OK"))
                    .andExpect(jsonPath("$.data.items", hasSize(3)));
        }

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC21 名稱搜尋不會搜到不可見的商品")
    @Sql("/fixtures/storefront-product-browse/TC21_keyword_visible_only.sql")
    void tc21_keywordVisibleOnly() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST).param("keyword", "手機"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id").value(101))
                .andExpect(jsonPath("$.data.total").value(1));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC22 篩選、搜尋、排序、分頁可以組合")
    @Sql("/fixtures/storefront-product-browse/TC22_combined.sql")
    void tc22_combined() throws Exception {
        Snapshot before = snapshot();

        // 分類 100 且名稱含「杯」共 5 筆，價格低到高為 10、20、30、40、50；第 2 頁（每頁 2 筆）是 30、40
        mockMvc.perform(get(LIST)
                        .param("categoryId", "100")
                        .param("keyword", "杯")
                        .param("sort", "price_asc")
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].price").value(30.0))
                .andExpect(jsonPath("$.data.items[1].price").value(40.0));

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC23 沒有任何可見商品時回空列表")
    @Sql("/fixtures/storefront-product-browse/TC23_no_visible.sql")
    void tc23_noVisibleProducts() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.total").value(0));

        assertThat(snapshot()).isEqualTo(before);
    }

    // ---------- 詳情 ----------

    @Test
    @DisplayName("TC24 詳情：列表欄位加商品描述")
    @Sql("/fixtures/storefront-product-browse/TC24_detail.sql")
    void tc24_detail() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(DETAIL + "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.name").value("電鍋"))
                .andExpect(jsonPath("$.data.price").value(199.5))
                .andExpect(jsonPath("$.data.mainImageUrl").value("https://img.example.com/100.jpg"))
                .andExpect(jsonPath("$.data.categoryName").value("家電"))
                .andExpect(jsonPath("$.data.stockStatus").value("IN_STOCK"))
                .andExpect(jsonPath("$.data.remaining").value(nullValue()))
                .andExpect(jsonPath("$.data.description").value("好用的電鍋"))
                .andExpect(jsonPath("$.data.sku").doesNotExist())
                .andExpect(jsonPath("$.data.costPrice").doesNotExist())
                .andExpect(jsonPath("$.data.stock").doesNotExist())
                .andExpect(jsonPath("$.data.status").doesNotExist())
                .andExpect(jsonPath("$.data.categoryId").doesNotExist())
                .andExpect(jsonPath("$.data.onShelfTime").doesNotExist())
                .andExpect(jsonPath("$.data.createTime").doesNotExist())
                .andExpect(jsonPath("$.data.updateTime").doesNotExist());

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC25 詳情：庫存剩 1～9 顯示只剩多少")
    @Sql("/fixtures/storefront-product-browse/TC25_detail_stock_hint.sql")
    void tc25_detailStockHint() throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(DETAIL + "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.stockStatus").value("LOW_STOCK"))
                .andExpect(jsonPath("$.data.remaining").value(9))
                .andExpect(jsonPath("$.data.stock").doesNotExist());
        mockMvc.perform(get(DETAIL + "102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.stockStatus").value("IN_STOCK"))
                .andExpect(jsonPath("$.data.remaining").value(nullValue()))
                .andExpect(jsonPath("$.data.stock").doesNotExist());

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC26 詳情：草稿商品找不到")
    @Sql("/fixtures/storefront-product-browse/TC26_detail_draft.sql")
    void tc26_detailDraft() throws Exception {
        assertDetailNotFound("100");
    }

    @Test
    @DisplayName("TC27 詳情：已下架商品找不到")
    @Sql("/fixtures/storefront-product-browse/TC27_detail_off_shelf.sql")
    void tc27_detailOffShelf() throws Exception {
        assertDetailNotFound("100");
    }

    @Test
    @DisplayName("TC28 詳情：已售完商品找不到")
    @Sql("/fixtures/storefront-product-browse/TC28_detail_sold_out.sql")
    void tc28_detailSoldOut() throws Exception {
        assertDetailNotFound("100");
    }

    @Test
    @DisplayName("TC29 詳情：分類停用的上架商品找不到")
    @Sql("/fixtures/storefront-product-browse/TC29_detail_category_disabled.sql")
    void tc29_detailCategoryDisabled() throws Exception {
        assertDetailNotFound("200");
    }

    @Test
    @DisplayName("TC30 詳情：商品不存在")
    @Sql("/fixtures/storefront-product-browse/TC30_detail_not_exist.sql")
    void tc30_detailNotExist() throws Exception {
        assertDetailNotFound("99999");
    }

    @Test
    @DisplayName("TC31 詳情：id 不是數字也回找不到")
    @Sql("/fixtures/storefront-product-browse/TC31_detail_not_number.sql")
    void tc31_detailNotNumber() throws Exception {
        assertDetailNotFound("abc");
    }

    @Test
    @DisplayName("TC32 看不到的商品回應內容完全相同，不透露原因")
    @Sql("/fixtures/storefront-product-browse/TC32_detail_same_response.sql")
    void tc32_detailSameResponse() throws Exception {
        Snapshot before = snapshot();

        // DRAFT、OFF_SHELF、SOLD_OUT、停用分類下的 ON_SHELF、不存在
        List<String> bodies = new ArrayList<>();
        for (String id : List.of("101", "102", "103", "201", "99999")) {
            MvcResult result = mockMvc.perform(get(DETAIL + id))
                    .andExpect(status().isNotFound())
                    .andReturn();
            bodies.add(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        }

        assertThat(bodies).hasSize(5);
        assertThat(bodies).allSatisfy(body -> assertThat(body).isEqualTo(bodies.get(0)));
        assertThat(bodies.get(0)).contains("PRODUCT_NOT_FOUND").contains(NOT_FOUND_MESSAGE);
        assertThat(snapshot()).isEqualTo(before);
    }

    // ---------- 共用 ----------

    /** 詳情看不到：404、PRODUCT_NOT_FOUND、「找不到商品」、data 為 null，DB 不變。 */
    private void assertDetailNotFound(String id) throws Exception {
        Snapshot before = snapshot();

        mockMvc.perform(get(DETAIL + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE))
                .andExpect(jsonPath("$.data").value(nullValue()));

        assertThat(snapshot()).isEqualTo(before);
    }

    /** 商品與分類的每個欄位，用來確認 API 沒有改動 DB。 */
    private Snapshot snapshot() {
        return new Snapshot(
                jdbcTemplate.queryForList("SELECT * FROM product ORDER BY id"),
                jdbcTemplate.queryForList("SELECT * FROM category ORDER BY id"));
    }

    private record Snapshot(List<Map<String, Object>> products, List<Map<String, Object>> categories) {
    }
}
