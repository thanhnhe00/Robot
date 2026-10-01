package com.thanhnhe00.robot.domain.model

/**
 * Kết quả thẩm định an toàn bộ nhớ của MemoryGuard (Phase 4.1).
 */
sealed interface MemoryGuardDecision {
    data class Allowed(
        val availableBytes: Long,
        val estimatedModelBytes: Long,
        val remainingHeadroomBytes: Long
    ) : MemoryGuardDecision

    data class Rejected(
        val reason: String,
        val availableBytes: Long,
        val estimatedModelBytes: Long,
        val requiredHeadroomBytes: Long
    ) : MemoryGuardDecision
}

/**
 * Trình bảo vệ bộ nhớ (MemoryGuard) - Phase 4.1.
 *
 * Chịu trách nhiệm:
 * 1. Preflight kiểm tra dung lượng RAM khả dụng so với yêu cầu của model.
 * 2. Đảm bảo luôn giữ lại một khoảng đệm an toàn (Safety Headroom) tối thiểu cho hệ điều hành
 *    và các dịch vụ cốt lõi của Samsung One UI, tránh bị Android kill app (OOM).
 *
 * KHÔNG chịu trách nhiệm quản lý vòng đời hay giải phóng con trỏ C++ của llama.cpp.
 */
class MemoryGuard(
    val minSafetyHeadroomBytes: Long = DEFAULT_MIN_HEADROOM_BYTES
) {
    companion object {
        // Mặc định giữ lại ít nhất 500 MB RAM cho OS và app UI
        const val DEFAULT_MIN_HEADROOM_BYTES: Long = 500L * 1024L * 1024L
    }

    /**
     * Thẩm định xem có đủ RAM an toàn để nạp model hay không.
     *
     * @param availableBytes Dung lượng RAM khả dụng hiện tại (lấy từ ActivityManager.MemoryInfo.availMem).
     * @param estimatedModelBytes Ước tính dung lượng RAM model sẽ chiếm khi nạp (thường = sizeBytes * 1.3).
     */
    fun evaluate(availableBytes: Long, estimatedModelBytes: Long): MemoryGuardDecision {
        if (availableBytes <= 0) {
            return MemoryGuardDecision.Rejected(
                reason = "Không thể đọc thông số RAM khả dụng của hệ thống (availMem <= 0)",
                availableBytes = availableBytes,
                estimatedModelBytes = estimatedModelBytes,
                requiredHeadroomBytes = minSafetyHeadroomBytes
            )
        }

        if (estimatedModelBytes <= 0) {
            return MemoryGuardDecision.Rejected(
                reason = "Kích thước ước lượng của model không hợp lệ (<= 0)",
                availableBytes = availableBytes,
                estimatedModelBytes = estimatedModelBytes,
                requiredHeadroomBytes = minSafetyHeadroomBytes
            )
        }

        val requiredTotal = estimatedModelBytes + minSafetyHeadroomBytes
        if (availableBytes < requiredTotal) {
            val shortageMb = (requiredTotal - availableBytes) / (1024 * 1024)
            return MemoryGuardDecision.Rejected(
                reason = "RAM khả dụng (${availableBytes / (1024 * 1024)} MB) không đủ để nạp model (${estimatedModelBytes / (1024 * 1024)} MB) kèm vùng đệm an toàn (${minSafetyHeadroomBytes / (1024 * 1024)} MB). Thiếu khoảng $shortageMb MB.",
                availableBytes = availableBytes,
                estimatedModelBytes = estimatedModelBytes,
                requiredHeadroomBytes = minSafetyHeadroomBytes
            )
        }

        val remainingHeadroom = availableBytes - estimatedModelBytes
        return MemoryGuardDecision.Allowed(
            availableBytes = availableBytes,
            estimatedModelBytes = estimatedModelBytes,
            remainingHeadroomBytes = remainingHeadroom
        )
    }
}
