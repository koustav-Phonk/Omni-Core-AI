package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SkeuoColors
import com.example.ui.theme.StatusLed
import com.example.ui.theme.tactileButton
import com.example.ui.theme.tactileRaised
import com.example.ui.theme.tactileRecessed

@Composable
fun TactileRockerSwitch(
    option1: String,
    option2: String,
    selectedOptionIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .tactileRecessed(RoundedCornerShape(6.dp))
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val options = listOf(option1, option2)
        options.forEachIndexed { index, title ->
            val isSelected = selectedOptionIndex == index

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .then(
                        if (isSelected) {
                            Modifier.tactileRaised(RoundedCornerShape(4.dp), elevation = 2.dp)
                        } else {
                            Modifier.clickable { onOptionSelected(index) }
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusLed(
                        color = if (isSelected) SkeuoColors.LedCyan else Color(0xFF2B323F),
                        size = 6.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title.uppercase(),
                        color = if (isSelected) SkeuoColors.TextHighContrast else SkeuoColors.TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TactileTogglePill(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    accentColor: Color = SkeuoColors.LedCyan,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .tactileButton(
                shape = RoundedCornerShape(6.dp),
                accentColor = if (isChecked) accentColor else null,
                onClick = { onCheckedChange(!isChecked) }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusLed(
            color = if (isChecked) accentColor else Color(0xFF222834),
            size = 7.dp
        )
        Spacer(modifier = Modifier.width(6.dp))
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isChecked) accentColor else SkeuoColors.TextMuted,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = label.uppercase(),
            color = if (isChecked) SkeuoColors.TextHighContrast else SkeuoColors.TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun TactileIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    tint: Color = SkeuoColors.TextNormal,
    accentColor: Color? = null,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .tactileButton(
                enabled = enabled,
                shape = RoundedCornerShape(8.dp),
                accentColor = accentColor,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else SkeuoColors.TextFaint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
