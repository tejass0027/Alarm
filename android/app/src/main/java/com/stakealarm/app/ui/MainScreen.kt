package com.stakealarm.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stakealarm.app.data.Alarm
import com.stakealarm.app.data.HistoryEntry

private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    alarms: List<Alarm>,
    balance: Double,
    canScheduleExact: Boolean,
    onRequestExactAlarmPermission: () -> Unit,
    onDeposit: (Double) -> Unit,
    onAddAlarm: (Alarm) -> Unit,
    onUpdateAlarm: (Alarm) -> Unit,
    onToggleAlarm: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    loadHistory: () -> List<HistoryEntry>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stake Alarm") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add alarm")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Balance: $${"%.2f".format(balance)}", style = MaterialTheme.typography.headlineSmall)
                Button(onClick = { showDepositDialog = true }) { Text("Deposit") }
            }

            if (!canScheduleExact) {
                Spacer(Modifier.height(8.dp))
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text("Exact alarms aren't allowed yet — alarms may be delayed.")
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = onRequestExactAlarmPermission) { Text("Grant permission") }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { showHistory = true }) { Text("View history") }
            Spacer(Modifier.height(8.dp))

            if (alarms.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No alarms yet. Tap + to add one.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmRow(
                            alarm = alarm,
                            onToggle = { onToggleAlarm(alarm) },
                            onEdit = { editingAlarm = alarm },
                            onDelete = { onDeleteAlarm(alarm) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlarmEditorDialog(
            existing = null,
            onDismiss = { showAddDialog = false },
            onSave = { onAddAlarm(it); showAddDialog = false }
        )
    }

    editingAlarm?.let { alarm ->
        AlarmEditorDialog(
            existing = alarm,
            onDismiss = { editingAlarm = null },
            onSave = { onUpdateAlarm(it); editingAlarm = null }
        )
    }

    if (showDepositDialog) {
        DepositDialog(
            onDismiss = { showDepositDialog = false },
            onConfirm = { onDeposit(it); showDepositDialog = false }
        )
    }

    if (showHistory) {
        HistoryDialog(history = loadHistory(), onDismiss = { showHistory = false })
    }
}

@Composable
private fun AlarmRow(alarm: Alarm, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("%02d:%02d".format(alarm.hour, alarm.minute), style = MaterialTheme.typography.titleLarge)
                Text(alarm.label.ifBlank { "(unlabeled)" }, style = MaterialTheme.typography.bodyMedium)
                val repeat = if (alarm.repeatDays.isEmpty()) "Once"
                else alarm.repeatDays.sorted().joinToString(", ") { dayLabels[it] }
                Text("$repeat · Stake $${"%.2f".format(alarm.stake)}", color = Color.Gray)
            }
            Switch(checked = alarm.enabled, onCheckedChange = { onToggle() })
            TextButton(onClick = onEdit) { Text("Edit") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}
