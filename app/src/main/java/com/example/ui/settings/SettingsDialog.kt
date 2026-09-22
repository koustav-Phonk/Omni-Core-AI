package com.example.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HardwareScrew
import com.example.ui.theme.SkeuoColors
import com.example.ui.theme.StatusLed
import com.example.ui.theme.tactileButton
import com.example.ui.theme.tactileRaised
import com.example.ui.theme.tactileRecessed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    temperature: Float,
    onTemperatureChanged: (Float) -> Unit,
    fontSize: Int,
    onFontSizeChanged: (Int) -> Unit,
    onExportMarkdown: suspend () -> String,
    onClearAllData: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                        text = "WORKSTATION PARAMS",
                        color = SkeuoColors.TextHighContrast,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SkeuoColors.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Temperature Slider Control
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .tactileRecessed(RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TEMPERATURE (CREATIVITY)",
                        color = SkeuoColors.TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%.2f", temperature),
                        color = SkeuoColors.LedCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = temperature,
                    onValueChange = onTemperatureChanged,
                    valueRange = 0.0f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = SkeuoColors.LedCyan,
                        activeTrackColor = SkeuoColors.LedCyan,
                        inactiveTrackColor = Color(0xFF222834)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Font Size Adjuster
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .tactileRecessed(RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TYPOGRAPHY SCALE",
                        color = SkeuoColors.TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${fontSize}pt",
                        color = SkeuoColors.LedCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = fontSize.toFloat(),
                    onValueChange = { onFontSizeChanged(it.toInt()) },
                    valueRange = 12f..18f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = SkeuoColors.LedCyan,
                        activeTrackColor = SkeuoColors.LedCyan,
                        inactiveTrackColor = Color(0xFF222834)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Export Chat Markdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tactileButton(
                        shape = RoundedCornerShape(8.dp),
                        accentColor = SkeuoColors.LedCyan,
                        onClick = {
                            scope.launch {
                                val md = onExportMarkdown()
                                if (md.isNotBlank()) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Chat Export", md))
                                    Toast.makeText(context, "Session exported as Markdown to clipboard", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No messages to export", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = SkeuoColors.LedCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EXPORT SESSION AS MARKDOWN",
                    color = SkeuoColors.TextHighContrast,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clear all data
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tactileButton(
                        shape = RoundedCornerShape(8.dp),
                        accentColor = SkeuoColors.LedRed,
                        onClick = {
                            onClearAllData()
                            Toast.makeText(context, "All session data purged", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = SkeuoColors.LedRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PURGE ALL SESSIONS",
                    color = SkeuoColors.TextHighContrast,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
