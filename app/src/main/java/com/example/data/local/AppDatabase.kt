package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppSettingDao
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.ProviderConfigDao
import com.example.data.local.entities.AppSettingEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.ProviderConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ProviderConfigEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun providerConfigDao(): ProviderConfigDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexus_ai_workstation.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial provider configs and a welcome conversation
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            seedInitialData(database)
                        }
                    }
                })
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val providers = listOf(
                ProviderConfigEntity("gemini", isEnabled = true, defaultModelId = "gemini-3.5-flash"),
                ProviderConfigEntity("openai", isEnabled = true, defaultModelId = "gpt-4o"),
                ProviderConfigEntity("anthropic", isEnabled = true, defaultModelId = "claude-3-5-sonnet-20241022"),
                ProviderConfigEntity("xai", isEnabled = true, defaultModelId = "grok-2-latest"),
                ProviderConfigEntity("deepseek", isEnabled = true, defaultModelId = "deepseek-chat"),
                ProviderConfigEntity("mistral", isEnabled = true, defaultModelId = "mistral-large-latest"),
                ProviderConfigEntity("meta_llama", isEnabled = true, defaultModelId = "meta-llama/llama-3.3-70b-instruct")
            )
            providers.forEach { db.providerConfigDao().insertOrUpdate(it) }

            // Create initial welcome conversation
            val welcomeConvId = "conv-workstation-init"
            val welcomeConv = ConversationEntity(
                id = welcomeConvId,
                title = "Nexus AI Workstation Initialized",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isPinned = true,
                isFavorite = true,
                folderName = "General",
                lastProvider = "gemini",
                lastModelId = "gemini-3.5-flash"
            )
            db.conversationDao().insert(welcomeConv)

            val welcomeMsg = MessageEntity(
                id = "msg-welcome-sys",
                conversationId = welcomeConvId,
                role = "assistant",
                content = """### Welcome to Nexus AI Workstation

All primary AI provider adapters have been mounted to the bus:
- **Google Gemini** (Gemini 3.5 Flash, 3.1 Pro Thinking, 3.1 Flash-Lite)
- **OpenAI** (GPT-4o, GPT-4o mini, o3-mini)
- **Anthropic Claude** (Claude 3.5 Sonnet, 3.5 Haiku)
- **xAI Grok** (Grok 2, Grok Beta)
- **DeepSeek** (DeepSeek V3, DeepSeek R1)
- **Mistral** (Mistral Large, Codestral)
- **Meta Llama** (Llama 3.3 70B, Llama 3.1 405B)

#### Features:
1. **Model Switcher**: Tap the top model selector to hot-swap active model at any turn.
2. **Multi-AI Compare Mode**: Tap the rack toggle to broadcast the same prompt to 2-4 models side-by-side.
3. **API Key Manager**: Store your personal keys securely with masked obfuscation and connection ping tests.
4. **Code Blocks**: Formatted with syntax highlighting and one-tap tactile copy.

Enter your query below to begin.""",
                timestamp = System.currentTimeMillis(),
                provider = "gemini",
                modelId = "gemini-3.5-flash",
                tokensCount = 280,
                latencyMs = 120
            )
            db.messageDao().insert(welcomeMsg)
        }
    }
}
