package com.example.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiModelInfo
import com.example.model.ChatMessage
import com.example.model.MessageRole
import com.example.model.ModelCatalog
import com.example.ui.components.MarkdownRenderer
import com.example.ui.components.ModelSelectionDialog
import com.example.ui.components.TactileIconButton
import com.example.ui.theme.HardwareScrew
import com.example.ui.theme.SkeuoColors
import com.example.ui.theme.StatusLed
import com.example.ui.theme.tactileButton
import com.example.ui.theme.tactileRaised
import com.example.ui.theme.tactileRecessed

@Composable
fun ChatMessagesView(
    messages: List<ChatMessage>,
    isStreaming: Boolean,
    fontSize: Int,
    onRegenerate: () -> Unit,
    onRetryWithModel: (AiModelInfo) -> Unit,
    onEditMessage: (messageId: String, newContent: String) -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isStreaming) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (messages.isEmpty()) {
        EmptyChatPlaceholder(modifier = modifier)
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                if (message.role == MessageRole.USER) {
                    UserMessageBubble(
                        message = message,
                        fontSize = fontSize,
                        onEdit = { newText -> onEditMessage(message.id, newText) },
                        onDelete = { onDeleteMessage(message.id) }
                    )
                } else {
                    AssistantMessageCard(
                        message = message,
                        fontSize = fontSize,
                        isStreamingThis = isStreaming && message == messages.lastOrNull(),
                        onRegenerate = onRegenerate,
                        onRetryWithModel = onRetryWithModel,
                        onDelete = { onDeleteMessage(message.id) }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun UserMessageBubble(
    message: ChatMessage,
    fontSize: Int,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(message.content) { mutableStateOf(message.content) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(10.dp))
                .tactileRecessed(RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusLed(color = SkeuoColors.LedCyan, size = 6.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OPERATOR",
                        color = SkeuoColors.LedCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row {
                    TactileIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit query",
                        size = 24.dp,
                        onClick = { isEditing = true }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TactileIconButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete query",
                        size = 24.dp,
                        tint = SkeuoColors.LedRed.copy(alpha = 0.8f),
                        onClick = onDelete
                    )
                }
            }

            // Attached image if present
            if (!message.imageBase64.isNullOrBlank()) {
                val bitmap = remember(message.imageBase64) {
                    try {
                        val decodedBytes = Base64.decode(message.imageBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Vision Input Payload",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, SkeuoColors.ChassisPlateBorder, RoundedCornerShape(6.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.content,
                color = SkeuoColors.TextHighContrast,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.4).sp
            )
        }
    }

    if (isEditing) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            title = { Text("Edit Query", color = SkeuoColors.TextHighContrast) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEdit(editText)
                    isEditing = false
                }) {
                    Text("Save & Update", color = SkeuoColors.LedCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel", color = SkeuoColors.TextMuted)
                }
            }
        )
    }
}

@Composable
fun AssistantMessageCard(
    message: ChatMessage,
    fontSize: Int,
    isStreamingThis: Boolean,
    onRegenerate: () -> Unit,
    onRetryWithModel: (AiModelInfo) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isThinkingExpanded by remember { mutableStateOf(false) }
    var showRetryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .tactileRaised(RoundedCornerShape(10.dp), elevation = 4.dp)
            .border(1.dp, SkeuoColors.ChassisPlateBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        // Hardware Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusLed(
                    color = Color(message.provider.accentColorHex),
                    size = 7.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.provider.displayName.uppercase(),
                            color = Color(message.provider.accentColorHex),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.modelId,
                            color = SkeuoColors.TextHighContrast,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (message.latencyMs > 0 || message.tokensCount > 0) {
                        Text(
                            text = "${message.latencyMs}ms • ${message.tokensCount} tok",
                            color = SkeuoColors.TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Quick Actions: Copy, Retry with another model, Regenerate, Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                TactileIconButton(
                    icon = Icons.Default.ContentCopy,
                    contentDescription = "Copy response",
                    size = 26.dp,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AI response", message.content))
                        Toast.makeText(context, "Response copied", Toast.LENGTH_SHORT).show()
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                TactileIconButton(
                    icon = Icons.Default.SwapHoriz,
                    contentDescription = "Retry with another model",
                    size = 26.dp,
                    onClick = { showRetryDialog = true }
                )
                Spacer(modifier = Modifier.width(4.dp))
                TactileIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Regenerate",
                    size = 26.dp,
                    onClick = onRegenerate
                )
                Spacer(modifier = Modifier.width(4.dp))
                TactileIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete message",
                    size = 26.dp,
                    tint = SkeuoColors.LedRed.copy(alpha = 0.8f),
                    onClick = onDelete
                )
            }
        }

        // Thinking accordion if present
        if (!message.thinkingContent.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .tactileRecessed(RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isThinkingExpanded = !isThinkingExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Thinking process",
                            tint = SkeuoColors.LedPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REASONING CHAIN",
                            color = SkeuoColors.LedPurple,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Icon(
                        imageVector = if (isThinkingExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle reasoning",
                        tint = SkeuoColors.TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = isThinkingExpanded) {
                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        Text(
                            text = message.thinkingContent,
                            color = SkeuoColors.TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message Content Body
        MarkdownRenderer(
            content = message.content,
            fontSize = fontSize
        )

        // Streaming terminal cursor
        if (isStreamingThis) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusLed(
                    color = Color(message.provider.accentColorHex),
                    size = 6.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "█ STREAMING RESPONSE...",
                    color = Color(message.provider.accentColorHex),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showRetryDialog) {
        ModelSelectionDialog(
            currentModelId = message.modelId,
            onDismiss = { showRetryDialog = false },
            onSelect = { selectedModel ->
                showRetryDialog = false
                onRetryWithModel(selectedModel)
            }
        )
    }
}

@Composable
fun EmptyChatPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .tactileRaised(RoundedCornerShape(12.dp))
                .border(1.dp, SkeuoColors.ChassisPlateBorder, RoundedCornerShape(12.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HardwareScrew(size = 8.dp)
                Spacer(modifier = Modifier.width(12.dp))
                StatusLed(color = SkeuoColors.LedCyan, size = 8.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NEXUS AI CONSOLE READY",
                    color = SkeuoColors.TextHighContrast,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                HardwareScrew(size = 8.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Multi-provider neural bus online.\nSelect any model and transmit your instruction below.",
                color = SkeuoColors.TextMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
