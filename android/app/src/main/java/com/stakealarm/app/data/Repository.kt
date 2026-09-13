package com.stakealarm.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Repository(context: Context) {
    private val dir = context.applicationContext.filesDir
    private val alarmsFile = File(dir, "alarms.json")
    private val balanceFile = File(dir, "balance.json")
    private val historyFile = File(dir, "history.json")

    fun loadAlarms(): List<Alarm> {
        if (!alarmsFile.exists()) return emptyList()
        val arr = JSONArray(alarmsFile.readText())
        return (0 until arr.length()).map { Alarm.fromJson(arr.getJSONObject(it)) }
    }

    fun saveAlarms(alarms: List<Alarm>) {
        val arr = JSONArray()
        alarms.forEach { arr.put(it.toJson()) }
        alarmsFile.writeText(arr.toString(2))
    }

    fun getBalance(): Double {
        if (!balanceFile.exists()) return 0.0
        return JSONObject(balanceFile.readText()).optDouble("balance", 0.0)
    }

    fun saveBalance(balance: Double) {
        balanceFile.writeText(JSONObject().put("balance", balance).toString(2))
    }

    fun loadHistory(): List<HistoryEntry> {
        if (!historyFile.exists()) return emptyList()
        val arr = JSONArray(historyFile.readText())
        return (0 until arr.length()).map { HistoryEntry.fromJson(arr.getJSONObject(it)) }
    }

    fun appendHistory(entry: HistoryEntry) {
        val history = loadHistory() + entry
        val arr = JSONArray()
        history.forEach { arr.put(it.toJson()) }
        historyFile.writeText(arr.toString(2))
    }
}
