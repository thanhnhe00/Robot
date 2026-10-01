package com.thanhnhe00.robot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thanhnhe00.robot.data.provider.BackendProvider
import com.thanhnhe00.robot.data.provider.DebugScriptedProvider
import com.thanhnhe00.robot.data.provider.MockProvider
import com.thanhnhe00.robot.domain.action.ValidatedAction
import com.thanhnhe00.robot.domain.exec.ActionExecutor
import com.thanhnhe00.robot.platform.AndroidAlarmScheduler
import com.thanhnhe00.robot.platform.AndroidAppLauncher
import com.thanhnhe00.robot.platform.AndroidBatteryReader
import com.thanhnhe00.robot.platform.AndroidTimeProvider
import com.thanhnhe00.robot.platform.AndroidVolumeController
import com.thanhnhe00.robot.ui.ProviderChoice
import com.thanhnhe00.robot.ui.RobotApprovalCard
import com.thanhnhe00.robot.ui.RobotFace
import com.thanhnhe00.robot.ui.RobotSpeechBubble
import com.thanhnhe00.robot.ui.RobotViewModel
import com.thanhnhe00.robot.ui.theme.RobotTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RobotTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                ) { innerPadding ->
                    RobotMainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun RobotMainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val executor = remember {
        ActionExecutor(
            timeProvider = AndroidTimeProvider(),
            batteryReader = AndroidBatteryReader(context),
            alarmScheduler = AndroidAlarmScheduler(context),
            appLauncher = AndroidAppLauncher(context),
            volumeController = AndroidVolumeController(context)
        )
    }

    val backendProvider = remember {
        BackendProvider(
            baseUrl = BuildConfig.BACKEND_BASE_URL,
            apiKey = BuildConfig.BACKEND_API_KEY
        )
    }
    val mockProvider = remember { MockProvider() }
    val debugProvider = remember { DebugScriptedProvider() }

    val viewModel: RobotViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RobotViewModel(
                    backendProvider = backendProvider,
                    mockProvider = mockProvider,
                    debugProvider = debugProvider,
                    executor = executor
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 1. Mặt Robot biểu cảm động (RobotFace)
            RobotFace(
                state = uiState.robotState,
                isAwaitingApproval = uiState.isAwaitingApproval
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Bong bóng thoại của Robot (SpeechBubble)
            RobotSpeechBubble(
                speech = uiState.speechBubble,
                state = uiState.robotState,
                safetyWarning = uiState.safetyWarning,
                onRetry = { viewModel.retryLastQuery() },
                onDismissError = { viewModel.dismissError() }
            )

            // 3. Hộp thoại xác nhận duyệt hành động (Khi có Action chờ duyệt)
            AnimatedVisibility(
                visible = uiState.isAwaitingApproval && uiState.pendingActionLabel != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    RobotApprovalCard(
                        actionLabel = uiState.pendingActionLabel ?: "",
                        onApprove = { viewModel.approvePendingAction() },
                        onReject = { viewModel.rejectPendingAction() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Ô nhập tin nhắn cho Robot
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Trò chuyện cùng Robot (tiếng Việt)") },
                placeholder = { Text("Ví dụ: Mở YouTube, mấy giờ rồi, hack...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isProcessing && !uiState.isAwaitingApproval,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.submitText(inputText)
                        inputText = ""
                    }
                })
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Nút gửi và các mẫu thử nhanh
            val samples = when (uiState.selectedProvider) {
                ProviderChoice.DEBUG_SCRIPTED -> listOf("hack", "evil", "999", "move", "25:00", "lie")
                else -> listOf("Mấy giờ rồi?", "Mức pin?", "Báo thức 07:30", "Mở YouTube", "Âm lượng 40")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                samples.take(3).forEach { sample ->
                    OutlinedButton(
                        onClick = {
                            inputText = sample
                            viewModel.submitText(sample)
                            inputText = ""
                        },
                        enabled = !uiState.isProcessing && !uiState.isAwaitingApproval,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(sample, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            if (samples.size > 3) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    samples.drop(3).take(3).forEach { sample ->
                        OutlinedButton(
                            onClick = {
                                inputText = sample
                                viewModel.submitText(sample)
                                inputText = ""
                            },
                            enabled = !uiState.isProcessing && !uiState.isAwaitingApproval,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(sample, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.submitText(inputText)
                        inputText = ""
                    }
                },
                enabled = !uiState.isProcessing && !uiState.isAwaitingApproval && inputText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Robot đang xử lý...")
                } else {
                    Text("Gửi cho Robot")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Bộ chọn AI Provider
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Bộ nguồn AI (AI Provider):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    ProviderChoice.values().forEach { providerChoice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (!uiState.isProcessing) {
                                        viewModel.selectProvider(providerChoice)
                                    }
                                }
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (uiState.selectedProvider == providerChoice),
                                onClick = {
                                    if (!uiState.isProcessing) {
                                        viewModel.selectProvider(providerChoice)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = providerChoice.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = providerChoice.subtitle,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Thử nghiệm trực tiếp Actions (Phase 3.3)
            Text(
                text = "Thử nghiệm 5 Actions trực tiếp (Phase 3.3):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val r = executor.execute(ValidatedAction.GetTime)
                            viewModel.submitText("Mấy giờ rồi?")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("1. Giờ", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val r = executor.execute(ValidatedAction.GetBattery)
                            viewModel.submitText("Mức pin?")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("2. Pin", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val r = executor.execute(ValidatedAction.SetAlarm(7, 30, "Robot báo thức"))
                            viewModel.submitText("Báo thức 07:30")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("3. Báo thức", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val r = executor.execute(ValidatedAction.OpenApp("com.google.android.youtube"))
                            viewModel.submitText("Mở YouTube")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("4. YouTube", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val r = executor.execute(ValidatedAction.SetVolume(35))
                            viewModel.submitText("Âm lượng 40")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("5. Âm lượng", fontSize = 11.sp)
                }
            }
        }
    }
}
