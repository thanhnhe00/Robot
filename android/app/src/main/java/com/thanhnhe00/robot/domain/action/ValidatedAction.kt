package com.thanhnhe00.robot.domain.action

/**
 * Action đã được ActionValidator kiểm tra và xác thực an toàn tuyệt đối.
 * Chỉ có kiểu ValidatedAction mới được phép truyền vào ActionExecutor.
 */
sealed interface ValidatedAction {
    data object GetTime : ValidatedAction

    data object GetBattery : ValidatedAction

    data class SetAlarm(
        val hour: Int,
        val minute: Int,
        val label: String? = null
    ) : ValidatedAction

    data class OpenApp(
        val packageName: String
    ) : ValidatedAction

    data class SetVolume(
        val percent: Int
    ) : ValidatedAction
}
