package com.example.remainder.notegrep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.imePadding
import androidx.navigation.NavController
import com.example.remainder.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    navController: NavController,
    notesViewModel: NotesViewModel,
    id: Int,
    showTopBar: Boolean = true
) {
    val notes by notesViewModel.notes.collectAsState(initial = emptyList())
    val note = notes.firstOrNull { it.id == id }
    var text by remember { mutableStateOf("") }

    LaunchedEffect(note) {
        text = note?.content.orEmpty()
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val currentDate = remember {
        val formatter = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
        formatter.format(java.util.Date())
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("Notepad") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                notesViewModel.updateNote(id, text)
                                navController.popBackStack()
                            },
                            enabled = text.isNotBlank()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Box {
                    if (text.isEmpty()) {
                        Text(
                            "Write your note here...",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = TextStyle.Default.copy(color = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 300.dp), // miejsca do pisania
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        })
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(300.dp)) // zapas pod klawiaturę
            }
        }
    }
}
