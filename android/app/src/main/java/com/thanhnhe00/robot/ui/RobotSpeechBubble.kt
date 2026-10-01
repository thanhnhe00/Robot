package com.thanhnhe00.robot.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanhnhe00.robot.domain.state.RobotState

@Composable
fun RobotSpeechBubble(
    speech: String,
    state: RobotState,
    safetyWarning: String?,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleBg = when (state) {
        RobotState.ERROR -> Color(0xFF2A1519)
        RobotState.SPEAKING -> Color(0xFF0F242A)
        RobotState.PROCESSING -> Color(0xFF1E1B4B)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleBorder = when (state) {
        RobotState.ERROR -> Color(0xFFFF5252)
        RobotState.SPEAKING -> Color(0xFF00E5FF)
        RobotState.PROCESSING -> Color(0xFF7C4DFF)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(bubbleBg)
                .border(1.dp, bubbleBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = when (state) {
                        RobotState.IDLE -> "🟢 Robot sẵn sàng"
                        RobotState.PROCESSING -> "🟣 Đang suy nghĩ..."
                        RobotState.SPEAKING -> "🔵 Đang trả lời"
                        RobotState.ERROR -> "🔴 Thông báo lỗi"
                        else -> "Robot"
                    }
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = bubbleBorder
                    )

                    if (state == RobotState.PROCESSING) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = bubbleBorder
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val speechTextColor = if (state == RobotState.IDLE) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    Color(0xFFF8FAFC)
                }

                Text(
                    text = speech,
                    fontSize = 15.sp,
                    color = speechTextColor,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp
                )

                if (safetyWarning != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = safetyWarning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF8A80)
                    )
                }

                AnimatedVisibility(
                    visible = (state == RobotState.ERROR),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismissError,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Bỏ qua", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onRetry,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Thử lại", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
