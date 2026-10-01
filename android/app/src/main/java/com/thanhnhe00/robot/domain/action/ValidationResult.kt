package com.thanhnhe00.robot.domain.action

enum class RejectionReason {
    UNKNOWN_ACTION,
    HARDWARE_NOT_READY,
    INVALID_PARAMS,
    MALFORMED
}

sealed interface ValidationResult {
    data class Valid(val action: ValidatedAction) : ValidationResult
    data class Rejected(val reason: RejectionReason) : ValidationResult

    val isValid: Boolean get() = this is Valid
}
