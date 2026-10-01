package com.thanhnhe00.robot.domain.action

enum class HardwareStatus {
    READY,
    STUB
}

data class ActionSpec(
    val name: String,
    val timeoutMs: Long,
    val permission: String,
    val hardware: HardwareStatus
)
