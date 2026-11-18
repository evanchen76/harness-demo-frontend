package com.example.shop;

import com.example.shop.common.ApiResponse;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

/**
 * docs/guide/architecture.md「程式慣例」的可執行版本：違反就紅燈，跟著 ./dev.sh test、./dev.sh verify 一起跑。
 *
 * <p>每條規則的 because 寫「為什麼」與「怎麼修」，失敗訊息直接告訴 AI 下一步。
 * 在 review 發現 AI 重複犯的錯，能寫成規則的就加在這裡（見 docs/guide/improve-rules.md「把錯誤變成規則」）。
 */
@AnalyzeClasses(packages = "com.example.shop", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controller_不直接存取_mapper = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Mapper")
            .because("Controller 只處理 HTTP，規則與交易放在 Service。"
                    + "修法：把查詢與寫入搬到對應的 XxxService，Controller 改呼叫 Service");

    @ArchTest
    static final ArchRule controller_回傳_ApiResponse = methods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
            .and().arePublic()
            .should().haveRawReturnType(ApiResponse.class)
            .because("所有 API 回應都是 { code, message, data }，OpenAPI 規格也照這個格式寫。"
                    + "修法：成功回傳 ApiResponse.ok(data)；失敗拋 BizException，由 GlobalExceptionHandler 轉成 ApiResponse");

    @ArchTest
    static final ArchRule service_不依賴_web_層 = noClasses()
            .that().areAnnotatedWith(Service.class)
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
            .because("Service 放業務規則，不認識 HTTP。"
                    + "修法：HTTP 相關的東西（header、request、狀態碼）留在 Controller，以參數傳進 Service；錯誤用 BizException + ErrorCode 表達");

    @ArchTest
    static final ArchRule 不用_ResponseStatusException = noClasses()
            .should().dependOnClassesThat().areAssignableTo(ResponseStatusException.class)
            .because("錯誤回應要有 ErrorCode，才會出現在回應的 code 與 OpenAPI 規格裡。"
                    + "修法：在 ErrorCode 加一筆（含 HTTP 狀態），改拋 new BizException(ErrorCode.XXX)");

    @ArchTest
    static final ArchRule 時間用_LocalDateTime = noClasses()
            .should().dependOnClassesThat().haveFullyQualifiedName("java.util.Date")
            .orShould().dependOnClassesThat().haveFullyQualifiedName("java.sql.Timestamp")
            .because("時間一律用 LocalDateTime（時區 Asia/Taipei）。修法：欄位與參數改成 java.time.LocalDateTime");

    @ArchTest
    static final ArchRule 不留除錯輸出 = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
            .because("除錯輸出不能留在程式裡。修法：刪掉 System.out / System.err / printStackTrace，需要記錄就用 SLF4J logger");
}
