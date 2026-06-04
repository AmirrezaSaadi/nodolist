package com.example.todolist.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.todolist.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Upsert
    suspend fun upsertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM note_table WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    // Update the query to sort by isPinned first
    @Query("SELECT * FROM note_table ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>
}