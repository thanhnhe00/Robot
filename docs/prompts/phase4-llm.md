# PROMPT PHASE 4 — SPIKE LLM TRÊN Z FLIP5 + LOCAL PROVIDER + MODEL MANAGER v2

## 0. VAI TRÒ VÀ CONTEXT

Bạn đang tiếp tục phát triển repository **ROBOTV1**.

Repository là một Android app local-first, trong đó LLM chỉ đóng vai trò **đề xuất**, không được trực tiếp thực thi hành động.

Kiến trúc safety invariant hiện tại phải được giữ nguyên:

```text
LLM / AIProvider
      ↓
ModelReply
      ↓
ProposedAction
      ↓
ActionValidator
      ↓
ValidatedAction
      ↓
User Approval UI
      ↓
ActionExecutor
```

**Không được bypass `ActionValidator`, approval UI hoặc `ActionExecutor` contract.**

Phase 4 có mục tiêu kiểm chứng khả năng chạy LLM local thực tế trên thiết bị:

```text
Samsung Galaxy Z Flip5
SM-F731B
Snapdragon 8 Gen 2 for Galaxy
Android 15 / API 35
8 GB RAM
```

Mục tiêu của Phase 4 không phải chứng minh toàn bộ ROBOTV1 có thể chạy hoàn toàn local.

Mục tiêu chính là:

1. dựng được native LLM runtime tối thiểu trên Android ARM64;
2. quản lý GGUF model artifact an toàn;
3. tích hợp một `LocalProvider` vào `AIProvider` hiện có;
4. chạy inference thực tế trên Z Flip5;
5. đo benchmark reproducible;
6. xác định giới hạn RAM/CPU/thermal;
7. đưa ra quyết định kiến trúc dựa trên số đo thực tế.

---

# 1. SOURCE OF TRUTH — BẮT BUỘC

Trước mỗi subtask, phải đọc và đối chiếu:

```text
AGENTS.md
docs/HANDOFF.md
docs/issues/04-llm-on-z-flip5.md
docs/research/A-llm.md
docs/decisions/
android/
```

Ngoài ra phải inspect:

```text
settings.gradle.kts
build.gradle.kts
android/build.gradle.kts
AndroidManifest.xml
```

và toàn bộ source liên quan tới:

```text
AIProvider
BackendProvider
MockProvider
DebugScriptedProvider
ChatRequest
ModelReply
ProposedAction
ActionValidator
ValidatedAction
ActionExecutor
RobotViewModel
RobotUiState
ProviderChoice
```

### Quy tắc source of truth

Ưu tiên theo thứ tự:

```text
1. Code đang tồn tại
2. Tests đang tồn tại
3. ADB/device measurement
4. AGENTS.md / HANDOFF
5. Research / Issue / ADR
6. Prompt Phase 4
```

Nếu prompt mâu thuẫn với code hoặc measurement thực tế:

> Không được ép code phải phù hợp với prompt.

Phải:

1. ghi nhận mismatch;
2. giải thích;
3. chọn phương án phù hợp với architecture hiện tại;
4. cập nhật documentation nếu cần.

Không được tự tạo API/class/package chỉ vì prompt giả định chúng tồn tại.

---

# 2. QUY TẮC LÀM VIỆC

## 2.1 Một turn chỉ xử lý một subitem

Thứ tự bắt buộc:

```text
4.0
↓
4.1
↓
4.2
↓
4.3
↓
4.4
```

Không tự động nhảy sang subitem tiếp theo.

Sau mỗi subitem phải báo cáo:

```text
STATUS:
PASS / BLOCKED / PARTIAL

WHAT CHANGED

WHAT WAS MEASURED

TEST RESULT

FILES CHANGED

RISKS / BLOCKERS

NEXT STEP
```

Không commit hoặc push nếu người dùng chưa yêu cầu.

---

## 2.2 Skills

Nếu repository có các skills sau thì sử dụng đúng mục đích:

```text
/robot-task
/android-development
/ai-model-evaluation
/research-and-adr
```

Không tạo thêm workflow/abstraction chỉ để “đẹp kiến trúc”.

Ưu tiên implementation nhỏ nhất nhưng đủ để kiểm chứng hypothesis của Phase 4.

---

# 3. PHẠM VI PHASE 4

## IN SCOPE

### 3.1 Native LLM runtime

Baseline runtime:

```text
llama.cpp
CPU
ARM64 / arm64-v8a
Android NDK
JNI
```

GPU/NPU acceleration không thuộc baseline.

Nếu phát hiện acceleration khả thi thì chỉ ghi nhận dưới dạng optional follow-up, không làm scope phình ra.

---

### 3.2 Model artifact management

Phải hỗ trợ tối thiểu:

```text
GGUF metadata
file existence
file size
SHA-256
model path
model identifier
quantization
architecture metadata nếu đọc được
```

---

### 3.3 LocalProvider

Implement:

```text
LocalProvider : AIProvider
```

nhưng phải tuân thủ interface hiện có của repository.

Không thay đổi `AIProvider` chỉ để phục vụ LocalProvider nếu không thật sự cần thiết.

---

### 3.4 Benchmark

Benchmark inference thực tế trên:

```text
Samsung Galaxy Z Flip5
```

với Golden Benchmark Set cố định.

---

### 3.5 Decision

Kết quả cuối cùng phải trả lời:

```text
LLM local trên Z Flip5:
    GO
    LIMITED
    NO-GO
```

Decision chỉ áp dụng cho **local LLM feasibility**, không được suy rộng thành bằng chứng rằng toàn bộ ROBOTV1 local-first đã được chứng minh.

---

# 4. OUT OF SCOPE

Không làm:

```text
training
fine-tuning
RAG
vector database
voice STT
voice TTS
ESP32 firmware
motor control
robot hardware integration
cloud migration
GPU/NPU optimization
multi-model serving
background inference service
```

Không biến Phase 4 thành production LLM platform.

---

# 5. DEVICE BASELINE

Thiết bị mục tiêu:

```text
Samsung Galaxy Z Flip5
SM-F731B
Snapdragon 8 Gen 2 for Galaxy
Android 15
API 35
8 GB RAM
```

Các thông số RAM/storage/thermal phải được **xác minh lại bằng ADB trước benchmark**.

Không coi số liệu trong prompt là measurement thực tế.

Phải ghi:

```text
device model
Android version
API level
ABI
available RAM
total RAM
free storage
battery level
battery temperature
thermal status nếu Android expose được
CPU information nếu lấy được
```

---

# 6. MODEL SELECTION RULE

## QUAN TRỌNG

Không được hard-code model shortlist từ prompt cũ.

Model shortlist phải lấy từ:

```text
docs/research/A-llm.md
```

tại thời điểm bắt đầu Phase 4.

Research A hiện là nguồn tham chiếu cho model candidates.

Nếu shortlist trong Research A thay đổi trước khi Phase 4 chạy:

> sử dụng shortlist mới nhất trong Research A.

Nếu cần thay đổi model ngoài Research A:

1. giải thích lý do;
2. cập nhật Research A;
3. ghi lại quyết định;
4. sau đó mới benchmark.

Không tự ý đổi model giữa các benchmark.

---

## 6.1 Quantization

Ưu tiên:

```text
Q4_K_M
Q5_K_M
```

nếu artifact GGUF tồn tại và có thể kiểm thử hợp lý.

Chỉ thêm:

```text
Q6_K
Q8_0
```

nếu:

```text
storage
RAM
thermal budget
benchmark time
```

cho phép.

Không ép chạy model/quantization có nguy cơ OOM chỉ để đủ số lượng benchmark.

---

# 7. MEMORY SAFETY CONTRACT

Thiết bị có 8 GB RAM nhưng available RAM thay đổi theo trạng thái hệ thống.

Không được coi:

```text
8 GB RAM
```

là budget inference.

## 7.1 Preflight

Trước khi load model:

```text
ActivityManager.getMemoryInfo()
```

phải được gọi.

Nếu available memory thấp hơn safety threshold đã cấu hình:

```text
REJECT LOAD
```

Không load model.

Threshold phải được ghi trong report.

---

## 7.2 Memory budget

Không dùng một con số hard-coded để giả vờ rằng mọi model đều chạy được.

Phải phân loại:

```text
WITHIN_BUDGET
OVER_BUDGET
UNSAFE_TO_TEST
OOM/CRASH
```

Nếu model vượt memory budget:

```text
không được tiếp tục sustained benchmark
```

nhưng có thể ghi nhận nó là boundary/stress candidate nếu việc kiểm tra load được thực hiện an toàn.

---

## 7.3 Memory measurement

Phân biệt:

### Preflight system memory

```text
ActivityManager.MemoryInfo
```

với:

### Process/runtime memory

Ví dụ:

```text
adb shell dumpsys meminfo <package>
```

và native memory measurement khả dụng.

Không được gọi system available RAM là “RAM của model”.

Report phải phân biệt:

```text
system available memory
process memory
native memory
model file size
```

---

# 8. NATIVE MEMORY LIFECYCLE

Không được dùng JVM GC làm cơ chế chính để giải phóng LLM.

Kiến trúc:

```text
Kotlin
  ↓
JNI
  ↓
Native Runtime
  ↓
llama_model
llama_context
native buffers
```

`unload()` phải giải phóng native resources thông qua JNI.

`System.gc()` chỉ là biện pháp phụ trợ, không phải guarantee.

---

# 9. ARCHITECTURE CONTRACT

Phase 4 phải giữ separation:

```text
ModelManager
    ↓
model metadata / artifact / checksum / path
    ↓
LocalProvider
    ↓
LlamaRuntime
    ↓
JNI
    ↓
llama.cpp
```

## ModelManager KHÔNG sở hữu native handle

`ModelManager` chịu trách nhiệm:

```text
ModelMetadata
model catalog
model path
file validation
SHA-256
availability
selected model
```

Native runtime chịu trách nhiệm:

```text
native model handle
native context
threads
generation
unload
native memory
```

Không tạo:

```text
ModelManager.isLoaded = true
```

chỉ để biểu diễn native state nếu native runtime chưa thực sự load model.

Nếu cần trạng thái, phải phân biệt:

```text
artifact available
load requested
loaded
generating
unloading
failed
```

và state phải phản ánh lifecycle thực tế.

---

# 10. PHASE 4.0 — RECON + BENCHMARK FOUNDATION

## Mục tiêu

Trước khi viết LocalProvider, phải xác minh:

```text
repository architecture
native build feasibility
model candidates
benchmark protocol
device baseline
```

---

## 10.1 Repository audit

Kiểm tra:

```text
AIProvider
ChatRequest
ModelReply
ProposedAction
ActionValidator
ValidatedAction
ActionExecutor
RobotViewModel
RobotUiState
ProviderChoice
```

Xác định chính xác:

```text
package
constructor
dependency injection/wiring
tests
existing provider selection
```

Không giả định package/class.

---

## 10.2 Native toolchain audit

Kiểm tra thực tế:

```text
Android NDK
CMake
Gradle Android plugin
arm64-v8a
minSdk
targetSdk
JNI support
```

Xác định version đang dùng.

Nếu repo chưa có native infrastructure:

> chỉ dựng minimal native/JNI skeleton cần thiết cho Phase 4.

Không dựng framework native abstraction lớn.

---

## 10.3 Device baseline

Dùng ADB để ghi nhận:

```text
adb shell getprop ro.product.model
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abi
adb shell cat /proc/meminfo
adb shell df
adb shell dumpsys meminfo
```

và các thermal information khả dụng.

Lưu baseline vào issue/HANDOFF hoặc benchmark artifact phù hợp.

---

## 10.4 Golden Benchmark Set

Tạo một benchmark set cố định:

```text
20 prompts
```

chia:

```text
5 action requests
5 Vietnamese conversation
5 system-information requests
5 safety traps
```

Nên lưu dưới dạng machine-readable artifact, ví dụ:

```text
docs/benchmarks/phase4-golden-set.json
```

Mỗi case tối thiểu có:

```text
id
category
prompt
expected response class
expected action type nếu có
expected safety outcome
```

Không thay đổi prompt trong quá trình benchmark.

Nếu benchmark set thay đổi:

> tăng version benchmark set.

---

## 10.5 Action benchmark

Chỉ dùng action mà `ActionValidator` hiện hỗ trợ và có thể đi qua safety contract.

Không coi các action đang:

```text
STUB
unsupported
rejected
```

là expected-success action.

---

## 10.6 llama.cpp integration research

Xác minh:

```text
upstream version/commit
Android build method
CMake integration
JNI boundary
ARM64 build
GGUF loading API
generation API
```

Pin version/commit sử dụng.

Không sử dụng floating dependency không xác định version.

---

## 10.7 Phase 4.0 gate

Chỉ PASS 4.0 khi có:

```text
[ ] repository architecture understood
[ ] native toolchain verified
[ ] arm64 build path verified
[ ] device baseline recorded
[ ] model shortlist frozen from Research A
[ ] Golden Benchmark Set created
[ ] llama.cpp version/commit recorded
```

Nếu native toolchain không build được:

```text
STATUS = BLOCKED
```

Không chuyển sang 4.1/4.2 như thể runtime đã khả thi.

---

# 11. PHASE 4.1 — MODEL MANAGER v0

## Mục tiêu

Xây model artifact management tối thiểu.

Package phải phù hợp với package structure thực tế của repository.

Không bắt buộc dùng package:

```text
com.thanhnhe00.robot.data.model
```

nếu repo hiện tại có architecture khác.

---

## 11.1 ModelMetadata

Metadata tối thiểu:

```text
id
displayName
fileName
filePath
format
sizeBytes
sha256
architecture
parameterSize nếu xác định được
quantization
```

`isLoaded` chỉ thêm nếu state model được định nghĩa rõ ràng và không nhầm với native lifecycle.

---

## 11.2 ModelManager

ModelManager phải hỗ trợ tối thiểu:

```text
list models
get metadata
verify file exists
verify checksum
select model
validate artifact
```

Không nhúng llama.cpp implementation trực tiếp vào ModelManager.

---

## 11.3 MemoryGuard

MemoryGuard chịu trách nhiệm:

```text
preflight
available memory
minimum safety threshold
decision: allow/reject
```

Không chịu trách nhiệm:

```text
native model lifecycle
JNI context destruction
benchmark statistics
```

---

## 11.4 Tests

Có JVM/unit tests cho:

```text
metadata parsing
checksum
missing file
invalid checksum
memory guard decision
invalid model metadata
```

Nếu cần Android instrumentation test để verify ADB/device behavior thì tách riêng.

---

## 11.5 Device smoke test

Trên Z Flip5:

```text
model artifact exists
file size valid
SHA-256 valid
```

Chưa cần inference ở 4.1.

---

## 11.6 Phase 4.1 gate

PASS khi:

```text
[ ] ModelMetadata implemented
[ ] ModelManager implemented
[ ] checksum verified
[ ] MemoryGuard implemented
[ ] unit tests pass
[ ] GGUF artifact verified on device
```

---

# 12. PHASE 4.2 — LOCALPROVIDER + JNI + LLAMA.CPP

## Mục tiêu

Tạo inference path tối thiểu:

```text
AIProvider
    ↓
LocalProvider
    ↓
LlamaRuntime
    ↓
JNI
    ↓
llama.cpp
```

---

# 12.1 Không phá AIProvider

`LocalProvider` phải implement interface hiện tại:

```text
AIProvider
```

Không tự ý thay đổi public contract nếu không cần.

---

# 12.2 Lifecycle

Native runtime phải hỗ trợ:

```text
initialize
loadModel
createContext
generate
cancel/stop nếu runtime hỗ trợ
unload
destroy
```

Error path phải rõ:

```text
load failure
native exception/error
invalid model
OOM
generation failure
unload failure
```

Không để native failure làm crash app nếu có thể chuyển thành `Result.failure`.

---

# 12.3 Initial inference configuration

Baseline:

```text
threads = 4 hoặc 6
context = 1024 hoặc 2048
temperature = 0.2
```

Nhưng các thông số phải được ghi lại trong benchmark.

Không được thay đổi giữa model runs mà không ghi nhận.

---

# 12.4 System prompt

Local inference phải sử dụng system prompt phù hợp với ROBOTV1.

System prompt phải nhấn mạnh:

```text
Vietnamese-first interaction
concise responses
structured action output
no direct execution
action must pass validation
safety constraints
```

Không hard-code hardware capability chưa tồn tại.

---

# 12.5 Output contract

Local LLM raw text phải được parse thành:

```text
ModelReply
```

và nếu có action:

```text
ProposedAction
```

Output schema phải được định nghĩa rõ.

Ví dụ:

```json
{
  "response": "Mình sẽ kiểm tra pin.",
  "action": {
    "type": "get_battery",
    "params": {}
  }
}
```

hoặc:

```json
{
  "response": "Chào bạn!",
  "action": null
}
```

Parser phải xử lý:

```text
valid JSON
missing action
malformed JSON
extra text
unknown action
invalid parameters
empty output
```

---

# 12.6 Safety boundary

LocalProvider tuyệt đối không được gọi:

```text
ActionExecutor
```

và không được tạo:

```text
ValidatedAction
```

để bypass validation.

Luồng bắt buộc:

```text
raw LLM output
    ↓
parser
    ↓
ProposedAction
    ↓
existing ActionValidator
    ↓
ValidatedAction
    ↓
existing approval flow
```

---

# 12.7 Provider selection

Thêm `LOCAL` vào provider selection nếu architecture hiện tại yêu cầu.

Phải kiểm tra toàn bộ:

```text
ProviderChoice
RobotViewModel
factory/wiring
RobotUiState
UI
tests
exhaustive when
```

Không chỉ thêm một enum.

Không silent fallback:

```text
LOCAL → MOCK
```

nếu LocalProvider lỗi.

Failure phải được biểu diễn rõ.

---

# 12.8 UI

UI chỉ cần hiển thị tối thiểu:

```text
Provider: LOCAL
Model: <model>
Status: loading / ready / generating / error
```

Không xây model marketplace.

---

# 12.9 Phase 4.2 smoke test

Trước khi benchmark phải chứng minh trên Z Flip5:

```text
APK build
        ↓
JNI loads
        ↓
llama.cpp initializes
        ↓
GGUF loads
        ↓
one prompt generates
        ↓
output parsed
        ↓
ProposedAction created if applicable
        ↓
ActionValidator executes
        ↓
approval flow remains intact
        ↓
model unloads
        ↓
no crash
```

Nếu bất kỳ bước nào fail:

```text
STATUS = BLOCKED
```

Không chuyển sang 4.3 để tạo benchmark giả.

---

# 13. PHASE 4.3 — REPRODUCIBLE BENCHMARK

## Mục tiêu

Đo inference thực tế trên Z Flip5.

Benchmark phải sử dụng:

```text
Golden Benchmark Set
```

đã freeze ở 4.0.

---

# 13.1 Benchmark matrix

Mỗi model/quantization được đánh dấu:

```text
SUPPORTED
WITHIN_BUDGET
OVER_BUDGET
UNSAFE
CRASHED
OOM
```

Không bắt buộc mọi model phải benchmark sustained nếu device không đủ budget.

---

# 13.2 Warm-up

Mỗi configuration:

```text
3 warm-up runs
```

không tính vào kết quả chính.

Sau đó:

```text
5 measured runs
```

cho mỗi benchmark case/configuration nếu thời gian và thermal budget cho phép.

Nếu không đủ 5 runs:

> ghi rõ số run thực tế.

---

# 13.3 TTFT

Định nghĩa:

```text
TTFT =
timestamp(first generated token)
-
timestamp(request accepted by runtime)
```

Không tính thời gian trước request acceptance.

Nếu runtime không expose timestamp token-level:

> ghi rõ limitation và dùng measurement gần nhất có thể.

Không tự gọi một số đo khác là TTFT chuẩn.

---

# 13.4 Decode speed

Đo:

```text
generated tokens / decode duration
```

Report:

```text
median tok/s
```

và nếu đủ mẫu:

```text
p95
```

Prompt processing speed và generation speed phải phân biệt nếu runtime cung cấp cả hai.

---

# 13.5 RAM

Ghi:

```text
idle process memory
after model load
peak during generation
after unload
```

Sử dụng:

```text
adb shell dumpsys meminfo <package>
```

hoặc measurement tương đương.

Không gọi model file size là RAM usage.

---

# 13.6 Thermal

Theo dõi thermal information mà thiết bị/Android expose được.

Nếu có:

```text
battery temperature
thermal status
thermal throttling indicators
```

ghi lại.

Không giả định rằng một CPU temperature cụ thể luôn accessible trên Android.

---

# 13.7 Thermal stop condition

Nếu benchmark vượt thermal threshold được xác định trước:

```text
STOP current benchmark
UNLOAD model
RECORD thermal event
COOLDOWN
```

Không tiếp tục benchmark trong trạng thái thermal unsafe.

Không có hành vi “dừng giải nhiệt”.

Mục tiêu là:

> dừng benchmark và cho thiết bị cooldown.

---

# 13.8 Crash/OOM

Mỗi configuration phải ghi:

```text
success
crash
OOM
timeout
invalid output
native error
thermal abort
```

Không retry vô hạn.

Nếu crash:

```text
stop
capture evidence
restart app/device state nếu cần
```

và ghi rõ.

---

# 13.9 Action accuracy

Đối với action prompts:

```text
expected action
actual ProposedAction
validator result
approval requirement
```

Phân biệt:

```text
correct action
incorrect action
missing action
unsafe action
malformed output
```

Không gọi:

> JSON parse success = action success.

---

# 13.10 Safety trap benchmark

Safety trap phải kiểm tra:

```text
model refuses/does not produce unsafe action
OR
ActionValidator rejects it
```

Điểm quan trọng là:

> safety correctness không được chỉ đánh giá bằng raw LLM output.

Trust boundary cuối cùng vẫn là `ActionValidator`.

---

# 13.11 Benchmark environment

Mỗi benchmark phải ghi:

```text
device
OS/API
battery %
model
quantization
llama.cpp commit
threads
context
temperature
prompt
run number
thermal state
memory state
```

Không benchmark đồng thời với:

```text
Android Studio
ADB log spam
screen recording
large background workload
```

nếu những thứ đó ảnh hưởng measurement.

---

# 13.12 Benchmark result format

Tạo bảng:

```text
Model
Quant
Load status
TTFT median
tok/s median
Peak PSS
Thermal event
Crash/OOM
Action accuracy
Safety result
```

Không điền số liệu ước lượng.

Nếu chưa đo:

```text
N/A — not measured
```

---

# 13.13 Phase 4.3 gate

PASS khi:

```text
[ ] Golden Set unchanged
[ ] benchmark configuration recorded
[ ] at least one valid local model benchmark completed
[ ] memory measured
[ ] TTFT/tok/s measured
[ ] thermal behavior recorded
[ ] crash/OOM recorded
[ ] action validation recorded
[ ] raw evidence retained
```

Nếu toàn bộ local models fail:

> vẫn có thể PASS 4.3 nếu failure được đo và chứng minh reproducibly.

Phase 4 không được “sửa số liệu” để đạt GO.

---

# 14. PHASE 4.4 — REPORT + ARCHITECTURAL DECISION

## Mục tiêu

Tổng hợp evidence và đưa ra decision cho:

```text
local LLM feasibility on Z Flip5
```

Không ép kết quả phải là GO.

Các outcome hợp lệ:

```text
GO
LIMITED
NO-GO
```

---

# 14.1 Report

Report phải có:

```text
1. Device
2. Runtime
3. Model matrix
4. Memory
5. TTFT
6. tok/s
7. Thermal
8. Stability
9. Action correctness
10. Safety results
11. Failure cases
12. Limitations
13. Recommendation for next phase
```

---

# 14.2 Model selection

Không dùng ranking/score tùy ý.

Nếu cần chọn model cho Phase 5, phải mô tả bằng evidence:

```text
memory fit
latency
throughput
stability
action reliability
thermal behavior
```

Không chọn model chỉ vì:

```text
smallest file
highest tok/s
lowest TTFT
```

mà bỏ qua các constraint còn lại.

---

# 14.3 Architecture outcome

Kết quả có thể là:

### FULL LOCAL

LLM đủ nhẹ và ổn định cho local inference trong scope đã đo.

### LIMITED LOCAL

Local phù hợp cho:

```text
short commands
simple conversation
structured robot intents
```

nhưng không phù hợp cho workloads lớn hơn.

### HYBRID

Local xử lý:

```text
short/simple commands
offline/safety-critical lightweight intents
```

và workload nặng hơn dùng backend.

### NO-GO

Local LLM chưa đáp ứng memory/performance/stability constraints trên thiết bị mục tiêu.

Không được biến kết quả này thành tuyên bố rằng toàn bộ local-first architecture thất bại.

---

# 14.4 ADR

Không tự chuyển:

```text
ADR-0002 Proposed → Accepted
```

vì phải kiểm tra trạng thái ADR thực tế trước khi sửa.

ADR-0002 hiện có mục đích về sequencing/LLM spike.

Phase 4 phải:

```text
update ADR-0002 nếu kết quả thực sự liên quan
```

hoặc tạo ADR mới nếu cần quyết định kiến trúc riêng cho local LLM.

Nếu tạo ADR mới, phải mô tả:

```text
Context
Decision
Evidence
Alternatives
Consequences
Status
```

Không overwrite lịch sử decision cũ.

---

# 14.5 Phase 5 budget

Nếu Phase 4 đủ evidence, định nghĩa budget đề xuất cho Phase 5:

```text
max model memory
expected peak process memory
thread count
context size
acceptable TTFT
acceptable tok/s
thermal operating envelope
```

Các con số phải lấy từ measurement thực tế.

Không lấy expected values từ model card thay cho device measurement.

---

# 14.6 HANDOFF

Cập nhật:

```text
docs/HANDOFF.md
```

với:

```text
what was implemented
what was measured
device
model
runtime
known limitations
failed experiments
chosen architecture
next action
```

Cập nhật:

```text
docs/issues/04-llm-on-z-flip5.md
```

với kết quả thực tế.

---

# 15. ACCEPTANCE CRITERIA TOÀN PHASE

Phase 4 chỉ được coi là hoàn thành khi:

```text
[ ] Repository architecture was inspected
[ ] Existing AIProvider contract preserved
[ ] Safety pipeline preserved
[ ] Native toolchain verified
[ ] llama.cpp version/commit recorded
[ ] ARM64 build verified
[ ] Model shortlist comes from Research A
[ ] Golden Benchmark Set frozen
[ ] Model artifact verification implemented
[ ] SHA-256 verification works
[ ] MemoryGuard works
[ ] LocalProvider implements AIProvider
[ ] JNI runtime loads successfully
[ ] GGUF loads successfully
[ ] One real inference succeeds OR failure is reproducibly documented
[ ] Model unload works
[ ] No direct ActionExecutor call from LocalProvider
[ ] ActionValidator remains mandatory
[ ] Provider selection supports LOCAL
[ ] Benchmark is reproducible
[ ] TTFT measured or limitation documented
[ ] tok/s measured
[ ] process memory measured
[ ] thermal behavior measured
[ ] crash/OOM behavior recorded
[ ] action correctness measured
[ ] safety traps evaluated
[ ] final architecture outcome documented
[ ] ADR updated/created correctly
[ ] HANDOFF updated
[ ] issue 04 updated
```

---

# 16. NON-GOALS / ANTI-PATTERNS

Agent MUST NOT:

```text
❌ giả định class/package chưa tồn tại
❌ tự đổi AIProvider chỉ vì LocalProvider
❌ để LocalProvider gọi ActionExecutor
❌ bypass ActionValidator
❌ coi JSON hợp lệ là action an toàn
❌ coi file size là RAM usage
❌ dùng System.gc() như native memory management
❌ hard-code model shortlist trái Research A
❌ benchmark bằng số liệu ước lượng
❌ tự tạo benchmark result nếu device chưa chạy
❌ ép model vượt memory budget để lấy số liệu
❌ retry OOM vô hạn
❌ benchmark trong thermal unsafe state
❌ tự động đổi ADR status
❌ thêm GPU/NPU optimization vào Phase 4
❌ xây abstraction framework lớn quanh llama.cpp
❌ thêm dependency không cần thiết
❌ commit/push tự động
```

---

# 17. DEFINITION OF DONE

Phase 4 hoàn thành khi ROBOTV1 có bằng chứng thực tế trả lời được:

```text
1. Z Flip5 có chạy được LLM local ARM64 hay không?

2. Model nào trong Research A phù hợp với memory budget thực tế?

3. Local inference có đạt latency/throughput chấp nhận được cho workload của ROBOTV1 không?

4. Native runtime có ổn định không?

5. Thermal có trở thành constraint không?

6. LocalProvider có tích hợp được mà không phá safety architecture không?

7. Phase 5 nên đi theo:
   FULL LOCAL
   LIMITED LOCAL
   HYBRID
   hoặc NO-GO?
```

**Không được trả lời các câu hỏi trên bằng assumption.**

Phải trả lời bằng:

```text
CODE
+
TEST
+
ADB MEASUREMENT
+
BENCHMARK EVIDENCE
```

---

# 18. REQUIRED END-OF-TURN REPORT

Sau mỗi turn, trả về đúng cấu trúc:

```text
## PHASE 4 STATUS

Subtask:
4.x

Status:
PASS / PARTIAL / BLOCKED

### What changed
- ...

### Files changed
- ...

### Device evidence
- ...

### Tests
- ...

### Measurements
- ...

### Safety checks
- ...

### Known limitations
- ...

### Decision
- ...

### Next subtask
4.x+1
```

Nếu BLOCKED:

```text
## BLOCKER

Cause:
...

Evidence:
...

Why it blocks the next step:
...

Smallest next action:
...
```

Không được giả vờ PASS khi evidence chưa tồn tại.

---

# 19. FINAL PRINCIPLE

Phase 4 không phải cuộc thi để “chạy được một model”.

Mục tiêu là xây dựng **bằng chứng kỹ thuật đủ tin cậy** để ROBOTV1 quyết định cách sử dụng local LLM trên Z Flip5.

Ưu tiên:

```text
Evidence > assumption
Safety > benchmark score
Existing architecture > speculative abstraction
Reproducibility > impressive demo
Smallest working implementation > framework
```

Và invariant quan trọng nhất:

```text
LLM proposes.
Validator decides.
User approves.
Executor executes.
```

Phase 4 tuyệt đối không được phá invariant này.
