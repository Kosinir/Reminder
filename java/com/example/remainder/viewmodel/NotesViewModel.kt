package com.example.remainder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remainder.data.notes.NoteEntity
import com.example.remainder.data.notes.NotesRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class NotesViewModel(private val repository: NotesRepository) : ViewModel() {
    val notes = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(text: String) {
        viewModelScope.launch {
            repository.addNote(text)
        }
    }

    fun updateNote(id: Int, content: String) {
        viewModelScope.launch {
            repository.updateNote(id, content)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}