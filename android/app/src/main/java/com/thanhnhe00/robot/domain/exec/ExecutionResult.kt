package com.thanhnhe00.robot.domain.exec

enum class FailureCategory {
    TIMEOUT,
    APP_NOT_INSTALLED,
    PERMISSION_DENIED,
    SECURITY_ERROR,
    EXECUTION_ERROR
}

sealed interface ExecutionResult {
    val userMessage: String

    data class Success(
        override val userMessage: String
    ) : ExecutionResult

    data class Failed(
        override val userMessage: String,
        val category: FailureCategory = FailureCategory.EXECUTION_ERROR
    ) : ExecutionResult

    val isSuccess: Boolean get() = this is Success
}
