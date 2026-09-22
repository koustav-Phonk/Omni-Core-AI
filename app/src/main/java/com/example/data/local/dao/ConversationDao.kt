package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE folderName = :folder ORDER BY updatedAt DESC")
    fun getConversationsByFolder(folder: String): Flow<List<ConversationEntity>>

    @Query("SELECT DISTINCT folderName FROM conversations WHERE folderName IS NOT NULL AND folderName != ''")
    fun getAllFolders(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("UPDATE conversations SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTitle(id: String, title: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET isPinned = NOT isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun togglePin(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET isFavorite = NOT isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleFavorite(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET folderName = :folderName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFolder(id: String, folderName: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}
