package com.task.noteapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.task.noteapp.databinding.ActivityNoteBinding
import com.task.noteapp.db.NoteDatabase
import com.task.noteapp.repository.NoteRepository
import com.task.noteapp.utils.shortToast
import com.task.noteapp.viewmodel.NoteActivityViewModel
import com.task.noteapp.viewmodel.NoteActivityViewModelFactory

class NoteActivity : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var noteActivityBinding: ActivityNoteBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteActivityBinding=ActivityNoteBinding.inflate(layoutInflater)

        try {
            setContentView(noteActivityBinding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteViewModelProviderFactory = NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(
                this,
                noteViewModelProviderFactory
            )[NoteActivityViewModel::class.java]
        } catch (e: Exception) {
            shortToast("error occurred")
        }
    }
}