# 程式結構與慣例

## 程式結構

依業務領域分 package（`com.example.shop.<領域>`），領域底下再按角色分檔，不是整個專案先按層分：

| 位置 | 放什麼 |
|------|--------|
| `src/main/java/.../<領域>/`（`product`、`category`） | 該領域的 `Controller`、`Service`、`Mapper`（MyBatis-Plus）、實體與列舉。新功能先找對應領域，沒有才開新 package |
| `src/main/java/.../common/` | 跨領域共用：`ApiResponse`、`BizException`、`ErrorCode`、`GlobalExceptionHandler`、`HealthController` |
| `src/test/java/.../<領域>/` | 每個 feature 一個 `<Feature>ApiTest`，繼承 `ApiTestBase`；`ArchitectureTest` 在最上層 |
| `src/test/java/.../support/` | 測試用設定，例如依 `openapi.yaml` 驗證請求與回應的 `OpenApiValidationConfig` |
| `src/test/resources/fixtures/<feature>/` | 每個案例的 fixture（id 100 以上） |
| `db/migration/`、`db/seed.sql` | 結構異動 .sql、給服務與打 API 用的範例資料（id 1～99）；`db/init/` 是環境建立用，由使用者維護 |
| `docs/api/openapi.yaml` | API 規格，測試的請求與回應都依它驗證 |
| `docs/test-cases/<feature>.md` | 每個 feature 的測案（Given / When / Then） |
| `docs/changes/<slug>/` | 每個需求的進度與決策 |
| `config/checkstyle.xml` | `./dev.sh check` 的 lint 規則 |

## 程式慣例

這幾條由 `ArchitectureTest`（ArchUnit）檢查，違反會紅燈，失敗訊息的 because 後面有修法：

- 回應一律用 `ApiResponse`：成功 `ApiResponse.ok(data)`；失敗拋 `BizException`，由 `GlobalExceptionHandler` 轉換（不用 `ResponseStatusException`）
- 分層：Controller 只處理 HTTP，不直接用 Mapper；規則與交易放 Service，Service 不依賴 web 層
- 時間用 `LocalDateTime`，時區 Asia/Taipei（不用 `java.util.Date`、`java.sql.Timestamp`）
- 不留 `System.out` / `System.err` / `printStackTrace`

還沒寫成檢查的：多表寫入的 Service 方法加 `@Transactional`。
