package com.example.ui.compare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiProviderType
import com.example.model.ComparisonTileState
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
fun MultiModelCompareScreen(
    selectedModels: List<Pair<AiProviderType, String>>,
    comparisonResults: List<ComparisonTileState>,
    isComparing: Boolean,
    onAddOrChangeModel: (provider: AiProviderType, modelId: String) -> Unit,
    onRemoveModel: (provider: AiProviderType, modelId: String) -> Unit,
    onRunCompare: (prompt: String) -> Unit,
    onStopCompare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }
    var showModelPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SkeuoColors.ChassisBg)
            .padding(12.dp)
    ) {
        // Comparison Hardware Rack Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .tactileRaised(RoundedCornerShape(8.dp), elevation = 4.dp)
                .border(1.dp, SkeuoColors.ChassisPlateBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HardwareScrew(size = 8.dp)
                Spacer(modifier = Modifier.width(8.dp))
                StatusLed(color = SkeuoColors.LedAmber, size = 8.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MULTI-AI MATRIX",
                    color = SkeuoColors.TextHighContrast,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "${selectedModels.size} / 4 NODES",
                color = SkeuoColors.LedAmber,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Models Rack
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            selectedModels.forEach { (provider, modelId) ->
                val modelInfo = ModelCatalog.findModel(modelId)
                val displayName = modelInfo?.displayName ?: modelId

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF14171E))
                        .border(1.dp, Color(provider.accentColorHex).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusLed(color = Color(provider.accentColorHex), size = 6.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = displayName,
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (selectedModels.size > 2) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove model",
                            tint = SkeuoColors.TextMuted,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onRemoveModel(provider, modelId) }
                        )
                    }
                }
            }

            if (selectedModels.size < 4) {
                Row(
                    modifier = Modifier
                        .tactileButton(
                            shape = RoundedCornerShape(6.dp),
                            onClick = { showModelPicker = true }
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add node",
                        tint = SkeuoColors.LedCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ADD NODE",
                        color = SkeuoColors.LedCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Parallel Comparison Cards List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(comparisonResults, key = { "${it.provider.id}_${it.modelId}" }) { tile ->
                ComparisonCard(tile = tile)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Broadcast Prompt Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .tactileRecessed(RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                textStyle = TextStyle(
                    color = SkeuoColors.TextHighContrast,
                    fontSize = 13.sp
                ),
                cursorBrush = SolidColor(SkeuoColors.LedAmber),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                decorationBox = { inner ->
                    if (promptInput.isEmpty()) {
                        Text(
                            text = "Broadcast prompt to all ${selectedModels.size} models...",
                            color = SkeuoColors.TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    inner()
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (isComparing) {
                Row(
                    modifier = Modifier
                        .tactileButton(
                            shape = RoundedCornerShape(6.dp),
                            accentColor = SkeuoColors.LedRed,
                            onClick = onStopCompare
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Halt",
                        tint = SkeuoColors.LedRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "HALT",
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .tactileButton(
                            enabled = promptInput.isNotBlank(),
                            shape = RoundedCornerShape(6.dp),
                            accentColor = SkeuoColors.LedAmber,
                            onClick = {
                                val p = promptInput.trim()
                                if (p.isNotBlank()) {
                                    onRunCompare(p)
                                }
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Compare",
                        tint = SkeuoColors.LedAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BROADCAST",
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }

    if (showModelPicker) {
        ModelSelectionDialog(
            currentModelId = "",
            onDismiss = { showModelPicker = false },
            onSelect = { selected ->
                showModelPicker = false
                onAddOrChangeModel(selected.provider, selected.id)
            }
        )
    }
}

@Composable
fun ComparisonCard(tile: ComparisonTileState) {
    val context = LocalContext.current
    val modelInfo = ModelCatalog.findModel(tile.modelId)
    val displayName = modelInfo?.displayName ?: tile.modelId

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .tactileRaised(RoundedCornerShape(8.dp), elevation = 3.dp)
            .border(1.dp, SkeuoColors.ChassisPlateBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Tile Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusLed(
                    color = if (tile.isGenerating) SkeuoColors.LedAmber else Color(tile.provider.accentColorHex),
                    size = 7.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = tile.provider.displayName.uppercase(),
                        color = Color(tile.provider.accentColorHex),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = displayName,
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tile.latencyMs > 0) {
                    Text(
                        text = "${tile.latencyMs}ms",
                        color = SkeuoColors.LedCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                TactileIconButton(
                    icon = Icons.Default.ContentCopy,
                    contentDescription = "Copy best response",
                    size = 26.dp,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Comparison AI Output", tile.responseText))
                        Toast.makeText(context, "Copied response from $displayName", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (tile.isGenerating && tile.responseText.isEmpty()) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusLed(color = SkeuoColors.LedAmber, size = 6.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GENERATING STREAM...",
                    color = SkeuoColors.LedAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            MarkdownRenderer(
                content = tile.responseText,
                fontSize = 13
            )
        }
    }
}
