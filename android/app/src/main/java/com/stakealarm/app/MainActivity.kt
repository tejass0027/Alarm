package com.stakealarm.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.stakealarm.app.alarm.AlarmScheduler
import com.stakealarm.app.data.Alarm
import com.stakealarm.app.data.LocalVirtualProvider
import com.stakealarm.app.data.Repository
import com.stakealarm.app.ui.MainScreen
import com.stakealarm.app.ui.theme.StakeAlarmTheme

class MainActivity : ComponentActivity() {
    private lateinit var repository: Repository
    private lateinit var provider: LocalVirtualProvider
    private lateinit var scheduler: AlarmScheduler

    private var canScheduleExactState by mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = Repository(this)
        provider = LocalVirtualProvider(repository)
        scheduler = AlarmScheduler(this)
        canScheduleExactState = scheduler.canScheduleExact()

        requestNotificationPermissionIfNeeded()

        setContent {
            StakeAlarmTheme {
                var alarms by remember { mutableStateOf(repository.loadAlarms()) }
                var balance by remember { mutableDoubleStateOf(provider.getBalance()) }

                fun persist(updated: List<Alarm>) {
                    alarms = updated
                    repository.saveAlarms(updated)
                }

                MainScreen(
                    alarms = alarms,
                    balance = balance,
                    canScheduleExact = canScheduleExactState,
                    onRequestExactAlarmPermission = { openExactAlarmSettings() },
                    onDeposit = { amount -> balance = provider.deposit(amount) },
                    onAddAlarm = { alarm ->
                        persist(alarms + alarm)
                        scheduler.schedule(alarm)
                    },
                    onUpdateAlarm = { updated ->
                        scheduler.cancel(updated)
                        persist(alarms.map { if (it.id == updated.id) updated else it })
                        if (updated.enabled) scheduler.schedule(updated)
                    },
                    onToggleAlarm = { alarm ->
                        val updated = alarm.copy(enabled = !alarm.enabled)
                        persist(alarms.map { if (it.id == updated.id) updated else it })
                        if (updated.enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
                    },
                    onDeleteAlarm = { alarm ->
                        scheduler.cancel(alarm)
                        persist(alarms.filterNot { it.id == alarm.id })
                    },
                    loadHistory = { repository.loadHistory() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::scheduler.isInitialized) {
            canScheduleExactState = scheduler.canScheduleExact()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }
    }
}
