package com.example.remainder.edittaskgrep

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.remainder.data.tasks.RepeatInterval
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDateStep(
    dueTimestamp: Long,
    onDateChange: (Long) -> Unit,
    onTimeChange: (Long) -> Unit,
    isRepeating: Boolean,
    onIsRepeatingChange: (Boolean) -> Unit,
    repeatInterval: RepeatInterval,
    onRepeatIntervalChange: (RepeatInterval) -> Unit,
    repeatEndTimestamp: Long?,
    onRepeatEndTimestampChange: (Long?) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val selectedCal = remember { Calendar.getInstance().apply { timeInMillis = dueTimestamp } }
    val endCal = remember { Calendar.getInstance().apply { repeatEndTimestamp?.let { timeInMillis = it } } }

    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showEndDateDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    if (showDateDialog) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                selectedCal.set(Calendar.YEAR, y)
                selectedCal.set(Calendar.MONTH, m)
                selectedCal.set(Calendar.DAY_OF_MONTH, d)
                onDateChange(selectedCal.timeInMillis)
                showDateDialog = false
            },
            selectedCal.get(Calendar.YEAR),
            selectedCal.get(Calendar.MONTH),
            selectedCal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            setOnCancelListener { showDateDialog = false }
        }.show()
    }

    if (showTimeDialog) {
        TimePickerDialog(
            context,
            { _, h, min ->
                selectedCal.set(Calendar.HOUR_OF_DAY, h)
                selectedCal.set(Calendar.MINUTE, min)
                onTimeChange(selectedCal.timeInMillis)
                showTimeDialog = false
            },
            selectedCal.get(Calendar.HOUR_OF_DAY),
            selectedCal.get(Calendar.MINUTE),
            true
        ).apply {
            setOnCancelListener { showTimeDialog = false }
        }
            .show()
    }

    if (showEndDateDialog) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                endCal.set(y, m, d)
                onRepeatEndTimestampChange(endCal.timeInMillis)
                showEndDateDialog = false
            },
            endCal.get(Calendar.YEAR),
            endCal.get(Calendar.MONTH),
            endCal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            setOnCancelListener { showEndDateDialog = false }
        }.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edyt: Date and Hours",
                        maxLines = 2,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            Button(
                onClick = { showDateDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Data: ${dateFormat.format(Date(selectedCal.timeInMillis))}")
            }

            Button(
                onClick = { showTimeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Godzina: ${timeFormat.format(Date(selectedCal.timeInMillis))}")
            }

            Divider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isRepeating,
                    onCheckedChange = { checked ->
                        onIsRepeatingChange(checked)
                        if (!checked) {
                            onRepeatIntervalChange(RepeatInterval.NONE)
                            onRepeatEndTimestampChange(null)
                        } else if (repeatInterval == RepeatInterval.NONE) {
                            onRepeatIntervalChange(RepeatInterval.DAILY)
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text("Powtarzaj")
            }

            if (isRepeating) {
                Text("Częstotliwość", style = MaterialTheme.typography.titleMedium)

                RepeatInterval.entries
                    .filter { it != RepeatInterval.NONE }
                    .forEach { interval ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRepeatIntervalChange(interval) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (interval == repeatInterval),
                                onClick = { onRepeatIntervalChange(interval) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(interval.name)
                        }
                    }

                OutlinedButton(
                    onClick = { showEndDateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zakończ: ${repeatEndTimestamp?.let { dateFormat.format(Date(it)) } ?: "brak"}")
                }
            }

          //  Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onBack) {
                    Text("Wstecz")
                }
                Button(
                    onClick = onSave,
                    enabled = !isRepeating || (repeatInterval != RepeatInterval.NONE && repeatEndTimestamp != null)
                ) {
                    Text("Zapisz")
                }
            }
        }
    }
}
