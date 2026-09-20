package com.example.cpen321application.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val CONFETTI_COLORS = listOf(
    Color(0xFFE53935), // red
    Color(0xFFFB8C00), // orange
    Color(0xFFFDD835), // yellow
    Color(0xFF43A047), // green
    Color(0xFF1E88E5), // blue
    Color(0xFF8E24AA), // purple
)

/** One falling piece of paper. All values are fractions of the canvas size. */
private data class ConfettiPiece(
    val startX: Float,
    val delay: Float,
    val swayAmount: Float,
    val swayCycles: Float,
    val spin: Float,
    val widthPx: Float,
    val heightPx: Float,
    val color: Color,
)

/**
 * A one-shot confetti burst drawn with Canvas (no third-party library).
 * Pass a [restartKey] that changes each time you want a new burst.
 */
@Composable
fun ConfettiOverlay(
    restartKey: Any,
    modifier: Modifier = Modifier,
    pieceCount: Int = 80,
    durationMillis: Int = 4000,
) {
    val pieces = remember(restartKey) {
        List(pieceCount) {
            ConfettiPiece(
                startX = Random.nextFloat(),
                delay = Random.nextFloat() * 0.35f,
                swayAmount = 0.02f + Random.nextFloat() * 0.06f,
                swayCycles = 1.5f + Random.nextFloat() * 2.5f,
                spin = 360f * (1 + Random.nextInt(3)) * if (Random.nextBoolean()) 1f else -1f,
                widthPx = 14f + Random.nextFloat() * 10f,
                heightPx = 6f + Random.nextFloat() * 8f,
                color = CONFETTI_COLORS[Random.nextInt(CONFETTI_COLORS.size)],
            )
        }
    }

    val progress = remember(restartKey) { Animatable(0f) }
    LaunchedEffect(restartKey) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis, easing = LinearEasing))
    }

    Canvas(modifier = modifier) {
        pieces.forEach { piece ->
            // Each piece starts a little later than the one before it.
            val span = 1f - piece.delay
            val t = ((progress.value - piece.delay) / span).coerceIn(0f, 1f)
            if (t <= 0f) return@forEach

            val sway = sin(t * piece.swayCycles * 2f * PI.toFloat()) * piece.swayAmount
            val x = (piece.startX + sway) * size.width
            val y = -30f + t * (size.height + 60f)

            rotate(degrees = piece.spin * t, pivot = Offset(x, y)) {
                drawRect(
                    color = piece.color.copy(alpha = 1f - (t * t * 0.4f)),
                    topLeft = Offset(x - piece.widthPx / 2f, y - piece.heightPx / 2f),
                    size = Size(piece.widthPx, piece.heightPx),
                )
            }
        }
    }
}
