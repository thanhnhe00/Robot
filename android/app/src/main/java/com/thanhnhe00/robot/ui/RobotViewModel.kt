package com.thanhnhe00.robot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhnhe00.robot.data.provider.AIProvider
import com.thanhnhe00.robot.data.provider.ChatRequest
import com.thanhnhe00.robot.data.provider.ProviderError
import com.thanhnhe00.robot.domain.action.ActionValidator
import com.thanhnhe00.robot.domain.action.ValidatedAction
import com.thanhnhe00.robot.domain.action.ValidationResult
import com.thanhnhe00.robot.domain.exec.ActionExecutor
import com.thanhnhe00.robot.domain.state.RobotEvent
import com.thanhnhe00.robot.domain.state.RobotState
import com.thanhnhe00.robot.domain.state.reduce
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RobotViewModel(
    private val backendProvider: AIProvider,
    private val mockProvider: AIProvider,
    private val debugProvider: AIProvider,
    private val executor: ActionExecutor
) : ViewModel() {

    private val _uiState = MutableStateFlow(RobotUiState())
    val uiState: StateFlow<RobotUiState> = _uiState.asStateFlow()

    private var speechJob: Job? = null
    private var lastUserQuery: String = ""

    companion object {
        const val SAFE_FALLBACK_MESSAGE = "Xin lỗi, mình chưa thực hiện được yêu cầu đó."
    }

    private val activeProvider: AIProvider
        get() = when (_uiState.value.selectedProvider) {
            ProviderChoice.BACKEND -> backendProvider
            ProviderChoice.MOCK -> mockProvider
            ProviderChoice.DEBUG_SCRIPTED -> debugProvider
        }

    fun selectProvider(choice: ProviderChoice) {
        if (_uiState.value.isProcessing) return
        _uiState.update { it.copy(selectedProvider = choice) }
    }

    /**
     * Gửi yêu cầu bằng văn bản tới Robot.
     * Quy trình: reduce sang PROCESSING -> AI Provider -> ActionValidator -> Hỏi duyệt -> ActionExecutor -> SPEAKING -> IDLE
     */
    fun submitText(text: String) {
        val query = text.trim()
        if (query.isEmpty() || _uiState.value.isProcessing) return

        lastUserQuery = query
        speechJob?.cancel()

        val nextState = reduce(_uiState.value.robotState, RobotEvent.SubmitText(query))
        _uiState.update {
            it.copy(
                robotState = nextState,
                isProcessing = true,
                errorMessage = null,
                safetyWarning = null,
                pendingAction = null,
                pendingActionLabel = null,
                speechBubble = "Đang suy nghĩ..."
            )
        }

        viewModelScope.launch {
            val genResult = activeProvider.generate(ChatRequest(text = query))

            genResult.fold(
                onSuccess = { reply ->
                    handleModelReply(reply.response, reply.action)
                },
                onFailure = { error ->
                    handleProviderError(error)
                }
            )
        }
    }

    private fun handleModelReply(responseText: String, proposed: com.thanhnhe00.robot.domain.action.ProposedAction?) {
        if (proposed == null) {
            // Không có action -> chuyển trực tiếp sang SPEAKING câu trả lời
            finishProcessingWithSpeech(responseText)
            return
        }

        // Bước thẩm định an toàn (ActionValidator)
        when (val validation = ActionValidator.validate(proposed)) {
            is ValidationResult.Valid -> {
                val action = validation.action
                val label = describeAction(action)

                // THEO YÊU CẦU CỦA USER: Mọi cử chỉ/hành động robot đều phải hiển thị hộp thoại duyệt trước khi chạy
                _uiState.update {
                    it.copy(
                        robotState = RobotState.SPEAKING,
                        isProcessing = false,
                        pendingAction = action,
                        pendingActionLabel = label,
                        safetyWarning = null,
                        speechBubble = "Mình muốn thực hiện: $label. Bạn có cho phép không?"
                    )
                }
            }

            is ValidationResult.Rejected -> {
                // TỪ CHỐI AN TOÀN: Executor tuyệt đối KHÔNG bao giờ được gọi
                val code = validation.reason.name
                val warning = "TỪ CHỐI AN TOÀN [$code]: Hành động '${proposed.type}' bị chặn!"

                // Quy tắc mục 4: Nếu model nói dối khẳng định đã làm khi action bị từ chối -> thay bằng câu an toàn
                val finalSpeech = if (isAffirmativeOrLying(responseText)) {
                    SAFE_FALLBACK_MESSAGE
                } else {
                    responseText.ifBlank { SAFE_FALLBACK_MESSAGE }
                }

                _uiState.update {
                    it.copy(
                        robotState = RobotState.ERROR,
                        isProcessing = false,
                        safetyWarning = warning,
                        speechBubble = finalSpeech
                    )
                }
            }
        }
    }

    /**
     * Người dùng duyệt cho phép thực thi hành động đã thẩm định an toàn.
     */
    fun approvePendingAction() {
        val action = _uiState.value.pendingAction ?: return
        _uiState.update { it.copy(pendingAction = null, pendingActionLabel = null, isProcessing = true) }

        viewModelScope.launch {
            val result = executor.execute(action)
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    lastExecutionResult = result
                )
            }
            finishProcessingWithSpeech(result.userMessage)
        }
    }

    /**
     * Người dùng từ chối / hủy bỏ hành động của robot.
     */
    fun rejectPendingAction() {
        _uiState.update {
            it.copy(
                pendingAction = null,
                pendingActionLabel = null,
                isProcessing = false
            )
        }
        finishProcessingWithSpeech("Đã hủy thực hiện hành động theo ý bạn.")
    }

    private fun handleProviderError(error: Throwable) {
        val msg = when (error) {
            is ProviderError.Unauthorized -> "Lỗi xác thực: Khóa API không đúng hoặc thiếu."
            is ProviderError.Timeout -> "Hết thời gian chờ phản hồi từ nhà cung cấp AI (Timeout)."
            is ProviderError.ProviderUnavailable -> "Máy chủ AI không khả dụng (HTTP ${error.statusCode})."
            is ProviderError.Network -> "Lỗi kết nối mạng: Không kết nối được đến máy chủ."
            is ProviderError.InvalidResponse -> "Lỗi phản hồi dữ liệu: ${error.msg}"
            else -> "Lỗi: ${error.localizedMessage ?: error.message}"
        }

        val nextState = reduce(_uiState.value.robotState, RobotEvent.ErrorOccurred(msg))
        _uiState.update {
            it.copy(
                robotState = nextState,
                isProcessing = false,
                errorMessage = msg,
                speechBubble = msg
            )
        }
    }

    fun retryLastQuery() {
        if (lastUserQuery.isNotBlank()) {
            submitText(lastUserQuery)
        }
    }

    fun dismissError() {
        val nextState = reduce(_uiState.value.robotState, RobotEvent.DismissError)
        _uiState.update {
            it.copy(
                robotState = nextState,
                errorMessage = null,
                safetyWarning = null,
                speechBubble = "Chào bạn! Mình có thể giúp gì tiếp cho bạn?"
            )
        }
    }

    private fun finishProcessingWithSpeech(speech: String) {
        speechJob?.cancel()

        val speakingState = reduce(_uiState.value.robotState, RobotEvent.ProcessingFinished)
        _uiState.update {
            it.copy(
                robotState = speakingState,
                isProcessing = false,
                speechBubble = speech
            )
        }

        // Thời gian hiển thị hoạt hình nói theo độ dài chữ (chưa có TTS ở Phase 3)
        val speechDurationMs = (1200L + speech.length * 35L).coerceIn(1800L, 5000L)

        speechJob = viewModelScope.launch {
            delay(speechDurationMs)
            // Khi hết thời gian nói -> tự động chuyển về IDLE
            if (_uiState.value.robotState == RobotState.SPEAKING && _uiState.value.pendingAction == null) {
                val idleState = reduce(_uiState.value.robotState, RobotEvent.SpeakingFinished)
                _uiState.update { it.copy(robotState = idleState) }
            }
        }
    }

    private fun describeAction(action: ValidatedAction): String {
        return when (action) {
            is ValidatedAction.GetTime -> "Xem giờ hệ thống"
            is ValidatedAction.GetBattery -> "Kiểm tra mức pin"
            is ValidatedAction.SetAlarm -> "Đặt báo thức lúc ${String.format("%02d:%02d", action.hour, action.minute)}" +
                    if (action.label != null) " (${action.label})" else ""
            is ValidatedAction.OpenApp -> "Mở ứng dụng ${friendlyAppName(action.packageName)}"
            is ValidatedAction.SetVolume -> "Chỉnh âm lượng về ${action.percent}%"
        }
    }

    private fun friendlyAppName(pkg: String): String {
        return when (pkg) {
            "com.google.android.youtube" -> "YouTube"
            "com.android.chrome" -> "Chrome"
            "com.android.settings" -> "Cài đặt"
            "com.spotify.music" -> "Spotify"
            else -> pkg
        }
    }

    private fun isAffirmativeOrLying(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("đã ") || lower.contains("đang ") || lower.contains("xong") ||
                lower.contains("thành công") || lower.contains("rồi nhé")
    }
}
