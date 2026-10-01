package com.thanhnhe00.robot.domain.state

/**
 * Các trạng thái vòng đời của Robot AI (ADR-0001, ADR-0006).
 *
 * Trong Phase 3 (Text & UI):
 * - Chỉ sử dụng chu trình: IDLE -> PROCESSING -> SPEAKING -> IDLE và ERROR.
 * - WAKE và LISTENING được khai báo sẵn cho Phase 5 (Voice: Wake word & STT)
 *   nhưng TUYỆT ĐỐI không thể chạm tới được ở Phase 3.
 */
enum class RobotState {
    IDLE,
    WAKE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

sealed interface RobotEvent {
    data class SubmitText(val text: String) : RobotEvent
    data object ProcessingFinished : RobotEvent
    data object SpeakingFinished : RobotEvent
    data class ErrorOccurred(val message: String) : RobotEvent
    data object DismissError : RobotEvent
}

/**
 * Hàm thuần chuyển trạng thái (Pure State Reducer).
 *
 * Đảm bảo:
 * 1. Không phụ thuộc Android runtime (pure Kotlin, testable trên JVM).
 * 2. Bỏ qua các sự kiện không hợp lệ ở trạng thái hiện tại (không crash, không ném exception).
 * 3. Chặn hoàn toàn việc vô tình chuyển sang WAKE hoặc LISTENING ở Phase 3.
 */
fun reduce(state: RobotState, event: RobotEvent): RobotState {
    return when (event) {
        is RobotEvent.SubmitText -> {
            when (state) {
                RobotState.IDLE, RobotState.ERROR -> RobotState.PROCESSING
                else -> state // Đang bận thì bỏ qua
            }
        }

        RobotEvent.ProcessingFinished -> {
            when (state) {
                RobotState.PROCESSING -> RobotState.SPEAKING
                else -> state
            }
        }

        RobotEvent.SpeakingFinished -> {
            when (state) {
                RobotState.SPEAKING -> RobotState.IDLE
                else -> state
            }
        }

        is RobotEvent.ErrorOccurred -> {
            RobotState.ERROR
        }

        RobotEvent.DismissError -> {
            when (state) {
                RobotState.ERROR -> RobotState.IDLE
                else -> state
            }
        }
    }
}
