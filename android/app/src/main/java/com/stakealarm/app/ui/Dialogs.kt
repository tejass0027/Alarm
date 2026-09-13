package com.stakealarm.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stakealarm.app.data.Alarm
import com.stakealarm.app.data.HistoryEntry

private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorDialog(existing: Alarm?, onDismiss: () -> Unit, onSave: (Alarm) -> Unit) {
    var hour by remember { mutableStateOf((existing?.hour ?: 7).toString()) }
    var minute by remember { mutableStateOf((existing?.minute ?: 0).toString()) }
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var stake by remember { mutableStateOf((existing?.stake ?: 5.0).toString()) }
    var rounds by remember { mutableStateOf((existing?.rounds ?: 2).toString()) }
    var timeout by remember { mutableStateOf((existing?.timeoutSeconds ?: 120).toString()) }
    var challengeType by remember { mutableStateOf(existing?.challengeType ?: "random") }
    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(existing?.repeatDays ?: emptyList()) } }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New alarm" else "Edit alarm") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hour, onValueChange = { hour = it }, label = { Text("Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minute, onValueChange = { minute = it }, label = { Text("Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = label, onValueChange = { label = it }, label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = stake, onValueChange = { stake = it }, label = { Text("Stake ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text("Repeat on")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayLabels.forEachIndexed { index, day ->
                        FilterChip(
                            selected = selectedDays.contains(index),
                            onClick = {
                                if (selectedDays.contains(index)) selectedDays.remove(index) else selectedDays.add(index)
                            },
                            label = { Text(day) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Challenge")
                Row {
                    listOf("random" to "Random", "math" to "Math", "typing" to "Typing").forEach { (value, text) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = challengeType == value, onClick = { challengeType = value })
                            Text(text)
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = rounds, onValueChange = { rounds = it }, label = { Text("Rounds to solve") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = timeout, onValueChange = { timeout = it }, label = { Text("Timeout before auto-fail (sec)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val h = hour.toIntOrNull()
                val m = minute.toIntOrNull()
                val s = stake.toDoubleOrNull()
                val r = rounds.toIntOrNull()
                val t = timeout.toIntOrNull()
                when {
                    h == null || h !in 0..23 || m == null || m !in 0..59 -> error = "Enter a valid time."
                    s == null || s <= 0 -> error = "Stake must be greater than 0."
                    r == null || r <= 0 || t == null || t <= 0 -> error = "Enter valid rounds and timeout."
                    else -> onSave(
                        (existing ?: Alarm(hour = h, minute = m, stake = s)).copy(
                            hour = h, minute = m, stake = s, label = label,
                            repeatDays = selectedDays.toList(), challengeType = challengeType,
                            rounds = r, timeoutSeconds = t, enabled = true
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DepositDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deposit") },
        text = {
            Column {
                Text("Amount to add to your virtual balance ($):")
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                if (error.isNotEmpty()) Text(error, color = Color.Red)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.toDoubleOrNull()
                if (value == null || value <= 0) error = "Enter a positive amount." else onConfirm(value)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun HistoryDialog(history: List<HistoryEntry>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alarm history") },
        text = {
            if (history.isEmpty()) {
                Text("No alarms have fired yet.")
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    history.sortedByDescending { it.timestamp }.forEach { entry ->
                        Text("${entry.timestamp.take(19).replace('T', ' ')} — ${entry.label.ifBlank { "(unlabeled)" }}")
                        Text("  ${entry.result} · stake $${"%.2f".format(entry.stake)} · balance $${"%.2f".format(entry.balanceAfter)}")
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
