package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provider_configs")
data class ProviderConfigEntity(
    @PrimaryKey
    val providerId: String,
    val apiKeyEncrypted: String = "",
    val isEnabled: Boolean = true,
    val customEndpoint: String? = null,
    val defaultModelId: String? = null,
    val lastTestedStatus: String? = "UNTESTED", // "CONNECTED", "ERROR", "UNTESTED"
    val lastTestedLatencyMs: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
