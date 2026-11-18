package com.example.shop;

import com.example.shop.support.OpenApiValidationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

/**
 * API 測試的共同基底：連 shop（真的 MySQL），每個測試前先清空所有表。
 *
 * <p>子類別在測試方法上加 {@code @Sql("/fixtures/<功能>/<案例>.sql")} 載入該案例的前置資料。
 * 因為 {@link SqlMergeMode.MergeMode#MERGE}，方法上的 @Sql 會接在 cleanup 之後執行，不會取代它。
 *
 * <p>每個請求與回應都會用 docs/api/openapi.yaml 驗證（{@link OpenApiValidationConfig}）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(OpenApiValidationConfig.class)
@ActiveProfiles("test")
@Sql(scripts = "/fixtures/common/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class ApiTestBase {

    @Autowired
    protected MockMvc mockMvc;

    /** 用來在 Then 階段直接查 DB，確認資料真的寫進去。 */
    @Autowired
    protected JdbcTemplate jdbcTemplate;
}
