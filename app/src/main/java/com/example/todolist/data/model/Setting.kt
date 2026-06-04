package com.example.todolist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings_table")
data class Setting(
    @PrimaryKey val key: String,
    val value: String
)