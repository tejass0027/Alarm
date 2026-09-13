package com.stakealarm.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.stakealarm.app.data.Repository
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return
        val repository = Repository(context)
        val alarms = repository.loadAlarms()
        val alarm = alarms.find { it.id == alarmId } ?: return
        if (!alarm.enabled) return

        val updated = alarm.copy(
            lastTriggeredDate = LocalDate.now().toString(),
            enabled = alarm.repeatDays.isNotEmpty()
        )
        repository.saveAlarms(alarms.map { if (it.id == updated.id) updated else it })

        if (updated.enabled) {
            AlarmScheduler(context).schedule(updated)
        }

        val serviceIntent = Intent(context, AlarmRingService::class.java)
            .putExtra(EXTRA_ALARM_ID, alarmId)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
    }
}
