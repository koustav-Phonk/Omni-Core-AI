package com.example.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

object SkeuoColors {
    // Chassis & Plate
    val ChassisBg = Color(0xFF090A0D)
    val ChassisPlate = Color(0xFF111318)
    val ChassisPlateBorder = Color(0xFF222630)

    // Surfaces
    val SurfaceRaised = Color(0xFF191C24)
    val SurfaceRaisedTop = Color(0xFF212630)
    val SurfaceRaisedBottom = Color(0xFF13161C)

    val SurfaceRecessed = Color(0xFF07080A)
    val SurfaceRecessedTop = Color(0xFF050608)
    val SurfaceRecessedBottom = Color(0xFF0C0D11)

    val MetallicRack = Color(0xFF1D222B)
    val MetallicRackHighlight = Color(0xFF384050)
    val RubberMatte = Color(0xFF14161A)

    // Bevel highlights and deep cast shadows
    val BevelHighlight = Color(0xFF384152)
    val BevelHighlightSubtle = Color(0x33FFFFFF)
    val BevelShadow = Color(0xFF020304)
    val BevelShadowDark = Color(0xFF000000)

    // Text & Symbols
    val TextHighContrast = Color(0xFFF1F3F7)
    val TextNormal = Color(0xFFD4D8E2)
    val TextMuted = Color(0xFF7A869A)
    val TextFaint = Color(0xFF454D5C)

    // Status Diodes & Accents
    val LedGreen = Color(0xFF00E676)
    val LedGreenGlow = Color(0x6600E676)
    val LedCyan = Color(0xFF00E5FF)
    val LedCyanGlow = Color(0x6600E5FF)
    val LedAmber = Color(0xFFFFB300)
    val LedAmberGlow = Color(0x66FFB300)
    val LedRed = Color(0xFFFF3D00)
    val LedRedGlow = Color(0x66FF3D00)
    val LedPurple = Color(0xFFB388FF)
}

/**
 * Creates a physical raised 3D surface with top-left highlight and bottom-right shadow bevel.
 */
fun Modifier.tactileRaised(
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    elevation: Dp = 3.dp,
    bevelAlpha: Float = 0.6f
): Modifier = this
    .shadow(elevation, shape, clip = false)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(SkeuoColors.SurfaceRaisedTop, SkeuoColors.SurfaceRaisedBottom)
        ),
        shape = shape
    )
    .drawBehind {
        val strokeWidth = 1.2f * density
        // Top-left highlight
        drawLine(
            color = SkeuoColors.BevelHighlight.copy(alpha = bevelAlpha),
            start = Offset(0f, size.height),
            end = Offset(0f, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = SkeuoColors.BevelHighlight.copy(alpha = bevelAlpha),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth
        )
        // Bottom-right shadow
        drawLine(
            color = SkeuoColors.BevelShadow,
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth * 1.5f
        )
        drawLine(
            color = SkeuoColors.BevelShadow,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth * 1.5f
        )
    }

/**
 * Creates a recessed/sunken 3D hardware panel or tray.
 */
fun Modifier.tactileRecessed(
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    bevelAlpha: Float = 0.8f
): Modifier = this
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(SkeuoColors.SurfaceRecessedTop, SkeuoColors.SurfaceRecessedBottom)
        ),
        shape = shape
    )
    .border(
        width = 1.dp,
        color = SkeuoColors.ChassisPlateBorder,
        shape = shape
    )
    .drawBehind {
        val strokeWidth = 1.5f * density
        // Inset top shadow
        drawLine(
            color = SkeuoColors.BevelShadowDark.copy(alpha = bevelAlpha),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth * 1.8f
        )
        drawLine(
            color = SkeuoColors.BevelShadowDark.copy(alpha = bevelAlpha),
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = strokeWidth * 1.2f
        )
        // Bottom edge catch-light
        drawLine(
            color = SkeuoColors.BevelHighlightSubtle,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1f * density
        )
    }

/**
 * Physical hardware push-button with active pressed state depression and lighting shift.
 */
@Composable
fun Modifier.tactileButton(
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    accentColor: Color? = null,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) 2f else 0f,
        label = "button_press_anim"
    )

    val baseBrush = remember(isPressed, accentColor) {
        if (isPressed) {
            Brush.verticalGradient(
                colors = listOf(
                    accentColor?.copy(alpha = 0.35f) ?: Color(0xFF101217),
                    Color(0xFF181B22)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    accentColor?.copy(alpha = 0.22f) ?: SkeuoColors.SurfaceRaisedTop,
                    accentColor?.copy(alpha = 0.08f) ?: SkeuoColors.SurfaceRaisedBottom
                )
            )
        }
    }

    return this
        .offset { IntOffset(0, pressOffsetY.dp.roundToPx()) }
        .clip(shape)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
        .background(baseBrush, shape)
        .border(
            width = 1.dp,
            color = if (isPressed) SkeuoColors.BevelShadow else (accentColor?.copy(alpha = 0.5f) ?: SkeuoColors.BevelHighlight),
            shape = shape
        )
        .drawBehind {
            if (!isPressed) {
                // Top highlight rim
                drawLine(
                    color = Color(0x40FFFFFF),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.5f * density
                )
                // Bottom physical drop shadow
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.5f * density
                )
            }
        }
}

/**
 * Physical Status Diode LED with ambient bloom.
 */
@Composable
fun StatusLed(
    color: Color,
    isActive: Boolean = true,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isActive) color else Color(0xFF222630))
            .border(0.8.dp, Color(0xFF0A0C0F), CircleShape)
            .drawBehind {
                if (isActive) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f),
                        radius = this.size.minDimension * 0.35f,
                        center = Offset(this.size.width * 0.38f, this.size.height * 0.38f)
                    )
                }
            }
    )
}

/**
 * Metal screw rivet for corners of physical workstation racks.
 */
@Composable
fun HardwareScrew(
    modifier: Modifier = Modifier,
    size: Dp = 9.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2C323E), Color(0xFF14171D))
                )
            )
            .border(0.6.dp, Color(0xFF3F4858), CircleShape)
            .drawBehind {
                // Screw slot
                drawLine(
                    color = Color(0xFF0C0E12),
                    start = Offset(this.size.width * 0.25f, this.size.height * 0.5f),
                    end = Offset(this.size.width * 0.75f, this.size.height * 0.5f),
                    strokeWidth = 1f * density
                )
            }
    )
}
