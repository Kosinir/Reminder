package com.example.remainder.creattaskgrep

import android.R.attr.navigationIcon
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskInfoStep(
    listTitle: String,
    onListTitleChange: (String) -> Unit,
    tasks: List<String>,
    onTaskChange: (index: Int, value: String) -> Unit,
    onAddTask: () -> Unit,
    onNext: () -> Unit,
    onRemoveTask: (index: Int) -> Unit,
    onBack: () -> Unit
) {
    /*
    LaunchedEffect(Unit) {
        if (tasks.isEmpty()) {
            onAddTask()
        }
    }*/

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Step 1: Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = listTitle,
                    onValueChange = onListTitleChange,
                    label = { Text("List title") },
                    modifier = Modifier.fillMaxWidth()
                )

                tasks.forEachIndexed { index, taskText ->
                    OutlinedTextField(
                        value = taskText,
                        onValueChange = { onTaskChange(index, it) },
                        label = { Text("Task ${index + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { onRemoveTask(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = onAddTask) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onNext,
                    enabled = listTitle.isNotBlank() && tasks.all { it.isNotBlank() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next")
                }
            }
        }
    )
}

