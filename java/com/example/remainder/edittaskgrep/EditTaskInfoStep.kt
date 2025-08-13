package com.example.remainder.edittaskgrep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskInfoStep(
    listTitle: String,
    onListTitleChange: (String) -> Unit,
    tasksList: List<String>,
    tasksDone: List<Boolean>,
    onTaskChange: (index: Int, value: String) -> Unit,
    onTaskDoneChange: (index: Int, value: Boolean) -> Unit,
    onAddTask: () -> Unit,
    onRemoveTask: (index: Int) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task List") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            )
        },
        content = { inner ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = listTitle,
                    onValueChange = onListTitleChange,
                    label = { Text("List Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(tasksList) { index, taskText ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = tasksDone.getOrNull(index) == true,
                                onCheckedChange = { checked ->
                                    onTaskDoneChange(index, checked)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = taskText,
                                onValueChange = { onTaskChange(index, it) },
                                label = { Text("Task ${index + 1}") },
                                textStyle = if (tasksDone.getOrNull(index) == true)
                                    LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough)
                                else
                                    LocalTextStyle.current,
                                modifier = Modifier.weight(1f),
                                trailingIcon = {
                                    IconButton(onClick = { onRemoveTask(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove Task")
                                    }
                                }
                            )
                        }
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(onClick = onAddTask) {
                                Icon(Icons.Default.Add, contentDescription = "Add Task")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onNext,
                    enabled = listTitle.isNotBlank() && tasksList.all { it.isNotBlank() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next")
                }
            }
        }
    )
}
