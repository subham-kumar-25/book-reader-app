package com.bookreader.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
    val lastReadAt: Long = System.currentTimeMillis()
)

data class ReadingPreferences(
    val fontSize: Float = 16f,
    val readingTheme: String = "LIGHT", // LIGHT, DARK, SEPIA
    val lineSpacing: Float = 1.6f
)
