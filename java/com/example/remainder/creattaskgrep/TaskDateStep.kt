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
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import com.example.remainder.data.tasks.RepeatInterval

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateStep(
    dueTimestamp: Long,
    onDateChange: (Long) -> Unit,
    onTimeChange: (Long) -> Unit,
    isRepeating: Boolean,
    onIsRepeatingChange: (Boolean) -> Unit,
    repeatInterval: RepeatInterval,
    onRepeatIntervalChange: (RepeatInterval) -> Unit,
    repeatEndTimestamp: Long,
    onRepeatEndTimestampChange: (Long) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val selectedCalendar = remember { Calendar.getInstance().apply { timeInMillis = dueTimestamp } }
    val showDateDialog = remember { mutableStateOf(false) }
    val showTimeDialog = remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    if (showDateDialog.value) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                onDateChange(selectedCalendar.timeInMillis)
                showDateDialog.value = false
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            setOnCancelListener { showDateDialog.value = false }
        }.show()
    }

    if (showTimeDialog.value) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedCalendar.set(Calendar.MINUTE, minute)
                onTimeChange(selectedCalendar.timeInMillis)
                showTimeDialog.value = false
            },
            selectedCalendar.get(Calendar.HOUR_OF_DAY),
            selectedCalendar.get(Calendar.MINUTE),
            true
        ).apply{
            setOnCancelListener { showTimeDialog.value = false }
        }
            .show()
    }

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
            setOnCancelListener { showEndDatePicker = false }
        }.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Krok 2: Data, godzina i powtarzanie",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2
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
        ) {
            Text("Wybierz datę zadania:")
            Button(onClick = { showDateDialog.value = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Datę rozpoczęcia: ${dateFormat.format(Date(selectedCalendar.timeInMillis))}")
            }
            Text("Wybierz godzine:")
            Button(onClick = { showTimeDialog.value = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Godzina: ${timeFormat.format(Date(selectedCalendar.timeInMillis))}")
            }

            Divider()

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Powtarzać plan zadań?")
                Switch(
                    checked = isRepeating,
                    onCheckedChange = { checked ->
                        onIsRepeatingChange(checked)
                        if (!checked) onRepeatIntervalChange(RepeatInterval.NONE)
                        else if (repeatInterval == RepeatInterval.NONE) onRepeatIntervalChange(RepeatInterval.DAILY)
                    }
                )
            }

            if (isRepeating) {
                Text("Wybierz częstotliwość powtarzania zadań:", style = MaterialTheme.typography.titleMedium)

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
                Text("Wybierz datę zakończenia powtarzania:")
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zakończ: ${dateFormat.format(Date(repeatEndTimestamp))}")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onBack) { Text("Wstecz") }
                Button(
                    onClick = {
                        val millis = selectedCalendar.timeInMillis
                        onDateChange(millis)
                        onTimeChange(millis)
                        onNext()
                    }
                ) {
                    Text("Dalej")
                }
            }
        }
    }
}
