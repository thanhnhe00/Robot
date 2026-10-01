package com.thanhnhe00.robot.domain.model

/**
 * Trạng thái của tệp artifact model trên bộ nhớ máy (Phase 4.1).
 *
 * Phân định rõ ràng:
 * - Artifact state: Trạng thái tệp trên đĩa (tồn tại, kiểm tra checksum).
 * - Native runtime state: Trạng thái trong RAM native (do LlamaRuntime quản lý ở Phase 4.2).
 */
enum class ModelArtifactStatus {
    NOT_DOWNLOADED,   // Chưa có file trong bộ nhớ
    CORRUPTED,        // File có kích thước hoặc SHA-256 không khớp
    VERIFIED,         // File tồn tại, khớp kích thước và SHA-256
    LOAD_REQUESTED,   // Đã yêu cầu nạp vào runtime
    LOADED,           // Runtime native đã nạp thành công
    UNLOADED          // Đã giải phóng khỏi RAM
}
