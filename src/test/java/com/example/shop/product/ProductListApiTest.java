package com.example.shop.product;

import com.example.shop.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 商品列表 API。對應 docs/test-cases/product-list.md。
 */
class ProductListApiTest extends ApiTestBase {

    private static final String URL = "/api/admin/products";

    @Test
    @DisplayName("TC01 依 id 由小到大列出全部商品")
    @Sql("/fixtures/product-list/TC01_ordered_by_id.sql")
    void tc01_orderedById() throws Exception {
        List<Map<String, Object>> before = products();

        // 不帶 X-Operator
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[1].id").value(200))
                .andExpect(jsonPath("$.data[2].id").value(300))
                .andExpect(jsonPath("$.data[0].sku").value("TEST-L-100"))
                .andExpect(jsonPath("$.data[0].name").value("商品一"))
                .andExpect(jsonPath("$.data[0].categoryId").value(100))
                .andExpect(jsonPath("$.data[0].price").value(100.5))
                .andExpect(jsonPath("$.data[0].stock").value(10))
                .andExpect(jsonPath("$.data[0].status").value("ON_SHELF"))
                .andExpect(jsonPath("$.data[0].mainImageUrl").value("https://img.example.com/100.jpg"))
                .andExpect(jsonPath("$.data[0].onShelfTime").value("2026-01-01T10:00:00"))
                .andExpect(jsonPath("$.data[0].updateTime").value("2026-01-02T11:00:00"))
                .andExpect(jsonPath("$.data[1].sku").value("TEST-L-200"))
                .andExpect(jsonPath("$.data[1].price").value(200.0))
                .andExpect(jsonPath("$.data[2].sku").value("TEST-L-300"))
                .andExpect(jsonPath("$.data[2].stock").value(30))
                .andExpect(jsonPath("$.data[2].updateTime").value("2026-03-04T11:00:00"));

        assertThat(products()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC02 列表不含描述、成本價、建立時間")
    @Sql("/fixtures/product-list/TC02_excluded_fields.sql")
    void tc02_excludedFields() throws Exception {
        List<Map<String, Object>> before = products();

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].sku").value("TEST-L-001"))
                .andExpect(jsonPath("$.data[0].description").doesNotExist())
                .andExpect(jsonPath("$.data[0].costPrice").doesNotExist())
                .andExpect(jsonPath("$.data[0].createTime").doesNotExist());

        assertThat(products()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC03 四種狀態都以英文代碼列出")
    @Sql("/fixtures/product-list/TC03_all_statuses.sql")
    void tc03_allStatuses() throws Exception {
        List<Map<String, Object>> before = products();

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[*].status",
                        containsInAnyOrder("DRAFT", "ON_SHELF", "OFF_SHELF", "SOLD_OUT")));

        assertThat(products()).isEqualTo(before);
    }

    @Test
    @DisplayName("TC04 沒有商品時回空陣列")
    @Sql("/fixtures/product-list/TC04_no_products.sql")
    void tc04_noProducts() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data", hasSize(0)));

        assertThat(products()).isEmpty();
    }

    @Test
    @DisplayName("TC05 沒填的欄位回 null")
    @Sql("/fixtures/product-list/TC05_null_fields.sql")
    void tc05_nullFields() throws Exception {
        List<Map<String, Object>> before = products();

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[0].sku").value("TEST-L-001"))
                .andExpect(jsonPath("$.data[0].categoryId").value(100))
                .andExpect(jsonPath("$.data[0].price").value(99.0))
                .andExpect(jsonPath("$.data[0].stock").value(3))
                .andExpect(jsonPath("$.data[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.data[0].name").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].mainImageUrl").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].onShelfTime").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].updateTime").isNotEmpty());

        assertThat(products()).isEqualTo(before);
    }

    /** 全部商品的每個欄位（含 update_time），用來確認列表沒有改動 DB。 */
    private List<Map<String, Object>> products() {
        return jdbcTemplate.queryForList("SELECT * FROM product ORDER BY id");
    }
}
