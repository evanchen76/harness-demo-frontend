package com.example.shop.support;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;

/**
 * 每一個 MockMvc 請求與回應都要符合 docs/api/openapi.yaml。
 *
 * <p>路徑沒寫進規格、回應欄位型別或 HTTP 狀態不在規格裡，測試都會失敗。
 * 所以新增 API 時要先改規格（規格階段），再寫測試與實作。
 *
 * <p>例外：請求少了必填 header 不算失敗（{@code validation.request.parameter.header.missing}），
 * 因為「缺少 X-Operator 回 400」本身就是要測的錯誤案例；這類案例的回應仍然要符合規格。
 * 來源：product-on-shelf TC04，使用者決定（2026-11-12）。
 */
@TestConfiguration
public class OpenApiValidationConfig {

    public static final String SPEC = "docs/api/openapi.yaml";

    @Bean
    MockMvcBuilderCustomizer openApiValidation() {
        OpenApiInteractionValidator validator = OpenApiInteractionValidator.createFor(SPEC)
                .withLevelResolver(LevelResolver.create()
                        .withLevel("validation.request.parameter.header.missing", ValidationReport.Level.IGNORE)
                        .build())
                .build();
        return builder -> builder.alwaysExpect(openApi().isValid(validator));
    }
}
