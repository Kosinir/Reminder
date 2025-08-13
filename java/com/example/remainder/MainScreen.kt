package com.example.remainder

import android.R.attr.checked
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.remainder.data.tasks.TaskEntity
import com.example.remainder.viewmodel.TasksViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    tasksViewModel: TasksViewModel,
    onDeleteModeChanged: (Boolean) -> Unit
) {
    val oneTime by tasksViewModel.oneTimeTasks.collectAsState()
    val repeating by tasksViewModel.allTasks.collectAsState()
    var toDelete by remember { mutableStateOf<TaskEntity?>(null) }

    LaunchedEffect(toDelete) {
        onDeleteModeChanged(toDelete != null)
    }

    Scaffold { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text("One-time tasks", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                if (oneTime.isEmpty()) {
                    Text("No one-time tasks.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn {
                        items(oneTime) { task ->
                            //val selected = task == toDelete
                            TaskRow(
                                task = task,
                                isSelected = task == toDelete,
                                onClick = { navController.navigate("editTask/${task.id}") },
                                onCheckedChange = { done ->
                                    if(done){
                                        tasksViewModel.markTaskDone(task, true)
                                    } else {
                                        tasksViewModel.updateTask(task.copy(isDone = false))
                                    }
                                },
                                onLongClick = { toDelete = task }
                            )
                        }
                    }
                }

                Divider(Modifier.padding(vertical = 24.dp))
                Text("Repeating tasks", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                val repeatingTasks = repeating.filter { it.isRepeating }
                if (repeatingTasks.isEmpty()) {
                    Text("No repeating tasks yet.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn {
                        items(repeatingTasks) { task ->
                            val selected = task == toDelete
                            TaskRow(
                                task = task,
                                isSelected = selected,
                                onClick = { navController.navigate("editTask/${task.id}") },
                                onCheckedChange = { done ->
                                    if(done) {
                                        tasksViewModel.markTaskDone(task, true)
                                    } else {
                                        tasksViewModel.updateTask(task.copy(isDone = false))
                                    }
                                },
                                onLongClick = { toDelete = task }
                            )
                        }
                    }
                }
            }

            // Panel potwierdzenia usunięcia, wyrównany na dole
            toDelete?.let { task ->
                Surface(
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delete this task?")
                        Row {
                            TextButton(onClick = {
                                tasksViewModel.deleteTask(task)
                                toDelete = null
                            }) {
                                Text("Yes")
                            }
                            TextButton(onClick = { toDelete = null }) {
                                Text("No")
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskRow(
    task: TaskEntity,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = if (isSelected)
        MaterialTheme.colorScheme.primary.copy(alpha = 1.0f)
    else Color.Transparent

    val textColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier
            .background(bgColor)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = onCheckedChange,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = task.listTitle,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (task.isDone)
                        TextDecoration.LineThrough
                        else
                            TextDecoration.None
                )
            )
        }
    }
}



