package com.thanhnhe00.robot.ui

import com.thanhnhe00.robot.domain.action.ValidatedAction
import com.thanhnhe00.robot.domain.exec.ExecutionResult
import com.thanhnhe00.robot.domain.state.RobotState

enum class ProviderChoice(val title: String, val subtitle: String) {
    BACKEND("Backend (FastAPI)", "Kết nối laptop qua ADB reverse (127.0.0.1:8000)"),
    MOCK("Mock (Offline)", "Luật cục bộ xác định, không cần mạng"),
    DEBUG_SCRIPTED("Debug (An Toàn)", "Kịch bản test vi phạm & từ chối an toàn")
}

/**
 * Toàn bộ trạng thái giao diện của Robot (Phase 3.5).
 */
data class RobotUiState(
    val robotState: RobotState = RobotState.IDLE,
    val selectedProvider: ProviderChoice = ProviderChoice.MOCK,
    val speechBubble: String = "Chào bạn! Mình là Robot. Bạn cần mình giúp gì?",
    val pendingAction: ValidatedAction? = null,
    val pendingActionLabel: String? = null,
    val lastExecutionResult: ExecutionResult? = null,
    val safetyWarning: String? = null,
    val errorMessage: String? = null,
    val isProcessing: Boolean = false
) {
    val isAwaitingApproval: Boolean get() = pendingAction != null
}
