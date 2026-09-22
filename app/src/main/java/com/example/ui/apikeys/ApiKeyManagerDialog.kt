package com.example.ui.apikeys

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SecureStorage
import com.example.data.local.entities.ProviderConfigEntity
import com.example.model.AiProviderType
import com.example.network.KeyValidationResult
import com.example.ui.components.TactileIconButton
import com.example.ui.theme.HardwareScrew
import com.example.ui.theme.SkeuoColors
import com.example.ui.theme.StatusLed
import com.example.ui.theme.tactileButton
import com.example.ui.theme.tactileRaised
import com.example.ui.theme.tactileRecessed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyManagerDialog(
    configs: List<ProviderConfigEntity>,
    onDismiss: () -> Unit,
    onSaveKey: (provider: AiProviderType, key: String, endpoint: String?) -> Unit,
    onToggleEnabled: (provider: AiProviderType, isEnabled: Boolean) -> Unit,
    onDeleteKey: (provider: AiProviderType) -> Unit,
    onTestConnection: suspend (provider: AiProviderType) -> KeyValidationResult
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .tactileRaised(RoundedCornerShape(12.dp), elevation = 8.dp)
                .border(1.dp, SkeuoColors.ChassisPlateBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HardwareScrew(size = 8.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENCRYPTED KEY MANAGER",
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SkeuoColors.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Security reassurance badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0C141C))
                    .border(1.dp, Color(0xFF1E3048), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = SkeuoColors.LedCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Keys are securely masked and stored on your hardware device. Never transmitted to third-party telemetry.",
                    color = SkeuoColors.TextNormal,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            // Providers List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(AiProviderType.entries) { provider ->
                    val config = configs.firstOrNull { it.providerId == provider.id }
                    ProviderKeyCard(
                        provider = provider,
                        config = config,
                        onSaveKey = { key, endpoint -> onSaveKey(provider, key, endpoint) },
                        onToggleEnabled = { enabled -> onToggleEnabled(provider, enabled) },
                        onDeleteKey = { onDeleteKey(provider) },
                        onTestConnection = { onTestConnection(provider) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderKeyCard(
    provider: AiProviderType,
    config: ProviderConfigEntity?,
    onSaveKey: (key: String, endpoint: String?) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDeleteKey: () -> Unit,
    onTestConnection: suspend () -> KeyValidationResult
) {
    var isExpanded by remember { mutableStateOf(false) }
    var keyInput by remember { mutableStateOf("") }
    var endpointInput by remember(config?.customEndpoint) { mutableStateOf(config?.customEndpoint ?: "") }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val decryptedKey = remember(config?.apiKeyEncrypted) {
        SecureStorage.decryptKey(config?.apiKeyEncrypted ?: "")
    }
    val hasKey = decryptedKey.isNotBlank()
    val isEnabled = config?.isEnabled ?: true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .tactileRecessed(RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        // Provider Top Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val ledColor = when {
                    !hasKey && provider != AiProviderType.GEMINI -> SkeuoColors.LedAmber
                    config?.lastTestedStatus == "CONNECTED" -> SkeuoColors.LedGreen
                    config?.lastTestedStatus == "ERROR" -> SkeuoColors.LedRed
                    else -> Color(provider.accentColorHex)
                }
                StatusLed(color = ledColor, size = 8.dp)
                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = provider.displayName,
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasKey) "STATUS: CONFIGURED" else if (provider == AiProviderType.GEMINI) "BUILT-IN / READY" else "MISSING KEY",
                        color = if (hasKey) SkeuoColors.LedGreen else SkeuoColors.TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Test ping button
                Row(
                    modifier = Modifier
                        .tactileButton(
                            shape = RoundedCornerShape(4.dp),
                            onClick = {
                                scope.launch {
                                    isTesting = true
                                    val result = onTestConnection()
                                    isTesting = false
                                    testResult = result.message
                                }
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = "Test connection",
                        tint = if (isTesting) SkeuoColors.LedAmber else SkeuoColors.LedCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTesting) "TESTING..." else "TEST",
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Expand settings
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand configuration",
                    tint = SkeuoColors.TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { isExpanded = !isExpanded }
                )
            }
        }

        // Test result banner
        if (testResult != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = testResult!!,
                color = if (testResult!!.contains("Connected", ignoreCase = true)) SkeuoColors.LedGreen else SkeuoColors.LedRed,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Expanded Configuration Tray
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // Masked or new key entry
                Text(
                    text = "API KEY (${provider.keyPrefixHelp})",
                    color = SkeuoColors.TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F1116))
                        .border(1.dp, Color(0xFF232834), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    BasicTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        textStyle = TextStyle(
                            color = SkeuoColors.TextHighContrast,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(SkeuoColors.LedCyan),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (keyInput.isEmpty()) {
                                Text(
                                    text = if (hasKey) SecureStorage.maskKey(decryptedKey) else "Enter API Key...",
                                    color = if (hasKey) SkeuoColors.LedGreen else SkeuoColors.TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            inner()
                        }
                    )
                }

                // Custom endpoint
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CUSTOM BASE ENDPOINT (OPTIONAL)",
                    color = SkeuoColors.TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F1116))
                        .border(1.dp, Color(0xFF232834), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    BasicTextField(
                        value = endpointInput,
                        onValueChange = { endpointInput = it },
                        textStyle = TextStyle(
                            color = SkeuoColors.TextHighContrast,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(SkeuoColors.LedCyan),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (endpointInput.isEmpty()) {
                                Text(
                                    text = provider.defaultEndpoint,
                                    color = SkeuoColors.TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            inner()
                        }
                    )
                }

                // Action Buttons: Save Key, Remove Key
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (hasKey) {
                        TactileIconButton(
                            icon = Icons.Default.Delete,
                            contentDescription = "Delete key",
                            size = 28.dp,
                            tint = SkeuoColors.LedRed,
                            onClick = onDeleteKey
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row(
                        modifier = Modifier
                            .tactileButton(
                                enabled = keyInput.isNotBlank() || endpointInput != (config?.customEndpoint ?: ""),
                                shape = RoundedCornerShape(4.dp),
                                accentColor = SkeuoColors.LedCyan,
                                onClick = {
                                    val keyToSave = keyInput.ifBlank { decryptedKey }
                                    onSaveKey(keyToSave, endpointInput.ifBlank { null })
                                    keyInput = ""
                                    testResult = "Key saved successfully"
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SAVE CREDENTIAL",
                            color = SkeuoColors.TextHighContrast,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
