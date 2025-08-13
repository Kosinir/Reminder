package com.example.remainder.notegrep

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.remainder.data.notes.NoteEntity
import com.example.remainder.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotepadScreen(
    navController: NavController,
    notesViewModel: NotesViewModel,
    onDeleteModeChanged: (Boolean) -> Unit,
    onNoteSelected: (Int) -> Unit
) {
    val notes by notesViewModel.notes.collectAsState(initial = emptyList())
    var toDelete by remember { mutableStateOf<NoteEntity?>(null) }

    LaunchedEffect(toDelete) {
        onDeleteModeChanged(toDelete != null)
    }

    Scaffold { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            if (notes.isEmpty()) {
                Text("No notes yet.", Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(notes) { note ->
                        // Sprawdzamy, czy to jest zaznaczona notatka
                        val isSelected = note == toDelete

                        Card(
                            // Jeśli wybrana, zmieniamy kolory tła i ewentualnie dodajemy border
                            colors = if (isSelected) {
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 1.0f)
                                )
                            } else {
                                CardDefaults.cardColors()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .combinedClickable(
                                    onClick = {
                                        onNoteSelected(note.id)
                                    },
                                    onLongClick = {
                                        toDelete = note
                                    }
                                )
                        ) {
                            Text(
                                text = note.content,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val formattedDate = remember(note.createdAt) {
                                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                    .format(note.createdAt)
                            }
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            toDelete?.let { note ->
                Surface(
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Do you want to delete this note?",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TextButton(onClick = {
                                notesViewModel.deleteNote(note)
                                toDelete = null
                            }) {
                                Text("Delete")
                            }
                            TextButton(onClick = { toDelete = null }) {
                                Text("Cancel")
                            }
                        }
                    }
                }

            }
        }
    }
}

