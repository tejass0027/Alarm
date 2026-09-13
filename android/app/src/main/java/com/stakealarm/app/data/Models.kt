package com.stakealarm.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val stake: Double,
    val repeatDays: List<Int> = emptyList(), // 0=Mon..6=Sun; empty = one-time
    val label: String = "",
    val enabled: Boolean = true,
    val challengeType: String = "random", // "math" | "typing" | "random"
    val rounds: Int = 2,
    val timeoutSeconds: Int = 120,
    val lastTriggeredDate: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("hour", hour)
        put("minute", minute)
        put("stake", stake)
        put("repeatDays", JSONArray(repeatDays))
        put("label", label)
        put("enabled", enabled)
        put("challengeType", challengeType)
        put("rounds", rounds)
        put("timeoutSeconds", timeoutSeconds)
        put("lastTriggeredDate", lastTriggeredDate)
    }

    companion object {
        fun fromJson(o: JSONObject): Alarm {
            val days = mutableListOf<Int>()
            o.optJSONArray("repeatDays")?.let { arr ->
                for (i in 0 until arr.length()) days.add(arr.getInt(i))
            }
            return Alarm(
                id = o.getString("id"),
                hour = o.getInt("hour"),
                minute = o.getInt("minute"),
                stake = o.getDouble("stake"),
                repeatDays = days,
                label = o.optString("label", ""),
                enabled = o.optBoolean("enabled", true),
                challengeType = o.optString("challengeType", "random"),
                rounds = o.optInt("rounds", 2),
                timeoutSeconds = o.optInt("timeoutSeconds", 120),
                lastTriggeredDate = o.optString("lastTriggeredDate", "")
            )
        }
    }
}

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val alarmId: String,
    val timestamp: String,
    val label: String,
    val stake: Double,
    val result: String, // "solved" | "failed" | "gave_up"
    val balanceAfter: Double
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("alarmId", alarmId)
        put("timestamp", timestamp)
        put("label", label)
        put("stake", stake)
        put("result", result)
        put("balanceAfter", balanceAfter)
    }

    companion object {
        fun fromJson(o: JSONObject) = HistoryEntry(
            id = o.getString("id"),
            alarmId = o.getString("alarmId"),
            timestamp = o.getString("timestamp"),
            label = o.optString("label", ""),
            stake = o.getDouble("stake"),
            result = o.getString("result"),
            balanceAfter = o.getDouble("balanceAfter")
        )
    }
}
