package com.example.remainder.creattaskgrep

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
fun TaskRepeatStep(
    isRepeating: Boolean,
    onIsRepeatingChange: (Boolean) -> Unit,
    repeatInterval: RepeatInterval,
    onRepeatIntervalChange: (RepeatInterval) -> Unit,
    repeatEndTimestamp: Long,
    onRepeatEndTimestampChange: (Long) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showEndDatePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = repeatEndTimestamp }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                cal.set(y, m, d)
                onRepeatEndTimestampChange(cal.timeInMillis)
                showEndDatePicker = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Step 3: Repeat") }) },
        content = { inner ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Repeat?")
                    Switch(
                        checked = isRepeating,
                        onCheckedChange = { checked ->
                            onIsRepeatingChange(checked)
                            if (!checked) {
                                onRepeatIntervalChange(RepeatInterval.NONE)
                            }
                            else if (repeatInterval == RepeatInterval.NONE && checked) {
                                onRepeatIntervalChange(RepeatInterval.DAILY)
                            }
                        }
                    )
                }

                if (isRepeating) {
                    Text("Interval", style = MaterialTheme.typography.titleMedium)

                    Column {
                        RepeatInterval.entries
                            .filter { it != RepeatInterval.NONE }
                            .forEach { choice ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onRepeatIntervalChange(choice) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (repeatInterval == choice),
                                        onClick = { onRepeatIntervalChange(choice) }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(choice.name)
                                }
                            }
                    }

                    OutlinedButton(onClick = { showEndDatePicker = true }) {
                        Text(
                            "End Date: ${
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(Date(repeatEndTimestamp))
                            }"
                        )
                    }
                }

                Spacer(Modifier.weight(1f))
                //val canFinish = isRepeating || (repeatEndTimestamp > System.currentTimeMillis())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = onBack) { Text("Back") }
                    Button(onClick = onFinish) { Text("Finish") }
                }
            }
        }
    )
}
