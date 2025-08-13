package com.example.remainder.creattaskgrep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.remainder.data.tasks.RepeatInterval
import java.util.*
import android.app.DatePickerDialog
import java.text.SimpleDateFormat

@Composable
fun TimeInput(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    fun String.toClampedInt(min: Int, max: Int) =
        this.toIntOrNull()?.coerceIn(min, max) ?: 0

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = "%02d".format(hour),
            onValueChange = {
                val h = it.filter { c -> c.isDigit() }.take(2).toClampedInt(0, 23)
                onTimeChange(h, minute)
            },
            label = { Text("Godz.") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(":", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = "%02d".format(minute),
            onValueChange = {
                val m = it.filter { c -> c.isDigit() }.take(2).toClampedInt(0, 59)
                onTimeChange(hour, m)
            },
            label = { Text("Min") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp)
        )
    }
}
