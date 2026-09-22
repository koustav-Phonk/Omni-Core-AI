package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nexus AI", appName)
  }

  @Test
  fun `verify secure storage key masking and encryption`() {
    val rawKey = "sk-proj-123456789abcdefghijklmnopqrstuvwxyz"
    val masked = com.example.data.local.SecureStorage.maskKey(rawKey)
    assert(masked.startsWith("sk-p"))
    assert(masked.endsWith("wxyz"))
    assert(!masked.contains("123456789"))

    val encrypted = com.example.data.local.SecureStorage.encryptKey(rawKey)
    assert(encrypted.isNotEmpty())
    assert(encrypted != rawKey)

    val decrypted = com.example.data.local.SecureStorage.decryptKey(encrypted)
    assertEquals(rawKey, decrypted)
  }

  @Test
  fun `verify model catalog contains required providers`() {
    val geminiModels = com.example.model.ModelCatalog.getModelsForProvider(com.example.model.AiProviderType.GEMINI)
    assert(geminiModels.any { it.id == "gemini-3.5-flash" })
    assert(geminiModels.any { it.id == "gemini-3.1-pro-preview" })

    val openAiModels = com.example.model.ModelCatalog.getModelsForProvider(com.example.model.AiProviderType.OPENAI)
    assert(openAiModels.any { it.id == "gpt-4o" })

    val claudeModels = com.example.model.ModelCatalog.getModelsForProvider(com.example.model.AiProviderType.ANTHROPIC)
    assert(claudeModels.any { it.id.contains("claude-3-5-sonnet") })
  }
}
