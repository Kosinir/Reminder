package com.example.remainder.data.notes

import kotlinx.coroutines.flow.Flow

class NotesRepository(private val noteDao: NoteDao) {
    val notes : Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun addNote(content: String) {
        noteDao.insert(NoteEntity(content = content))
    }

    suspend fun updateNote(id: Int, content: String) {
        noteDao.update(NoteEntity(id = id, content = content))
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.delete(note)
    }

}