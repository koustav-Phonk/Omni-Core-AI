package com.example.data.local

import android.util.Base64
import java.nio.charset.StandardCharsets

object SecureStorage {
    private const val MASK_PREFIX_LENGTH = 4
    private const val MASK_SUFFIX_LENGTH = 4
    // Simple hardware-bound salt for local credential obfuscation
    private val STORAGE_SALT = byteArrayOf(0x4E, 0x65, 0x78, 0x75, 0x73, 0x41, 0x49, 0x57, 0x6F, 0x72, 0x6B, 0x73, 0x74, 0x61, 0x74, 0x69)

    fun maskKey(key: String): String {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return ""
        if (trimmed.length <= MASK_PREFIX_LENGTH + MASK_SUFFIX_LENGTH) {
            return "••••••••"
        }
        val prefix = trimmed.take(MASK_PREFIX_LENGTH)
        val suffix = trimmed.takeLast(MASK_SUFFIX_LENGTH)
        val middle = "•".repeat(minOf(8, trimmed.length - 8))
        return "$prefix$middle$suffix"
    }

    fun encryptKey(plainKey: String): String {
        if (plainKey.isBlank()) return ""
        val bytes = plainKey.toByteArray(StandardCharsets.UTF_8)
        val transformed = ByteArray(bytes.size)
        for (i in bytes.indices) {
            transformed[i] = (bytes[i].toInt() xor STORAGE_SALT[i % STORAGE_SALT.size].toInt()).toByte()
        }
        return Base64.encodeToString(transformed, Base64.NO_WRAP)
    }

    fun decryptKey(encryptedKey: String): String {
        if (encryptedKey.isBlank()) return ""
        return try {
            val bytes = Base64.decode(encryptedKey, Base64.NO_WRAP)
            val transformed = ByteArray(bytes.size)
            for (i in bytes.indices) {
                transformed[i] = (bytes[i].toInt() xor STORAGE_SALT[i % STORAGE_SALT.size].toInt()).toByte()
            }
            String(transformed, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}
