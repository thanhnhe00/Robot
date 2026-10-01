package com.thanhnhe00.robot.domain.exec

/**
 * Cổng (port) lấy giờ hệ thống.
 * Không gọi AI model, chỉ đọc đồng hồ thực của hệ điều hành.
 */
interface TimeProvider {
    fun getCurrentTimeFormatted(): String
}

/**
 * Cổng đọc thông tin phần trăm pin hiện tại của máy.
 */
interface BatteryReader {
    fun getBatteryPercentage(): Int
}

/**
 * Cổng đặt báo thức hệ thống thông qua Intent.
 */
interface AlarmScheduler {
    fun setAlarm(hour: Int, minute: Int, label: String?): Boolean
}

/**
 * Cổng khởi chạy ứng dụng ngoài thuộc whitelist đã được kiểm duyệt.
 */
interface AppLauncher {
    fun isAppInstalled(packageName: String): Boolean
    fun launchApp(packageName: String): Boolean
}

/**
 * Cổng điều khiển âm lượng media hệ thống.
 */
interface VolumeController {
    fun setMusicVolume(percent: Int): Boolean
    fun getMusicVolume(): Int
}
