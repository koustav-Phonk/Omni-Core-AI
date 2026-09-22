package com.example.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiModelInfo
import com.example.model.AiProviderType
import com.example.model.ModelCatalog
import com.example.ui.theme.HardwareScrew
import com.example.ui.theme.SkeuoColors
import com.example.ui.theme.StatusLed
import com.example.ui.theme.tactileButton
import com.example.ui.theme.tactileRaised
import com.example.ui.theme.tactileRecessed

@Composable
fun ModelSelectorTrigger(
    currentModel: AiModelInfo,
    onModelSelected: (AiModelInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .tactileButton(
                shape = RoundedCornerShape(8.dp),
                accentColor = Color(currentModel.provider.accentColorHex),
                onClick = { showDialog = true }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusLed(
            color = Color(currentModel.provider.accentColorHex),
            size = 8.dp
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = currentModel.provider.displayName.uppercase(),
                color = SkeuoColors.TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = currentModel.displayName,
                color = SkeuoColors.TextHighContrast,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Capability badges
        if (currentModel.capabilities.hasThinking) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Thinking Mode",
                tint = SkeuoColors.LedPurple,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        if (currentModel.capabilities.hasVision) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Vision Enabled",
                tint = SkeuoColors.LedCyan,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Select Model",
            tint = SkeuoColors.TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }

    if (showDialog) {
        ModelSelectionDialog(
            currentModelId = currentModel.id,
            onDismiss = { showDialog = false },
            onSelect = { selected ->
                onModelSelected(selected)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectionDialog(
    currentModelId: String,
    onDismiss: () -> Unit,
    onSelect: (AiModelInfo) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
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
                        text = "NEURAL MODEL BUS",
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
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

            Text(
                text = "Select an AI provider and active execution model",
                color = SkeuoColors.TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Providers and models list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiProviderType.entries.forEach { provider ->
                    val models = ModelCatalog.getModelsForProvider(provider)
                    if (models.isNotEmpty()) {
                        item(key = "provider_header_${provider.id}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusLed(
                                    color = Color(provider.accentColorHex),
                                    size = 7.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = provider.displayName.uppercase(),
                                    color = Color(provider.accentColorHex),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${provider.company}",
                                    color = SkeuoColors.TextFaint,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        items(models, key = { it.id }) { model ->
                            val isSelected = model.id == currentModelId

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .then(
                                        if (isSelected) {
                                            Modifier.tactileRecessed(RoundedCornerShape(8.dp))
                                        } else {
                                            Modifier.tactileButton(
                                                shape = RoundedCornerShape(8.dp),
                                                onClick = { onSelect(model) }
                                            )
                                        }
                                    )
                                    .clickable { onSelect(model) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = model.displayName,
                                            color = if (isSelected) SkeuoColors.LedCyan else SkeuoColors.TextHighContrast,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "[${model.contextLength}]",
                                            color = SkeuoColors.TextMuted,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = model.description,
                                        color = SkeuoColors.TextMuted,
                                        fontSize = 11.sp,
                                        maxLines = 2
                                    )

                                    // Capability chips
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (model.capabilities.hasVision) {
                                            MiniCapabilityTag("Vision", SkeuoColors.LedCyan)
                                        }
                                        if (model.capabilities.hasThinking) {
                                            MiniCapabilityTag("Reasoning", SkeuoColors.LedPurple)
                                        }
                                        if (model.capabilities.hasWebSearch) {
                                            MiniCapabilityTag("Live Search", SkeuoColors.LedGreen)
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = SkeuoColors.LedCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniCapabilityTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
