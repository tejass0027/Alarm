package com.stakealarm.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stakealarm.app.data.Repository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val repository = Repository(context)
        val scheduler = AlarmScheduler(context)
        repository.loadAlarms().filter { it.enabled }.forEach { scheduler.schedule(it) }
    }
}
