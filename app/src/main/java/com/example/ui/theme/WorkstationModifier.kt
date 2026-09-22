package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Surface tactile behavior style for dark physical workstation panels.
 */
enum class WorkstationBevelStyle {
    /**
     * Physical module or hardware deck extruded upward toward the operator.
     * Features bottom-right cast shadow, top-left specular highlight, and convex gradient.
     */
    RAISED,

    /**
     * Recessed or sunken bay for keyboards, display meters, or port compartments.
     * Features deep top/left inner ambient cavity shadows and bottom catch-light rim.
     */
    RECESSED,

    /**
     * Flush hardware chassis plate with beveled perimeter chamfer.
     */
    FLUSH,

    /**
     * Engraved or etched groove/trough for hardware seams and dividing gutters.
     */
    ENGRAVED
}

/**
 * Tone variations for dark charcoal physical workstation surfaces.
 */
enum class CharcoalTone(
    val topColor: Color,
    val bottomColor: Color,
    val borderLight: Color,
    val borderDark: Color
) {
    /** Deepest matte cavity black (#060709 -> #0A0C10) */
    DEEP_VOID(
        topColor = Color(0xFF060709),
        bottomColor = Color(0xFF0B0D12),
        borderLight = Color(0xFF222836),
        borderDark = Color(0xFF030406)
    ),

    /** Standard industrial chassis slate (#0F1116 -> #161A22) */
    CHASSIS_SLATE(
        topColor = Color(0xFF161A22),
        bottomColor = Color(0xFF0F1116),
        borderLight = Color(0xFF333D50),
        borderDark = Color(0xFF080A0D)
    ),

    /** Elevated physical workstation panel (#1D222C -> #13161D) */
    PANEL_DARK(
        topColor = Color(0xFF202633),
        bottomColor = Color(0xFF13161E),
        borderLight = Color(0xFF3E4A60),
        borderDark = Color(0xFF08090E)
    ),

    /** Metallic tactile plate with subtle cyan tint (#18202A -> #10141C) */
    ANODIZED_STEEL(
        topColor = Color(0xFF1D2633),
        bottomColor = Color(0xFF101620),
        borderLight = Color(0xFF3B506D),
        borderDark = Color(0xFF090D14)
    )
}

/**
 * Reusable Compose Modifier that simulates a dark charcoal physical workstation aesthetic
 * with customizable inner and outer shadows, specular edge bevels, and directional lighting.
 *
 * @param shape Corner clipping and outline geometry (defaults to 8dp rounded rectangle)
 * @param style Physical bevel behavior (RAISED, RECESSED, FLUSH, ENGRAVED)
 * @param tone Charcoal tonal variation and gradient palette
 * @param elevation Outer cast elevation shadow (for RAISED/FLUSH styles)
 * @param innerShadowDepth Depth/extent of inner cavity drop shadow (for RECESSED/ENGRAVED styles)
 * @param bevelAlpha Opacity multiplier for the specular catch-light line (0f to 1f)
 * @param accentGlow Optional subtle ambient LED glow color along the active beveled rim
 */
fun Modifier.darkCharcoalWorkstation(
    shape: CornerBasedShape = RoundedCornerShape(8.dp),
    style: WorkstationBevelStyle = WorkstationBevelStyle.RAISED,
    tone: CharcoalTone = CharcoalTone.PANEL_DARK,
    elevation: Dp = if (style == WorkstationBevelStyle.RAISED) 4.dp else 0.dp,
    innerShadowDepth: Dp = if (style == WorkstationBevelStyle.RECESSED || style == WorkstationBevelStyle.ENGRAVED) 6.dp else 0.dp,
    bevelAlpha: Float = 0.75f,
    accentGlow: Color? = null
): Modifier = composed {
    val density = LocalDensity.current
    val innerShadowPx = with(density) { innerShadowDepth.toPx() }

    val backgroundBrush = remember(style, tone) {
        when (style) {
            WorkstationBevelStyle.RAISED -> Brush.verticalGradient(
                colors = listOf(tone.topColor, tone.bottomColor)
            )
            WorkstationBevelStyle.RECESSED, WorkstationBevelStyle.ENGRAVED -> Brush.verticalGradient(
                colors = listOf(tone.bottomColor, tone.topColor)
            )
            WorkstationBevelStyle.FLUSH -> Brush.linearGradient(
                colors = listOf(tone.topColor, tone.bottomColor),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        }
    }

    val chamferBorderBrush = remember(style, tone, accentGlow) {
        val topHighlight = accentGlow?.copy(alpha = 0.8f) ?: tone.borderLight
        val bottomShadow = tone.borderDark

        when (style) {
            WorkstationBevelStyle.RAISED, WorkstationBevelStyle.FLUSH -> Brush.linearGradient(
                colors = listOf(topHighlight, bottomShadow),
                start = Offset.Zero,
                end = Offset(1000f, 1000f)
            )
            WorkstationBevelStyle.RECESSED, WorkstationBevelStyle.ENGRAVED -> Brush.linearGradient(
                colors = listOf(bottomShadow, topHighlight),
                start = Offset.Zero,
                end = Offset(1000f, 1000f)
            )
        }
    }

    this
        // 1. Directional outer cast shadow (elevation)
        .then(
            if (elevation > 0.dp && (style == WorkstationBevelStyle.RAISED || style == WorkstationBevelStyle.FLUSH)) {
                Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    clip = false,
                    ambientColor = Color(0x99000000),
                    spotColor = Color(0xEE000000)
                )
            } else {
                Modifier
            }
        )
        // 2. Base charcoal gradient surface
        .background(brush = backgroundBrush, shape = shape)
        // 3. Hardware chamfer perimeter border
        .border(
            width = 1.dp,
            brush = chamferBorderBrush,
            shape = shape
        )
        // 4. Clip content to the rounded shape
        .clip(shape)
        // 5. Render physical bevels and inner shadows
        .drawWithContent {
            // Draw standard child composable content first
            drawContent()

            val width = size.width
            val height = size.height
            val stroke1 = 1.2f * density.density
            val stroke2 = 2.0f * density.density

            when (style) {
                WorkstationBevelStyle.RAISED -> {
                    // Top-Left Specular Catch-Light (light source from top-left)
                    val highlightColor = Color.White.copy(alpha = (0.18f * bevelAlpha).coerceIn(0f, 1f))
                    drawLine(
                        color = highlightColor,
                        start = Offset(0f, 0f),
                        end = Offset(width, 0f),
                        strokeWidth = stroke1
                    )
                    drawLine(
                        color = highlightColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, height),
                        strokeWidth = stroke1
                    )

                    // Bottom-Right Deep Crevice Shadow
                    val shadowColor = Color.Black.copy(alpha = 0.85f)
                    drawLine(
                        color = shadowColor,
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = stroke2
                    )
                    drawLine(
                        color = shadowColor,
                        start = Offset(width, 0f),
                        end = Offset(width, height),
                        strokeWidth = stroke2
                    )

                    // Optional Accent Glow on Top Edge (e.g. active diode illumination)
                    if (accentGlow != null) {
                        drawLine(
                            color = accentGlow.copy(alpha = 0.45f),
                            start = Offset(0f, 0f),
                            end = Offset(width, 0f),
                            strokeWidth = 1.5f * density.density
                        )
                    }
                }

                WorkstationBevelStyle.RECESSED, WorkstationBevelStyle.ENGRAVED -> {
                    // Inner Drop Shadows: Cavity occludes ambient light at top & left
                    if (innerShadowPx > 0f) {
                        // Top inner shadow gradient
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xCC000000),
                                    Color(0x66000000),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = innerShadowPx
                            ),
                            topLeft = Offset.Zero,
                            size = Size(width, innerShadowPx)
                        )

                        // Left inner shadow gradient
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xAA000000),
                                    Color(0x44000000),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = innerShadowPx
                            ),
                            topLeft = Offset.Zero,
                            size = Size(innerShadowPx, height)
                        )
                    }

                    // Bottom Lip Specular Highlight (reflection from floor/console)
                    val bottomLipColor = Color.White.copy(alpha = (0.12f * bevelAlpha).coerceIn(0f, 1f))
                    drawLine(
                        color = bottomLipColor,
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = 1.0f * density.density
                    )
                    drawLine(
                        color = bottomLipColor,
                        start = Offset(width, 0f),
                        end = Offset(width, height),
                        strokeWidth = 0.8f * density.density
                    )
                }

                WorkstationBevelStyle.FLUSH -> {
                    // Subtle perimeter chamfer bevel
                    drawLine(
                        color = Color.White.copy(alpha = 0.10f * bevelAlpha),
                        start = Offset(0f, 0f),
                        end = Offset(width, 0f),
                        strokeWidth = stroke1
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.5f),
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = stroke1
                    )
                }
            }
        }
}

/**
 * Specialized convenience modifier for recessed input bays, meter screens, or sunken trays.
 */
fun Modifier.workstationRecessedBay(
    shape: CornerBasedShape = RoundedCornerShape(8.dp),
    tone: CharcoalTone = CharcoalTone.DEEP_VOID,
    innerShadowDepth: Dp = 6.dp,
    bevelAlpha: Float = 0.8f
): Modifier = darkCharcoalWorkstation(
    shape = shape,
    style = WorkstationBevelStyle.RECESSED,
    tone = tone,
    innerShadowDepth = innerShadowDepth,
    bevelAlpha = bevelAlpha
)

/**
 * Specialized convenience modifier for raised hardware plates, cards, and rack tiles.
 */
fun Modifier.workstationRaisedPlate(
    shape: CornerBasedShape = RoundedCornerShape(8.dp),
    tone: CharcoalTone = CharcoalTone.PANEL_DARK,
    elevation: Dp = 4.dp,
    bevelAlpha: Float = 0.75f,
    accentGlow: Color? = null
): Modifier = darkCharcoalWorkstation(
    shape = shape,
    style = WorkstationBevelStyle.RAISED,
    tone = tone,
    elevation = elevation,
    bevelAlpha = bevelAlpha,
    accentGlow = accentGlow
)
