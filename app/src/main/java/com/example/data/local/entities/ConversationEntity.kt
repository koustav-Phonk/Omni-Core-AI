package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val folderName: String? = null,
    val lastProvider: String = "gemini",
    val lastModelId: String = "gemini-3.5-flash"
)
