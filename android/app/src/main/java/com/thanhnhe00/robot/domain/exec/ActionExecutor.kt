package com.thanhnhe00.robot.domain.exec

import com.thanhnhe00.robot.domain.action.ActionValidator
import com.thanhnhe00.robot.domain.action.ValidatedAction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Bộ thực thi hành động trên hệ điều hành Android (Lớp bảo vệ cuối cùng).
 *
 * Nguyên tắc an toàn bất biến:
 * 1. CHỈ NHẬN kiểu dữ liệu ValidatedAction (compiler chặn đứng các ProposedAction chưa qua kiểm duyệt).
 * 2. Mọi hành động đều chạy trong giới hạn timeout (withTimeout) được định nghĩa trong ActionSpec.
 * 3. Bắt toàn bộ lỗi bảo mật (SecurityException) và ngoại lệ runtime, không bao giờ để crash app.
 */
class ActionExecutor(
    private val timeProvider: TimeProvider,
    private val batteryReader: BatteryReader,
    private val alarmScheduler: AlarmScheduler,
    private val appLauncher: AppLauncher,
    private val volumeController: VolumeController,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    suspend fun execute(action: ValidatedAction): ExecutionResult = withContext(dispatcher) {
        val specName = when (action) {
            is ValidatedAction.GetTime -> "get_time"
            is ValidatedAction.GetBattery -> "get_battery"
            is ValidatedAction.SetAlarm -> "set_alarm"
            is ValidatedAction.OpenApp -> "open_app"
            is ValidatedAction.SetVolume -> "set_volume"
        }

        val timeoutMs = ActionValidator.REGISTRY[specName]?.timeoutMs ?: 3000L

        try {
            val result = withTimeoutOrNull(timeoutMs) {
                kotlinx.coroutines.runInterruptible {
                    executeInternal(action)
                }
            }
            result ?: ExecutionResult.Failed(
                userMessage = "Thực hiện hành động quá thời gian cho phép ($timeoutMs ms).",
                category = FailureCategory.TIMEOUT
            )
        } catch (e: SecurityException) {
            ExecutionResult.Failed(
                userMessage = "Thiếu quyền hệ thống để thực hiện hành động.",
                category = FailureCategory.PERMISSION_DENIED
            )
        } catch (e: Exception) {
            ExecutionResult.Failed(
                userMessage = "Không thể thực hiện hành động: ${e.localizedMessage ?: "Lỗi không xác định"}",
                category = FailureCategory.EXECUTION_ERROR
            )
        }
    }

    private fun executeInternal(action: ValidatedAction): ExecutionResult {
        return when (action) {
            is ValidatedAction.GetTime -> {
                val formattedTime = timeProvider.getCurrentTimeFormatted()
                ExecutionResult.Success(formattedTime)
            }

            is ValidatedAction.GetBattery -> {
                val percent = batteryReader.getBatteryPercentage()
                if (percent >= 0) {
                    ExecutionResult.Success("Pin hiện tại còn $percent%.")
                } else {
                    ExecutionResult.Failed(
                        userMessage = "Không thể đọc thông tin pin từ hệ thống.",
                        category = FailureCategory.EXECUTION_ERROR
                    )
                }
            }

            is ValidatedAction.SetAlarm -> {
                val ok = alarmScheduler.setAlarm(action.hour, action.minute, action.label)
                if (ok) {
                    val labelInfo = if (action.label.isNullOrBlank()) "" else " (${action.label})"
                    val formatted = String.format("%02d:%02d", action.hour, action.minute)
                    ExecutionResult.Success("Đã mở cài đặt báo thức lúc $formatted$labelInfo.")
                } else {
                    ExecutionResult.Failed(
                        userMessage = "Không thể mở ứng dụng đồng hồ để đặt báo thức.",
                        category = FailureCategory.EXECUTION_ERROR
                    )
                }
            }

            is ValidatedAction.OpenApp -> {
                val installed = appLauncher.isAppInstalled(action.packageName)
                if (!installed) {
                    val appName = getFriendlyAppName(action.packageName)
                    return ExecutionResult.Failed(
                        userMessage = "Ứng dụng $appName chưa được cài đặt trên máy.",
                        category = FailureCategory.APP_NOT_INSTALLED
                    )
                }
                val ok = appLauncher.launchApp(action.packageName)
                if (ok) {
                    val appName = getFriendlyAppName(action.packageName)
                    ExecutionResult.Success("Đang mở $appName...")
                } else {
                    ExecutionResult.Failed(
                        userMessage = "Không thể khởi chạy ứng dụng.",
                        category = FailureCategory.EXECUTION_ERROR
                    )
                }
            }

            is ValidatedAction.SetVolume -> {
                val ok = volumeController.setMusicVolume(action.percent)
                if (ok) {
                    ExecutionResult.Success("Đã chỉnh âm lượng về ${action.percent}%.")
                } else {
                    ExecutionResult.Failed(
                        userMessage = "Không thể điều chỉnh âm lượng.",
                        category = FailureCategory.EXECUTION_ERROR
                    )
                }
            }
        }
    }

    private fun getFriendlyAppName(packageName: String): String {
        return when (packageName) {
            "com.google.android.youtube" -> "YouTube"
            "com.android.chrome" -> "Chrome"
            "com.android.settings" -> "Cài đặt"
            "com.spotify.music" -> "Spotify"
            else -> packageName
        }
    }
}
