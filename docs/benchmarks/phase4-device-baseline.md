# Phase 4 — Z Flip5 Device Baseline

Ngày ghi nhận: 2026-10-01
Phương thức đo: Truy vấn trực tiếp qua `adb shell` trên thiết bị vật lý kết nối serial `R5CW72WTDDR`.

## 1. Định danh thiết bị (Device Identity)
- **Manufacturer / Brand:** Samsung (`samsung`)
- **Device Model:** `SM-F731B` (Galaxy Z Flip5, phiên bản quốc tế)
- **Product Name:** `b5qxxx`
- **Board Platform:** `kalama` (Qualcomm Snapdragon 8 Gen 2 for Galaxy / `SM8550`, TSMC 4nm)
- **Hardware:** `qcom`
- **CPU Topology:** 8 nhân (1x Cortex-X3 @ 3.36 GHz + 4x Cortex-A715/A710 @ 2.8 GHz + 3x Cortex-A510 @ 2.0 GHz)
- **Primary ABI:** `arm64-v8a`
- **Supported ABIs:** `arm64-v8a, armeabi-v7a, armeabi`

## 2. Hệ điều hành & Firmware
- **Android Release:** `15`
- **Android SDK API:** `35`
- **Security Patch:** `2025-04-01`
- **Build Fingerprint:** Samsung One UI 7.0 build `70000`

## 3. Bộ nhớ RAM (System Memory)
- **MemTotal:** 7,347,128 kB (~7.01 GiB / ~7.35 GB vật lý)
- **MemFree:** 419,744 kB (~410 MB)
- **MemAvailable:** 1,981,232 kB (~1.89 GiB / ~1.98 GB)
- **Cached:** 1,675,280 kB
- **SwapCached:** 5,148 kB
- **Nhận định Memory Budget:** One UI 7 và các tiến trình hệ thống chiếm ~5.3 GB RAM. Dung lượng RAM khả dụng thực tế an toàn cho ứng dụng và LLM runtime dao động trong khoảng **1.8 GB – 2.0 GB**.

## 4. Bộ nhớ trong (Storage)
- **Phân vùng `/data`:** Dung lượng tổng 461 GB, đã dùng 58 GB, **khả dụng 402 GB** (13% use).
- **Nhận định Storage:** Hoàn toàn đủ dung lượng lưu trữ nhiều file model GGUF (kích thước từ 400 MB đến 2.5 GB).

## 5. Trạng thái Bộ nhớ Tiến trình App (`com.thanhnhe00.robot`)
Đo qua `dumpsys meminfo com.thanhnhe00.robot` ở trạng thái IDLE (chưa nạp LLM):
- **TOTAL Pss:** 127,828 kB (~125 MB)
- **Native Heap:** 8,265 kB (~8 MB)
- **Dalvik Heap:** 6,788 kB (~6.6 MB)
- **Private Dirty:** 84,940 kB (~83 MB)
- **RSS Total:** 219,072 kB (~214 MB)

## 6. Trạng thái Nhiệt độ & Pin (Battery & Thermal)
Đo qua `dumpsys battery` và `dumpsys thermalservice`:
- **Pin:** 71%, điện áp 3,788 mV, tình trạng sức khỏe tốt (health: 2).
- **Nhiệt độ Pin (BAT):** 36.0°C
- **Nhiệt độ Chip xử lý (AP):** 38.2°C
- **Nhiệt độ Mặt lưng (SKIN):** 37.2°C
- **Thermal Status:** `1` (NONE / LIGHT - trạng thái nhiệt an toàn, chưa xuất hiện Thermal Throttling).
