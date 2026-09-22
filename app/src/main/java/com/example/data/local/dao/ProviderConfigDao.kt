package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ProviderConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderConfigDao {
    @Query("SELECT * FROM provider_configs")
    fun getAllConfigs(): Flow<List<ProviderConfigEntity>>

    @Query("SELECT * FROM provider_configs WHERE providerId = :providerId LIMIT 1")
    suspend fun getConfig(providerId: String): ProviderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: ProviderConfigEntity)

    @Update
    suspend fun update(config: ProviderConfigEntity)

    @Query("UPDATE provider_configs SET isEnabled = :isEnabled WHERE providerId = :providerId")
    suspend fun setEnabled(providerId: String, isEnabled: Boolean)

    @Query("UPDATE provider_configs SET lastTestedStatus = :status, lastTestedLatencyMs = :latencyMs WHERE providerId = :providerId")
    suspend fun updateTestStatus(providerId: String, status: String, latencyMs: Long)

    @Query("DELETE FROM provider_configs WHERE providerId = :providerId")
    suspend fun delete(providerId: String)
}
