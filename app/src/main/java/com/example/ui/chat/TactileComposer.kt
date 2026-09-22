package com.example.ui.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiModelInfo
import com.example.ui.components.TactileIconButton
import com.example.ui.components.TactileTogglePill
import com.example.ui.theme.SkeuoColors
import com.example.ui.theme.StatusLed
import com.example.ui.theme.tactileButton
import com.example.ui.theme.tactileRaised
import com.example.ui.theme.tactileRecessed
import java.io.ByteArrayOutputStream

@Composable
fun TactileComposer(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    attachedImageBase64: String?,
    onAttachedImageChanged: (String?) -> Unit,
    enableWebSearch: Boolean,
    onToggleWebSearch: () -> Unit,
    isStreaming: Boolean,
    currentModel: AiModelInfo,
    onSendMessage: () -> Unit,
    onStopGeneration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Standard Android Photo Picker (zero broad storage permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    // Compress to reasonable payload size
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val byteArray = stream.toByteArray()
                    val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                    onAttachedImageChanged(base64String)
                }
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SkeuoColors.ChassisPlate)
            .border(1.dp, SkeuoColors.ChassisPlateBorder)
            .padding(10.dp)
    ) {
        // Thumbnail preview if image attached
        if (!attachedImageBase64.isNullOrBlank()) {
            val bitmap = remember(attachedImageBase64) {
                try {
                    val bytes = Base64.decode(attachedImageBase64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF141820))
                        .border(1.dp, Color(0xFF262D3D), RoundedCornerShape(6.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Attachment preview",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Image attached (Vision payload ready)",
                        color = SkeuoColors.LedCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { onAttachedImageChanged(null) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attachment",
                            tint = SkeuoColors.TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Recessed text area tray
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .tactileRecessed(RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = onInputTextChanged,
                textStyle = TextStyle(
                    color = SkeuoColors.TextHighContrast,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Default
                ),
                cursorBrush = SolidColor(SkeuoColors.LedCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp, max = 130.dp),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Transmit instructions to ${currentModel.displayName}...",
                            color = SkeuoColors.TextMuted,
                            fontSize = 13.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Controls Bar: Attachments, Search Toggle, Send/Stop Pushbutton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Attach image button (only enabled if model supports vision)
                TactileIconButton(
                    icon = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Attach image for vision",
                    size = 32.dp,
                    tint = if (currentModel.capabilities.hasVision) SkeuoColors.LedCyan else SkeuoColors.TextFaint,
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                // Live Web Search toggle
                if (currentModel.capabilities.hasWebSearch) {
                    TactileTogglePill(
                        label = "Search",
                        isChecked = enableWebSearch,
                        onCheckedChange = { onToggleWebSearch() },
                        icon = Icons.Default.Language,
                        accentColor = SkeuoColors.LedGreen
                    )
                }
            }

            // Physical Hardware Send / Stop Button
            if (isStreaming) {
                // Active Stop Button
                Row(
                    modifier = Modifier
                        .tactileButton(
                            shape = RoundedCornerShape(8.dp),
                            accentColor = SkeuoColors.LedRed,
                            onClick = onStopGeneration
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusLed(color = SkeuoColors.LedRed, size = 8.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Halt streaming",
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
                // Push-to-Transmit Button
                val hasInput = inputText.isNotBlank() || attachedImageBase64 != null
                Row(
                    modifier = Modifier
                        .tactileButton(
                            enabled = hasInput,
                            shape = RoundedCornerShape(8.dp),
                            accentColor = if (hasInput) SkeuoColors.LedCyan else null,
                            onClick = onSendMessage
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusLed(
                        color = if (hasInput) SkeuoColors.LedCyan else Color(0xFF262C38),
                        size = 8.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TRANSMIT",
                        color = if (hasInput) SkeuoColors.TextHighContrast else SkeuoColors.TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        tint = if (hasInput) SkeuoColors.LedCyan else SkeuoColors.TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
