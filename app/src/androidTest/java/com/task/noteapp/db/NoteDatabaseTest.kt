package com.task.noteapp.db

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.task.noteapp.common.getOrAwaitValue
import com.task.noteapp.model.Note
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class NoteDatabaseTest: TestCase(){

    private lateinit var db: NoteDatabase
    private lateinit var noteDao: DAO


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    public override fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context, NoteDatabase::class.java
        ).build()

        noteDao = db.getNoteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadNote() = runBlocking {
        val newNote= Note(
            10,
            title = "hello",
            content = "test content",
            date="25/04/2022",
            color=-1,
            imageUrl = "https://source.unsplash.com/random",
            edited = false
        )

        noteDao.addNote(newNote)

        val notesList = noteDao.getAllNote().getOrAwaitValue().count()
        assertEquals(1,notesList)
    }
}