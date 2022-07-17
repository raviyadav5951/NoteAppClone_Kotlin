package com.task.noteapp.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.noteapp.model.Note
import com.task.noteapp.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteActivityViewModel(private val repositoryObject: NoteRepository) : ViewModel() {

    fun saveNote(newNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repositoryObject.addNote(newNote)
    }

    fun updateNote(existingNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repositoryObject.updateNote(existingNote)
    }

    fun deleteNote(existingNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repositoryObject.deleteNote(existingNote)
    }

    fun searchNote(query: String): LiveData<List<Note>> {
        return repositoryObject.searchNote(query)
    }

    fun getAllNotes(): LiveData<List<Note>> = repositoryObject.getNote()

}