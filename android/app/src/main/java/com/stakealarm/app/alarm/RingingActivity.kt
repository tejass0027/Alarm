package com.stakealarm.app.alarm

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stakealarm.app.challenges.Challenge
import com.stakealarm.app.challenges.createChallenge
import com.stakealarm.app.data.Alarm
import com.stakealarm.app.data.HistoryEntry
import com.stakealarm.app.data.LocalVirtualProvider
import com.stakealarm.app.data.Repository
import com.stakealarm.app.ui.theme.StakeAlarmTheme
import java.time.LocalDateTime

class RingingActivity : ComponentActivity() {
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLockedAndTurnScreenOn()
        onBackPressedDispatcher.addCallback(this) { /* block back while ringing */ }

        val alarmId = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_ID)
        val repository = Repository(this)
        val alarm = alarmId?.let { id -> repository.loadAlarms().find { it.id == id } }

        if (alarm == null) {
            finish()
            return
        }

        val provider = LocalVirtualProvider(repository)

        setContent {
            StakeAlarmTheme {
                RingingScreen(
                    alarm = alarm,
                    onResolved = { result -> resolve(repository, provider, alarm, result) },
                    startTimeout = { seconds, onTimeout ->
                        timer = object : CountDownTimer(seconds * 1000L, 1000L) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() = onTimeout()
                        }.start()
                    }
                )
            }
        }
    }

    private fun resolve(repository: Repository, provider: LocalVirtualProvider, alarm: Alarm, result: String) {
        timer?.cancel()
        val newBalance = if (result == "solved") provider.getBalance() else provider.deduct(alarm.stake)
        repository.appendHistory(
            HistoryEntry(
                alarmId = alarm.id,
                timestamp = LocalDateTime.now().toString(),
                label = alarm.label,
                stake = alarm.stake,
                result = result,
                balanceAfter = newBalance
            )
        )
        stopService(Intent(this, AlarmRingService::class.java))
        finish()
    }

    private fun setShowWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        (getSystemService(KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)
    }
}

@Composable
private fun RingingScreen(
    alarm: Alarm,
    onResolved: (String) -> Unit,
    startTimeout: (Int, () -> Unit) -> Unit
) {
    var round by remember { mutableIntStateOf(1) }
    var challenge by remember { mutableStateOf<Challenge>(createChallenge(alarm.challengeType)) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var showGiveUpConfirm by remember { mutableStateOf(false) }
    var resolved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startTimeout(alarm.timeoutSeconds) {
            if (!resolved) {
                resolved = true
                onResolved("failed")
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "ALARM: ${alarm.label.ifBlank { "Wake up" }}",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "$${"%.2f".format(alarm.stake)} is staked on this alarm",
                color = Color(0xFFFF6B6B)
            )
            Spacer(Modifier.height(24.dp))
            Text("Round $round of ${alarm.rounds}", color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            Text(challenge.prompt(), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Your answer") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            if (feedback.isNotEmpty()) {
                Text(feedback, color = Color(0xFFFFD93D))
                Spacer(Modifier.height(8.dp))
            }
            Button(onClick = {
                if (challenge.check(answer)) {
                    if (round >= alarm.rounds) {
                        resolved = true
                        onResolved("solved")
                    } else {
                        feedback = "Correct! Next one..."
                        round += 1
                        challenge = createChallenge(alarm.challengeType)
                        answer = ""
                    }
                } else {
                    feedback = "Not quite — try again."
                    answer = ""
                }
            }) { Text("Submit") }
            Spacer(Modifier.height(32.dp))
            TextButton(onClick = { showGiveUpConfirm = true }) {
                Text("Give up (lose stake)", color = Color(0xFFFF6B6B))
            }
        }
    }

    if (showGiveUpConfirm) {
        AlertDialog(
            onDismissRequest = { showGiveUpConfirm = false },
            title = { Text("Give up?") },
            text = { Text("Give up and lose $${"%.2f".format(alarm.stake)}?") },
            confirmButton = {
                TextButton(onClick = {
                    showGiveUpConfirm = false
                    resolved = true
                    onResolved("gave_up")
                }) { Text("Give up") }
            },
            dismissButton = {
                TextButton(onClick = { showGiveUpConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
