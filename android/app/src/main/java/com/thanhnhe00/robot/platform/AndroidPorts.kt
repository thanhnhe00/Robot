package com.thanhnhe00.robot.platform

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.BatteryManager
import android.provider.AlarmClock
import com.thanhnhe00.robot.domain.exec.AlarmScheduler
import com.thanhnhe00.robot.domain.exec.AppLauncher
import com.thanhnhe00.robot.domain.exec.BatteryReader
import com.thanhnhe00.robot.domain.exec.TimeProvider
import com.thanhnhe00.robot.domain.exec.VolumeController
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class AndroidTimeProvider : TimeProvider {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun getCurrentTimeFormatted(): String {
        val now = LocalTime.now()
        val timeString = now.format(timeFormatter)
        return "Bây giờ là $timeString."
    }
}

class AndroidBatteryReader(private val context: Context) : BatteryReader {
    override fun getBatteryPercentage(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val capacity = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        if (capacity in 0..100) {
            return capacity
        }

        // Fallback đọc qua sticky intent ACTION_BATTERY_CHANGED
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) {
            return ((level.toFloat() / scale.toFloat()) * 100).toInt()
        }
        return -1
    }
}

class AndroidAlarmScheduler(private val context: Context) : AlarmScheduler {
    override fun setAlarm(hour: Int, minute: Int, label: String?): Boolean {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            if (!label.isNullOrBlank()) {
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
            }
            // Không gán EXTRA_SKIP_UI để hệ thống mở giao diện ứng dụng Đồng hồ cho người dùng kiểm soát/hủy
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}

class AndroidAppLauncher(private val context: Context) : AppLauncher {
    override fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun launchApp(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}

class AndroidVolumeController(private val context: Context) : VolumeController {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    override fun setMusicVolume(percent: Int): Boolean {
        val am = audioManager ?: return false
        return try {
            val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (percent * maxVolume / 100.0).roundToInt().coerceIn(0, maxVolume)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_SHOW_UI)
            true
        } catch (e: SecurityException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    override fun getMusicVolume(): Int {
        val am = audioManager ?: return 0
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (maxVolume == 0) return 0
        return ((current.toDouble() / maxVolume.toDouble()) * 100).roundToInt()
    }
}
