package com.example.notepad

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class Note(
    val title: String,
    val content: String
)

class NoteViewModel : ViewModel() {
    var notes = mutableStateListOf<Note>()
        private set

    fun addNote(title: String, content: String) {
        if (content.isNotBlank() || title.isNotBlank()) {
            notes.add(Note(title.trim(), content.trim()))
        }
    }

    fun updateNote(index: Int, title: String, content: String) {
        if (index in notes.indices) {
            notes[index] = Note(title.trim(), content.trim())
        }
    }
}
