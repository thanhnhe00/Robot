# PROMPT PHASE 3 — Android app (Robot UI + Action Executor)

> Dán toàn bộ file này vào Antigravity (mở workspace ở thư mục gốc repo `Robot`) hoặc lưu thành `docs/prompts/phase3-android.md`.
> Cách dùng: gửi "**Bắt đầu 3.0**". Agent chỉ làm **một mục con (3.0 → 3.6) mỗi lượt**, báo kết quả rồi dừng. Bạn xem, tự commit, rồi gửi "**Làm 3.x tiếp theo**".

---

## 1. Vai trò và bối cảnh

Bạn là kỹ sư Android/Kotlin đang làm **Phase 3** của dự án ROBOTV1 (robot AI local-first, tiếng Việt):

```
Android brain (Samsung Galaxy Z Flip5)  ⇄  Backend FastAPI (laptop)  ⇄  Model AI
         └─ validator + executor chạy TRÊN ĐIỆN THOẠI (ADR-0001), backend validate lại lớp thứ hai
```

- Phase 1 (AI Gateway) và Phase 2 (Action registry, safety pipeline, golden set, eval v0) đã xong và nằm trong `backend/`, `ai/`.
- **Mục tiêu Phase 3:** nhập text trên app → robot phản hồi (mặt robot + state machine) → thực thi action qua API Android chính thức; action ngoài whitelist bị **từ chối ngay trên app**; có `AIProvider` phía Android để Phase 4 gắn model local.
- **Không thuộc Phase 3:** giọng nói/wake word/TTS/STT (Phase 5), LLM chạy trên máy (Phase 4), router Local→Laptop→Cloud (Phase 6), memory dài hạn, launcher/kiosk, ESP32, camera, giao diện cover screen.

Chủ dự án là sinh viên CNTT, dùng **Windows**, đã cài **Android Studio**, đã bật **USB debugging**. Dự án dùng làm portfolio xin việc: ưu tiên code sạch, có test, có tài liệu, **không phô trương**.

## 2. Tham số do chủ dự án điền (đổi nếu muốn, không hỏi lại nếu để mặc định)

| Tham số | Mặc định | Ghi chú |
|---|---|---|
| applicationId / package | `com.thanhnhe00.robot` | đổi trước 3.1, sau đó rất khó đổi |
| Thiết bị đích | Samsung Galaxy Z Flip5, **SM-F731B, Android 15 (API 35), One UI 7** | do chủ dự án cung cấp |
| compileSdk / targetSdk | 35 | khớp Android 15 |
| minSdk | 33 | `ASSUMPTION`: Z Flip5 xuất xưởng Android 13; chỉ một thiết bị đích. Ghi trong ADR-0006 |
| Backend khi dev | `http://127.0.0.1:8000` qua `adb reverse tcp:8000 tcp:8000` | không mở backend ra LAN |
| Màn hình hỗ trợ | Màn hình chính (main display) | cover screen = backlog |

## 3. Quy trình bắt buộc cho MỖI lượt (kết hợp AGENTS.md + rules + skills + workflow)

1. **Nạp ngữ cảnh:** đọc `AGENTS.md`, `.agents/rules/*`, `docs/HANDOFF.md`, `docs/issues/03-android-app.md`, `docs/decisions/0001-*.md`, `docs/research/C-android-hardware.md`, và phần liên quan trong `docs/ARCHITECTURE.md`. Chạy `git status`, giữ nguyên thay đổi chưa commit của người dùng.
2. **Đối chiếu repo ↔ tài liệu:** code thực tế là bằng chứng. Nếu `HANDOFF.md` hoặc docs lệch code (ví dụ số test, trạng thái CI), **nêu rõ chỗ lệch**, không âm thầm tin tài liệu.
3. **Chọn skill:**
   - Luôn dùng `/robot-task` (điều phối một việc theo phase/phạm vi) và `/android-development` (mọi việc Android).
   - Dùng `/research-and-adr` khi viết ADR hoặc cần tra tài liệu chính thức (API/SDK/phiên bản).
   - `/ai-model-evaluation`, `/embedded-robot-safety`, `/backend-development`: chỉ dùng khi mục con **thực sự chạm** vào (ví dụ 3.2 sửa test backend).
   - Workflow `/start-robot-issue` tương đương các bước này; theo README `.agents` nó chỉ được hỗ trợ đến khoảng 1/11/2026, nên dùng `/robot-task` làm chính.
4. **Một mục con mỗi lượt.** Mở đầu bằng 3–5 dòng: mục tiêu, phạm vi, file dự kiến tác động, cách xác nhận. Rồi làm luôn trong phạm vi đó (không hỏi xác nhận từng dòng).
5. **Kiểm tra thật, báo thật:** chỉ ghi "đã chạy" khi thực sự chạy. Thiếu SDK/thiết bị/quyền thì ghi "chưa chạy vì …". Lệnh chuẩn: `./gradlew testDebugUnitTest lintDebug assembleDebug` (chạy trong `android/`); backend: `ruff check backend` và `python -m pytest backend/tests -q`.
6. **Không tự commit/push/tag/release.** Gợi ý commit message theo tiền tố repo (`feat:`, `test:`, `docs:`, `chore:`…).
7. **Kết thúc bằng báo cáo** theo mẫu ở mục 9, rồi **DỪNG**.

## 4. Quy tắc bất biến (rút từ rules của repo)

**An toàn**
- Model/backend chỉ **đề xuất**. Mọi action đi qua: `ProposedAction` (chưa tin cậy) → `ActionValidator` → `ValidatedAction` → `ActionExecutor`. **Executor chỉ nhận `ValidatedAction`** (kiểu khác nhau để compiler chặn lỗi).
- Parse/validate lỗi hoặc không rõ → **không thực thi**. Action lạ, tham số sai, action phần cứng chưa nối (`move`, `stop`) → từ chối.
- Không nới whitelist, quyền, timeout hay giới hạn để test/demo chạy qua.
- Không dùng LLM cho dữ liệu OS có API sẵn: giờ và pin lấy từ hệ thống, **không** dùng câu trả lời của model.
- Backend không phải lớp bảo vệ duy nhất: app có kiểm tra độc lập.
- Nếu action bị từ chối mà `response` của model vẫn khẳng định đã làm (ví dụ "Mình đã đặt báo thức") thì **không hiển thị câu đó**; hiển thị câu an toàn cố định "Xin lỗi, mình chưa thực hiện được yêu cầu đó." và ghi log mã lý do.

**Riêng tư**
- Dữ liệu người dùng là `LOCAL ONLY`. **Không log nội dung hội thoại**, chỉ log độ dài, mã lý do, tên action, latency.
- Không commit `local.properties`, `.env`, keystore, API key, `build/`, `.gradle/`.

**Phạm vi và chất lượng**
- Thay đổi nhỏ nhất đáp ứng mục con. Không thêm framework/abstraction ngoài phạm vi: **không** Hilt/Dagger, Room, Retrofit, Navigation, Firebase. Dùng DI thủ công.
- Tên biến/hàm/class bằng tiếng Anh; chuỗi giao diện và tài liệu bằng tiếng Việt (chuỗi UI để trong `strings.xml`).
- Không tự sửa lỗi lint/ngoài phạm vi. Không tuyên bố "chạy tốt trên Z Flip5" nếu chưa test trên máy: nhãn `ASSUMPTION`, `CHƯA BIẾT`, `CẦN KIỂM TRA`.
- Phiên bản thư viện/SDK: tra tài liệu chính thức hiện hành, **không** gõ phiên bản theo trí nhớ.

## 5. Quyết định đã chốt (ghi vào ADR-0006 ở mục 3.0)

1. **Kotlin + Jetpack Compose** cho app Android (cần foreground service mic, JNI/NDK cho model local, USB/BLE; Flutter/React Native thêm lớp cầu nối).
2. **Đồng bộ luật Kotlin ↔ Python bằng bộ test vector JSON dùng chung** (`ai/schemas/action_vectors.json`) + contract test đối chiếu `action_schema.json`; **chưa** dựng công cụ sinh code. Điều kiện **xem lại**: khi có consumer thứ ba (firmware ESP32 ở Phase 9, dashboard ở Phase 12).
3. **Dev kết nối backend bằng `adb reverse`** (backend giữ bind `127.0.0.1`). Mở LAN chỉ khi bắt buộc, và khi đó phải đặt `API_KEY`.
4. minSdk/targetSdk theo mục 2.

## 6. Kiến trúc Android mục tiêu

```
android/app/src/main/java/com/thanhnhe00/robot/
  domain/
    action/    ProposedAction, ValidatedAction, ActionSpec, ActionValidator, ValidationResult
    exec/      ActionExecutor, ExecutionResult, ports: TimeProvider, BatteryReader,
               AlarmScheduler, AppLauncher, VolumeController
    state/     RobotState, RobotEvent, reduce()
  data/
    provider/  AIProvider, ModelReply, ProviderError, BackendProvider, MockProvider
  platform/    triển khai ports bằng API Android (BatteryManager, AlarmClock, AudioManager…)
  ui/          RobotApp, RobotFace, RobotViewModel, ChatInput, theme
android/app/src/debug/…   DebugScriptedProvider (chỉ bản debug)
android/app/src/test/…    unit test JVM (validator, vectors, state, executor, providers)
```

Nguyên tắc: `domain/` là Kotlin thuần (không import `android.*`) để test bằng JVM không cần máy.

## 7. Hợp đồng validator (hành vi MỤC TIÊU)

Nguồn sự thật: `ai/schemas/action_schema.json` + `backend/app/action_registry.py` + `backend/app/actions.py` **bản hiện tại trong repo**. Bảng dưới là hành vi mục tiêu; **nếu backend hiện tại khác bảng, DỪNG và báo chênh lệch kèm đề xuất**, không tự sửa backend và không tự chọn bên đúng.

| Action | Tham số hợp lệ | Timeout | Quyền Android | Trạng thái |
|---|---|---|---|---|
| `get_time` | không (tham số thừa bị bỏ, `params: null` ⇒ `{}`) | 1000ms | không | READY |
| `get_battery` | không | 1000ms | không | READY |
| `set_alarm` | `time` chuỗi khớp `^([01]\d\|2[0-3]):[0-5]\d$`; `label` tùy chọn, trim, cắt tối đa 100 **code point**, rỗng thì bỏ | 3000ms | `SET_ALARM` | READY |
| `open_app` | `package` thuộc whitelist **khớp chính xác** (sau trim, phân biệt hoa/thường): `com.google.android.youtube`, `com.android.chrome`, `com.android.settings`, `com.spotify.music` | 5000ms | launch | READY |
| `set_volume` | `level` là **số nguyên JSON** 0..100; từ chối bool, chuỗi, số thực, null, số vượt phạm vi, `1e999` | 2000ms | `MODIFY_AUDIO_SETTINGS` | READY |
| `move`, `stop` | (schema đăng ký, chưa nối ESP32) | — | motor | **STUB ⇒ luôn từ chối** |

Chung: `type` phân biệt hoa/thường, được trim; `action` không phải object ⇒ từ chối; `params` không phải object (và không phải null) ⇒ từ chối; mọi action ngoài registry ⇒ từ chối. Validator **không bao giờ ném exception** ra ngoài: mọi input xấu trả `Rejected(reason)`.

Mã lý do (enum, dùng để log): `UNKNOWN_ACTION`, `HARDWARE_NOT_READY`, `INVALID_PARAMS`, `MALFORMED`.

---

## 8. Các mục con

### 3.0 — ADR-0006 (chỉ tài liệu)  · skill: `/research-and-adr`
**Làm:**
- Tạo `docs/decisions/0006-android-kotlin-compose-and-contract-sync.md` theo mẫu ADR (Date `2026-10-01`, Status `Accepted`): Decision (mục 5), Why, Alternatives (Flutter, React Native, sinh code từ schema ngay, hai bộ luật viết tay không test chung), Trade-offs, Chosen, **Điều kiện xem lại**.
- Thêm dòng 0006 vào bảng trong `docs/decisions/README.md`.
- Sửa dòng "Android: Kotlin hay Flutter" trong `docs/ARCHITECTURE.md` thành "Kotlin + Compose (ADR-0006)". **Không** sửa ADR-0005 (vẫn `Proposed`).
- Tra tài liệu chính thức cho minSdk/targetSdk và hành vi Android 15 (edge-to-edge khi targetSdk 35; foreground service mic); ghi nguồn + ngày truy cập trong ADR.

**Xong khi:** ADR có đủ mục, README cập nhật, không đụng code. **DỪNG.**

### 3.1 — Khởi tạo project Android + CI  · skill: `/android-development`
**Làm:**
- Nếu `android/` chỉ có `.gitkeep`: **hướng dẫn người dùng** tạo project bằng Android Studio (New Project → Empty Activity (Compose), Kotlin DSL, package theo mục 2, minSdk 33), lưu vào `android/`. **Không** tự viết tay toàn bộ Gradle. Nếu project đã có, kiểm tra thay vì tạo lại.
- Thêm dependency tối thiểu (tra phiên bản chính thức): `kotlinx-serialization-json`, `kotlinx-coroutines`, `okhttp`, `lifecycle-viewmodel-compose`; test: JUnit, `okhttp mockwebserver`, `kotlinx-coroutines-test`. Dùng version catalog của template.
- Bổ sung `.gitignore` (Android: `local.properties`, `build/`, `.gradle/`, `*.keystore`, `.idea/` cá nhân…).
- Màn hình tạm: hiện chữ "Robot" + `BuildConfig.VERSION_NAME`. **Chưa** khai báo quyền nào (thêm quyền khi có tính năng cần nó).
- Tạo `.github/workflows/android.yml`: `runs-on: ubuntu-24.04`, `actions/checkout`, `actions/setup-java` (Temurin, bản JDK theo yêu cầu AGP hiện hành), chạy `./gradlew testDebugUnitTest lintDebug assembleDebug` với `working-directory: android`; trigger khi đổi `android/**` hoặc `ai/schemas/**`. Ghim phiên bản action; `permissions: contents: read`.
- Tạo `android/README.md`: yêu cầu, cách chạy, lệnh `adb reverse tcp:8000 tcp:8000`, cách tạo `local.properties` (URL, API key — không commit).

**Xong khi:** `./gradlew assembleDebug` + `testDebugUnitTest` qua (ghi rõ nếu chưa chạy được); **người dùng** xác nhận app mở được trên Z Flip5. Không tuyên bố CI xanh trước khi thấy kết quả trên GitHub. **DỪNG.**

### 3.2 — Validator Kotlin + test vector chung  · skill: `/android-development` (+ `/backend-development` cho test Python)
**Làm:**
- `domain/action/`: `ProposedAction(type: String, params: JsonObject?)`, `ValidatedAction` (sealed: `GetTime`, `GetBattery`, `SetAlarm(hour, minute, label?)`, `OpenApp(packageName)`, `SetVolume(percent)`), `ActionSpec` (tên, timeoutMs, quyền, hardware status), `ValidationResult` (`Valid`/`Rejected(reason)`), `ActionValidator.validate(ProposedAction?)` theo mục 7.
- Tạo `ai/schemas/action_vectors.json` (xem mục 10). Mỗi ca: `id`, `description`, `action` (JSON thô), `expected` (`null` hoặc `{type, params}` sau làm sạch).
- Backend: thêm `backend/tests/test_action_vectors.py` chạy mọi ca qua `validate_action()` hiện có. **Chạy từng ca với backend hiện tại trước**; ca nào khác `expected` ⇒ liệt kê trong báo cáo và dừng chờ quyết định (không sửa backend khi chưa được duyệt).
- Kotlin: `ActionValidatorVectorsTest` đọc cùng file (truyền đường dẫn qua `systemProperty("repo.root", …)` trong `build.gradle.kts`).
- **Contract test:** đối chiếu danh sách action, whitelist package, timeout, giới hạn trong Kotlin với `ai/schemas/action_schema.json` — lệch ⇒ test fail.
- Lưu ý parser: Python `json.loads` chấp nhận `Infinity`/`NaN`, Kotlin thì không. Vector dùng JSON chuẩn (ví dụ `1e999`); ghi chú mọi khác biệt parser tìm thấy.

**Xong khi:** test Kotlin + test Python cùng đọc một file vector và qua; validator không ném exception với bất kỳ ca nào. **DỪNG.**

### 3.3 — Executor + quyền Android  · skill: `/android-development`
**Làm:**
- `domain/exec/`: các **port** (`TimeProvider`, `BatteryReader`, `AlarmScheduler`, `AppLauncher`, `VolumeController`) và `ActionExecutor.execute(ValidatedAction): ExecutionResult` (`Success(userMessage)` | `Failed(userMessage, category)`). Mỗi action chạy trong `withTimeout(spec.timeoutMs)`; timeout ⇒ `Failed`.
- `platform/`: triển khai bằng API chính thức:
  - `get_time`: giờ hệ thống, định dạng `vi-VN`, **không** hỏi model.
  - `get_battery`: `BatteryManager`.
  - `set_alarm`: `AlarmClock.ACTION_SET_ALARM` (hour, minutes, message). Mặc định **hiện UI đồng hồ** (không `EXTRA_SKIP_UI`) để người dùng thấy/hủy được. `CẦN KIỂM TRA` trên One UI 7.
  - `open_app`: `getLaunchIntentForPackage` chỉ với package đã validate; app chưa cài ⇒ `Failed` với câu thân thiện.
  - `set_volume`: `AudioManager`, stream `STREAM_MUSIC` (`ASSUMPTION`), quy đổi phần trăm → index theo `getStreamMaxVolume`; bắt `SecurityException`.
- `AndroidManifest.xml`: chỉ thêm `SET_ALARM` (và `INTERNET` ở 3.4); thêm `<queries>` liệt kê đúng 4 package whitelist để `open_app` hoạt động với package visibility (Android 11+). Không thêm quyền thừa.
- Test JVM với fake ports: thành công, lỗi, timeout, ngoại lệ từng action; `move`/`stop` không thể tới executor (kiểu dữ liệu không cho phép).
- (Tùy chọn, nhỏ) màn hình debug 5 nút gọi executor trực tiếp, đặt trong `src/debug/`, để thử trên máy trước khi có UI chính.

**Xong khi:** test qua; người dùng thử được 5 action trên Z Flip5 (ghi kết quả thật vào báo cáo, ca nào chưa thử ghi "chưa kiểm tra"). **DỪNG.**

### 3.4 — AIProvider phía Android  · skill: `/android-development`
**Làm:**
- `data/provider/`: `interface AIProvider { suspend fun generate(request: ChatRequest): Result<ModelReply> }`; `ChatRequest(sessionId, text)`; `ModelReply(response: String, action: ProposedAction?)` — action vẫn **chưa tin cậy**; `ProviderError` (sealed): `Unauthorized` (401), `ProviderUnavailable` (502/503), `Network`, `Timeout`, `InvalidResponse`.
- `BackendProvider` (OkHttp + kotlinx-serialization): `POST {baseUrl}/chat`, header `X-API-Key` nếu có cấu hình; `ignoreUnknownKeys`; timeout ≥ `REQUEST_TIMEOUT` của backend. Cấu hình lấy từ `BuildConfig` sinh ra từ `local.properties` (**không commit**). `network_security_config`: chỉ cho phép cleartext tới `127.0.0.1`/`localhost`, **không** bật cleartext toàn cục.
- `MockProvider` offline, xác định (deterministic), tiếng Việt: giờ, pin, "báo thức HH:MM", mở YouTube/Chrome/Cài đặt, "âm lượng N", chào hỏi, còn lại ⇒ câu mặc định + `action = null`.
- `DebugScriptedProvider` (chỉ `src/debug/`): trả action cố ý sai để chứng minh từ chối trên máy: type lạ, `open_app` với `com.evil.app`, `set_volume` 999, `move`, `set_alarm` giờ `25:00`, và một ca `response` nói dối ("đã đặt báo thức") kèm action bị từ chối.
- Chọn provider bằng một công tắc đơn giản (Backend / Mock; thêm Scripted ở debug). **Không** làm router/fallback tự động (Phase 6).
- Test: `MockWebServer` cho 200, 401, 502, 503, JSON hỏng, trường thiếu, timeout; test `MockProvider`.

**Xong khi:** test qua; chạy được với backend `LLM_PROVIDER=mock` qua `adb reverse`, và với Mock offline (bật chế độ máy bay). **DỪNG.**

### 3.5 — State machine + mặt robot + luồng chính  · skill: `/android-development`
**Làm:**
- `domain/state/`: `RobotState` = `IDLE, WAKE, LISTENING, PROCESSING, SPEAKING, ERROR`; `RobotEvent`; hàm thuần `reduce(state, event)`. Phase 3 chỉ dùng `IDLE → PROCESSING → SPEAKING → IDLE` và `ERROR`; `WAKE`/`LISTENING` **định nghĩa nhưng không thể tới được** (có test giữ điều đó). Event không hợp lệ ở state hiện tại ⇒ bỏ qua, không crash.
- `RobotViewModel`: nhận text → `PROCESSING` → `provider.generate` → `validator.validate` → (hợp lệ) `executor.execute` / (từ chối) câu an toàn cố định + log mã lý do → `SPEAKING` → `IDLE`. Chặn gửi trùng khi đang `PROCESSING`; lỗi provider ⇒ `ERROR` với thông điệp thân thiện + nút thử lại. `SPEAKING` ở Phase 3 = hiển thị bong bóng thoại + hoạt hình miệng theo độ dài chữ (**chưa có TTS**, ghi rõ trong code là placeholder cho Phase 5).
- UI Compose: `RobotFace` vẽ bằng `Canvas` (mắt chớp ở IDLE, mắt chuyển động ở PROCESSING, miệng ở SPEAKING, biểu cảm lỗi ở ERROR); ô nhập text + nút gửi; hiển thị kết quả thực thi (ví dụ "Bây giờ là 14:05"). Xử lý insets (edge-to-edge của Android 15), `contentDescription` cho trạng thái, chuỗi trong `strings.xml`. Bố cục Flex mode: `CẦN KIỂM TRA`, không làm UI riêng cho cover screen.
- Test: bảng chuyển trạng thái đầy đủ (kể cả chuyển trạng thái sai); test ViewModel với provider/executor giả, **bắt buộc có ca "action không hợp lệ thì executor không bao giờ được gọi"** và ca "response nói dối bị thay bằng câu an toàn".

**Xong khi:** test qua; nhập text trên máy thật ra phản hồi và thực thi được. **DỪNG.**

### 3.6 — Nghiệm thu trên Z Flip5 + cập nhật tài liệu  · skills: `/robot-task`, `/android-development`
**Làm:**
- Tạo `docs/benchmarks/phase3-android-manual-test.md`: ghi thông tin máy (`Build.MODEL`, `SDK_INT`, phiên bản One UI) rồi bảng ca test với cột *Dự kiến / Thực tế / Đạt / Ghi chú* — **để trống cột Thực tế/Đạt cho người dùng điền**, không tự điền.
- Các ca tối thiểu: giờ; pin; báo thức 07:30 (Đồng hồ mở với 07:30?); mở YouTube / Chrome / Cài đặt / Spotify (cài và chưa cài); âm lượng 0 / 30 / 100; hội thoại thường; **6 ca từ chối** bằng `DebugScriptedProvider`; chế độ máy bay + Mock (hoạt động); chế độ máy bay + Backend (vào `ERROR`, hiển thị thân thiện, không crash); backend tắt; API key sai (401); xoay/gập máy (Flex mode).
- Cập nhật `docs/HANDOFF.md`, `docs/issues/03-android-app.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`: **chỉ tick/ghi những gì đã kiểm chứng thật**; mục chưa test ghi `CHƯA KIỂM TRA`.
- Rà soát lần cuối: `./gradlew testDebugUnitTest lintDebug assembleDebug`, `ruff check backend`, `pytest backend/tests`.

**Xong khi:** đạt Definition of Done ở mục 11. Chỉ đề xuất tag `v0.3.0-phase3` nếu người dùng yêu cầu. **DỪNG.**

---

## 9. Mẫu báo cáo cuối mỗi lượt (bằng tiếng Việt đơn giản)

1. **Đã làm:** danh sách file tạo/sửa (một dòng mỗi file).
2. **Kiểm tra đã chạy:** lệnh + kết quả thật. Nếu chưa chạy: ghi "chưa chạy vì …".
3. **Chênh lệch repo ↔ docs / backend ↔ bảng mục 7** (nếu có).
4. **CHƯA BIẾT / ASSUMPTION / CẦN KIỂM TRA** còn lại.
5. **Việc người dùng cần làm** (lệnh hoặc thao tác cụ thể trên máy) + commit message gợi ý.
6. **Một bước kế tiếp** duy nhất, rồi dừng.

## 10. Bộ test vector tối thiểu (`ai/schemas/action_vectors.json`)

Viết thành JSON hợp lệ. `expected` = `null` nghĩa là bị từ chối. Cột *Cần quyết định* = hành vi backend hiện tại có thể khác bảng; chạy thử, báo, **không tự chọn**.

| Nhóm | Ca (action thô) | Kỳ vọng |
|---|---|---|
| get_time | `{type:get_time,params:{}}` / `params:null` / `params:{x:1}` | `{get_time,{}}` |
| get_time | `{type:" get_time "}` (khoảng trắng) | `{get_time,{}}` |
| get_time | `{type:"GET_TIME"}` | `null` |
| get_battery | `{type:get_battery}` | `{get_battery,{}}` |
| set_alarm | `time:"07:30"` | `{time:"07:30"}` |
| set_alarm | `time:"07:30", label:" Đi học "` | `{time:"07:30",label:"Đi học"}` |
| set_alarm | `time:"07:30", label:"   "` | `{time:"07:30"}` (label bị bỏ) |
| set_alarm | label dài 150 ký tự | label cắt còn 100 code point |
| set_alarm | `"24:00"`, `"7:30"`, `"07:60"`, `"0730"`, thiếu `time`, `time:730` (số) | `null` |
| open_app | 4 package whitelist (mỗi gói một ca) | `{package:…}` |
| open_app | `" com.android.chrome "` | `{package:"com.android.chrome"}` |
| open_app | `"COM.ANDROID.CHROME"`, `"com.evil.app"`, thiếu package, package là số | `null` |
| set_volume | `level` = 0, 50, 100 | `{level:…}` |
| set_volume | `101`, `-1`, `"50"`, `50.5`, `true`, `null`, `1e999`, `99999999999999999999` | `null` |
| set_volume | `50.0` (JSON số thực) | **Cần quyết định** (mục tiêu: `null`) |
| set_alarm | `label` không phải chuỗi (số 5) | **Cần quyết định** (mục tiêu: bỏ label hoặc `null`) |
| chung | `type:"call_phone"`, `type:""`, thiếu `type` | `null` |
| chung | `action` là chuỗi `"get_time"`, là mảng `[]`, `params` là mảng, `params` là chuỗi | `null` |
| STUB | `{type:move,params:{direction:forward}}`, `{type:stop}` | `null` |

## 11. Definition of Done của Phase 3 (từ issue 03)

- [ ] Nhập text → robot phản hồi và thực thi action trên Samsung Galaxy Z Flip5 (**có kết quả test thật ghi trong docs**).
- [ ] Action ngoài whitelist bị từ chối trên app (có test JVM **và** bằng chứng trên máy qua `DebugScriptedProvider`).
- [ ] Có test cho validator; bộ vector chung chạy qua ở cả Python và Kotlin; contract test với `action_schema.json` qua.
- [ ] `AIProvider` phía Android có `BackendProvider` + `MockProvider`; chạy được offline với Mock.
- [ ] CI Android có job chạy trên GitHub (chỉ ghi xanh khi thấy xanh).
- [ ] ADR-0006 `Accepted`; `HANDOFF.md`, issue 03, ROADMAP, ARCHITECTURE cập nhật đúng sự thật.
- [ ] Không có secret/`local.properties`/model weights trong Git; log không chứa nội dung hội thoại.

## 12. Danh sách CHƯA BIẾT / CẦN KIỂM TRA (giữ nhãn cho tới khi có bằng chứng)

- `ASSUMPTION`: minSdk 33; stream `STREAM_MUSIC` cho `set_volume`.
- `CẦN KIỂM TRA`: hành vi `AlarmClock.ACTION_SET_ALARM` trên One UI 7; package visibility khi `open_app` trên Android 15; edge-to-edge/insets với targetSdk 35; bố cục Flex mode và gập/mở máy; khác biệt parser JSON Python ↔ Kotlin cho số cực lớn.
- `CHƯA BIẾT`: hiệu năng, pin, nhiệt khi chạy app lâu (đo ở Phase 4/13, không kết luận ở Phase 3).
- Backlog (không làm ở Phase 3): UI cover screen, launcher/kiosk, TTS/STT/wake word, router provider, lưu lịch sử hội thoại trên máy.
