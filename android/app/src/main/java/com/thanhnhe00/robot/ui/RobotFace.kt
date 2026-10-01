package com.thanhnhe00.robot.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.thanhnhe00.robot.R
import com.thanhnhe00.robot.domain.state.RobotState
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Giao diện khuôn mặt Robot sống động (Dynamic Expressive Robot Face).
 *
 * Vẽ hoàn toàn bằng Jetpack Compose Canvas với các cử chỉ:
 * - Chớp mắt tự nhiên định kỳ (Organic periodic blink).
 * - Nghiêng mắt tò mò và đảo mắt khi PROCESSING.
 * - Mắt cong hình bán nguyệt tươi cười + miệng sóng âm thanh khi SPEAKING.
 * - Mắt to tròn long lanh chờ duyệt khi AWAITING_APPROVAL.
 * - Mắt chéo áy náy + viền đỏ cam khi ERROR.
 */
@Composable
fun RobotFace(
    state: RobotState,
    isAwaitingApproval: Boolean,
    modifier: Modifier = Modifier
) {
    val stateDescription = when {
        isAwaitingApproval -> stringResource(R.string.state_awaiting_approval)
        state == RobotState.IDLE -> stringResource(R.string.state_idle)
        state == RobotState.PROCESSING -> stringResource(R.string.state_processing)
        state == RobotState.SPEAKING -> stringResource(R.string.state_speaking)
        state == RobotState.ERROR -> stringResource(R.string.state_error)
        else -> stringResource(R.string.state_idle)
    }

    // Quản lý chớp mắt tự nhiên theo chu kỳ
    var blinkProgress by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(state) {
        while (true) {
            val nextBlinkDelay = Random.nextLong(2800L, 5000L)
            delay(nextBlinkDelay)
            // Nhắm mắt nhanh
            blinkProgress = 0.08f
            delay(120L)
            // Mở mắt lại
            blinkProgress = 1f
        }
    }

    val animatedBlink by animateFloatAsState(
        targetValue = blinkProgress,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "blinkAnimation"
    )

    // Infinite transitions cho các hiệu ứng động liên tục
    val infiniteTransition = rememberInfiniteTransition(label = "robotFaceInfinite")

    // Nhịp thở / hào quang neon
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    // Đảo mắt khi PROCESSING
    val pupilScanOffset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pupilScan"
    )

    // Sóng âm miệng khi SPEAKING
    val mouthWave1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouthWave1"
    )
    val mouthWave2 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouthWave2"
    )

    // Màu sắc chủ đạo theo trạng thái
    val (primaryGlowColor, secondaryGlowColor) = when {
        isAwaitingApproval -> Color(0xFFFFD600) to Color(0xFF00E5FF) // Vàng kim + Cyan
        state == RobotState.PROCESSING -> Color(0xFF7C4DFF) to Color(0xFF00E5FF) // Tím điện tử + Cyan
        state == RobotState.SPEAKING -> Color(0xFF00E676) to Color(0xFF00E5FF) // Xanh ngọc + Cyan
        state == RobotState.ERROR -> Color(0xFFFF5252) to Color(0xFFFF9100) // Đỏ cam cảnh báo
        else -> Color(0xFF00E5FF) to Color(0xFF0091EA) // Cyan Neon thư giãn
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(horizontal = 8.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0F1D),
                        Color(0xFF050811)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        primaryGlowColor.copy(alpha = 0.6f * pulseGlow),
                        secondaryGlowColor.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .semantics { contentDescription = stateDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val eyeWidth = width * 0.22f
            val baseEyeHeight = height * 0.44f
            val currentEyeHeight = baseEyeHeight * animatedBlink
            val eyeSpacing = width * 0.16f

            val leftEyeCenter = Offset(x = width / 2f - eyeSpacing - eyeWidth / 2f, y = height * 0.44f)
            val rightEyeCenter = Offset(x = width / 2f + eyeSpacing + eyeWidth / 2f, y = height * 0.44f)

            // 1. Vẽ hai mắt Robot
            drawRobotEye(
                center = leftEyeCenter,
                width = eyeWidth,
                height = currentEyeHeight,
                state = state,
                isAwaitingApproval = isAwaitingApproval,
                isLeft = true,
                pupilScan = if (state == RobotState.PROCESSING) pupilScanOffset else 0f,
                primaryColor = primaryGlowColor,
                secondaryColor = secondaryGlowColor
            )

            drawRobotEye(
                center = rightEyeCenter,
                width = eyeWidth,
                height = currentEyeHeight,
                state = state,
                isAwaitingApproval = isAwaitingApproval,
                isLeft = false,
                pupilScan = if (state == RobotState.PROCESSING) pupilScanOffset else 0f,
                primaryColor = primaryGlowColor,
                secondaryColor = secondaryGlowColor
            )

            // 2. Vẽ miệng / Sóng âm thanh
            drawRobotMouth(
                centerX = width / 2f,
                centerY = height * 0.78f,
                state = state,
                isAwaitingApproval = isAwaitingApproval,
                mouthWave1 = mouthWave1,
                mouthWave2 = mouthWave2,
                color = primaryGlowColor
            )
        }
    }
}

private fun DrawScope.drawRobotEye(
    center: Offset,
    width: Float,
    height: Float,
    state: RobotState,
    isAwaitingApproval: Boolean,
    isLeft: Boolean,
    pupilScan: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val rotationAngle = when {
        state == RobotState.PROCESSING -> if (isLeft) -9f else 9f
        state == RobotState.ERROR -> if (isLeft) 14f else -14f
        isAwaitingApproval -> if (isLeft) -5f else 5f
        else -> 0f
    }

    rotate(degrees = rotationAngle, pivot = center) {
        val cornerRadius = CornerRadius(width / 2f, width / 2f)

        // Hào quang ngoài (Outer Glow)
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent),
                center = center,
                radius = width * 1.1f
            ),
            topLeft = Offset(center.x - width * 0.65f, center.y - height * 0.65f),
            size = Size(width * 1.3f, height * 1.3f),
            cornerRadius = CornerRadius(width * 0.65f, width * 0.65f)
        )

        if (state == RobotState.SPEAKING) {
            // Mắt cong hình trăng lưỡi liềm tươi cười (Smiling Arched Eyes)
            val path = Path().apply {
                val startX = center.x - width / 2f
                val endX = center.x + width / 2f
                val topY = center.y + height * 0.15f
                moveTo(startX, topY)
                quadraticTo(center.x, center.y - height * 0.45f, endX, topY)
            }
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = width * 0.35f, cap = StrokeCap.Round)
            )
        } else {
            // Thân mắt hình viên thuốc bo tròn (Pill Capsule)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, secondaryColor),
                    startY = center.y - height / 2f,
                    endY = center.y + height / 2f
                ),
                topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
                size = Size(width, height),
                cornerRadius = cornerRadius
            )

            // Đốm phản chiếu ánh sáng (Sparkle Highlight) tạo cảm giác có hồn
            if (height > 20f) {
                val highlightOffset = Offset(
                    x = center.x - width * 0.18f + pupilScan * 0.4f,
                    y = center.y - height * 0.22f
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = width * 0.16f,
                    center = highlightOffset
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = width * 0.08f,
                    center = Offset(highlightOffset.x + width * 0.28f, highlightOffset.y + height * 0.32f)
                )
            }
        }
    }
}

private fun DrawScope.drawRobotMouth(
    centerX: Float,
    centerY: Float,
    state: RobotState,
    isAwaitingApproval: Boolean,
    mouthWave1: Float,
    mouthWave2: Float,
    color: Color
) {
    when {
        state == RobotState.SPEAKING -> {
            // Equalizer 5 cột sóng âm thanh mấp máy theo câu nói
            val barCount = 5
            val barSpacing = 16f
            val totalWidth = barCount * barSpacing
            val startX = centerX - totalWidth / 2f

            for (i in 0 until barCount) {
                val factor = when (i) {
                    0, 4 -> mouthWave1 * 0.5f + 0.2f
                    1, 3 -> mouthWave2 * 0.8f + 0.3f
                    else -> ((mouthWave1 + mouthWave2) / 2f).coerceIn(0.4f, 1f)
                }
                val barHeight = 28f * factor
                drawLine(
                    color = color,
                    start = Offset(startX + i * barSpacing, centerY - barHeight / 2f),
                    end = Offset(startX + i * barSpacing, centerY + barHeight / 2f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }
        }

        state == RobotState.PROCESSING -> {
            // 3 chấm nhỏ nhấp nháy suy nghĩ
            val dotSpacing = 18f
            for (i in -1..1) {
                drawCircle(
                    color = color.copy(alpha = 0.8f),
                    radius = 4.5f,
                    center = Offset(centerX + i * dotSpacing, centerY)
                )
            }
        }

        isAwaitingApproval -> {
            // Miệng tròn tò mò ":o"
            drawCircle(
                color = color,
                radius = 8f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4f)
            )
        }

        state == RobotState.ERROR -> {
            // Nét lượn sóng nhẹ áy náy
            val path = Path().apply {
                moveTo(centerX - 24f, centerY + 4f)
                quadraticTo(centerX, centerY - 6f, centerX + 24f, centerY + 4f)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )
        }

        else -> {
            // IDLE: Nụ cười mỉm nhẹ thanh lịch
            val path = Path().apply {
                moveTo(centerX - 20f, centerY - 2f)
                quadraticTo(centerX, centerY + 6f, centerX + 20f, centerY - 2f)
            }
            drawPath(
                path = path,
                color = color.copy(alpha = 0.7f),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }
    }
}
