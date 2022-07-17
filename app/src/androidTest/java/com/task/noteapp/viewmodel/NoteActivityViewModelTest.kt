package com.task.noteapp.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.task.noteapp.db.NoteDatabase
import com.task.noteapp.repository.NoteRepository
import junit.framework.TestCase
import com.task.noteapp.common.getOrAwaitValue
import com.task.noteapp.model.Note
import org.junit.*

import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class NoteActivityViewModelTest : TestCase(){

    private lateinit var noteDatabase: NoteDatabase
    private lateinit var viewModel: NoteActivityViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    public override fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        noteDatabase = Room.inMemoryDatabaseBuilder(
            context, NoteDatabase::class.java
        ).allowMainThreadQueries().build()

        val dataSource = NoteRepository(noteDatabase)
        viewModel = NoteActivityViewModel(dataSource)
    }

    @After
    @Throws(IOException::class)
    public override fun tearDown() {
        noteDatabase.close()
    }

    @Test
    fun testSaveNote() {
        val newNote= Note(
            10,
            title = "hello",
            content = "test content",
            date="25/04/2022",
            color=-1,
            imageUrl = "https://source.unsplash.com/random",
            edited = false
        )
        viewModel.saveNote(newNote)
        val result = viewModel.getAllNotes().getOrAwaitValue().find {
            it.title == "hello" && it.date == "25/04/2022"
        }

        Assert.assertNotNull(result)

    }


    @Test
    fun testDeleteNote(){
        val note= Note(
            1,
            title = "hello",
            content = "test content",
            date="25/04/2022",
            color=-1,
            imageUrl = "https://source.unsplash.com/random",
            edited = false
        )
        viewModel.saveNote(note)
        val count = viewModel.getAllNotes().getOrAwaitValue().count()

        assertEquals(1,count)

        viewModel.deleteNote(note)

        val result = viewModel.getAllNotes().getOrAwaitValue().count()

        assertEquals(0,result)

    }

    @Test
    fun testSearchNoteMatchingResult(){
        val note1= Note(
            2,
            title = "note one",
            content = "test content one",
            date="24/04/2022",
            color=-1,
            imageUrl = "https://source.unsplash.com/random",
            edited = false
        )

        viewModel.saveNote(note1)

        val result2 = viewModel.searchNote("24/04/2022").getOrAwaitValue().count()
        assertEquals(1,result2)



    }


    @Test
    fun testSearchNote_NoMatchingResult(){
        val note1= Note(
            2,
            title = "note one",
            content = "test content one",
            date="25/04/2022",
            color=-1,
            imageUrl = "https://source.unsplash.com/random",
            edited = false
        )

        viewModel.saveNote(note1)

        val result = viewModel.searchNote("three").getOrAwaitValue().count()
        assertEquals(0,result)
    }

}