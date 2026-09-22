package com.example.ui.sidebar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.local.entities.ConversationEntity
import com.example.model.AiProviderType
import com.example.ui.components.TactileRockerSwitch
import com.example.ui.theme.HardwareScrew
import com.example.ui.theme.SkeuoColors
import com.example.ui.theme.StatusLed
import com.example.ui.theme.tactileButton
import com.example.ui.theme.tactileRaised
import com.example.ui.theme.tactileRecessed
import com.example.viewmodel.WorkstationMode

@Composable
fun WorkstationSidebar(
    conversations: List<ConversationEntity>,
    currentConversationId: String?,
    currentMode: WorkstationMode,
    onSelectMode: (WorkstationMode) -> Unit,
    onSelectConversation: (String) -> Unit,
    onNewConversation: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRenameConversation: (String, String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onOpenApiKeys: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PINNED, FAVORITE
    var renameTargetConv by remember { mutableStateOf<ConversationEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    val filteredConversations = remember(conversations, searchQuery, selectedFilter) {
        conversations.filter { conv ->
            val matchesSearch = searchQuery.isBlank() || conv.title.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "PINNED" -> conv.isPinned
                "FAVORITE" -> conv.isFavorite
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(SkeuoColors.ChassisPlate)
            .border(1.dp, SkeuoColors.ChassisPlateBorder)
            .padding(12.dp)
    ) {
        // Branding Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HardwareScrew(size = 8.dp)
                Spacer(modifier = Modifier.width(8.dp))
                StatusLed(color = SkeuoColors.LedCyan, size = 8.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NEXUS WORKSTATION",
                    color = SkeuoColors.TextHighContrast,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            HardwareScrew(size = 8.dp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode Switcher Rocker
        TactileRockerSwitch(
            option1 = "Console",
            option2 = "Compare",
            selectedOptionIndex = if (currentMode == WorkstationMode.CHAT_CONSOLE) 0 else 1,
            onOptionSelected = { index ->
                onSelectMode(if (index == 0) WorkstationMode.CHAT_CONSOLE else WorkstationMode.MULTI_AI_COMPARE)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // New Session Tactile Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .tactileButton(
                    shape = RoundedCornerShape(8.dp),
                    accentColor = SkeuoColors.LedCyan,
                    onClick = onNewConversation
                )
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = SkeuoColors.LedCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NEW SESSION",
                color = SkeuoColors.TextHighContrast,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search conversations bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .tactileRecessed(RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = SkeuoColors.TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(SkeuoColors.LedCyan),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search logs...",
                                color = SkeuoColors.TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        inner()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filters Pill Row: All, Pinned, Favorites
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("ALL", "PINNED", "FAVORITE").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Color(0xFF1E2430) else Color.Transparent)
                        .border(
                            0.8.dp,
                            if (isSelected) SkeuoColors.LedCyan.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) SkeuoColors.LedCyan else SkeuoColors.TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Conversations List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredConversations, key = { it.id }) { conv ->
                val isSelected = conv.id == currentConversationId
                var menuExpanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .then(
                            if (isSelected) {
                                Modifier.tactileRecessed(RoundedCornerShape(6.dp))
                            } else {
                                Modifier.tactileButton(
                                    shape = RoundedCornerShape(6.dp),
                                    onClick = { onSelectConversation(conv.id) }
                                )
                            }
                        )
                        .clickable { onSelectConversation(conv.id) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val providerType = AiProviderType.fromId(conv.lastProvider)
                        StatusLed(
                            color = if (isSelected) SkeuoColors.LedCyan else Color(providerType.accentColorHex),
                            size = 6.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (conv.isPinned) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Pinned",
                                        tint = SkeuoColors.LedAmber,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                if (conv.isFavorite) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorite",
                                        tint = SkeuoColors.LedAmber,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = conv.title,
                                    color = if (isSelected) SkeuoColors.TextHighContrast else SkeuoColors.TextNormal,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${conv.lastProvider.uppercase()} • ${conv.lastModelId}",
                                color = SkeuoColors.TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = SkeuoColors.TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(SkeuoColors.ChassisPlate)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (conv.isPinned) "Unpin" else "Pin", color = SkeuoColors.TextNormal) },
                                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = SkeuoColors.LedAmber) },
                                onClick = {
                                    onTogglePin(conv.id)
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (conv.isFavorite) "Remove Favorite" else "Favorite", color = SkeuoColors.TextNormal) },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = SkeuoColors.LedAmber) },
                                onClick = {
                                    onToggleFavorite(conv.id)
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rename", color = SkeuoColors.TextNormal) },
                                leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = SkeuoColors.LedCyan) },
                                onClick = {
                                    renameTargetConv = conv
                                    renameText = conv.title
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = SkeuoColors.LedRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SkeuoColors.LedRed) },
                                onClick = {
                                    onDeleteConversation(conv.id)
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Hardware Rack: API Keys, Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .tactileButton(
                        shape = RoundedCornerShape(6.dp),
                        onClick = onOpenApiKeys
                    )
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = SkeuoColors.LedAmber,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "API KEYS",
                    color = SkeuoColors.TextHighContrast,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .tactileButton(
                        shape = RoundedCornerShape(6.dp),
                        onClick = onOpenSettings
                    )
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = SkeuoColors.LedCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SETTINGS",
                    color = SkeuoColors.TextHighContrast,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (renameTargetConv != null) {
        AlertDialog(
            onDismissRequest = { renameTargetConv = null },
            title = { Text("Rename Session", color = SkeuoColors.TextHighContrast) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = renameTargetConv
                    if (target != null && renameText.isNotBlank()) {
                        onRenameConversation(target.id, renameText.trim())
                    }
                    renameTargetConv = null
                }) {
                    Text("Rename", color = SkeuoColors.LedCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetConv = null }) {
                    Text("Cancel", color = SkeuoColors.TextMuted)
                }
            }
        )
    }
}
