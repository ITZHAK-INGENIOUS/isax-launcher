package com.isax.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Glyphes du HUD, dessinés au Canvas.
 *
 * Tracés à la main plutôt qu'importés d'une bibliothèque d'icônes : ils
 * reprennent exactement les formes du croquis (batterie segmentée, jauge CPU
 * circulaire, carte de stockage, ondes réseau) et se redimensionnent sans
 * crénelage.
 */

@Composable
fun BatteryGlyph(pct: Float, accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val h = size.height
        val bodyW = size.width * 0.84f
        val capW = size.width * 0.07f
        val stroke = (h * 0.14f).coerceAtLeast(1f)
        val radius = CornerRadius(h * 0.28f)

        drawRoundRect(
            color = accent.copy(alpha = 0.30f),
            topLeft = Offset(0f, 0f),
            size = Size(bodyW, h),
            cornerRadius = radius,
            style = Stroke(stroke)
        )
        val inner = (bodyW - stroke * 2f).coerceAtLeast(0f)
        drawRoundRect(
            color = accent,
            topLeft = Offset(stroke, stroke),
            size = Size(inner * pct.coerceIn(0f, 1f), (h - stroke * 2f).coerceAtLeast(0f)),
            cornerRadius = CornerRadius(h * 0.18f),
            style = Fill
        )
        drawRoundRect(
            color = accent.copy(alpha = 0.55f),
            topLeft = Offset(bodyW + capW * 0.35f, h * 0.30f),
            size = Size(capW, h * 0.40f),
            cornerRadius = CornerRadius(capW * 0.35f)
        )
    }
}

@Composable
fun CpuGlyph(pct: Float, accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val d = minOf(size.width, size.height)
        val stroke = (d * 0.16f).coerceAtLeast(1f)
        val inset = stroke / 2f
        val box = Size(d - stroke, d - stroke)
        val origin = Offset((size.width - d) / 2f + inset, (size.height - d) / 2f + inset)

        drawArc(
            color = accent.copy(alpha = 0.28f),
            startAngle = -90f, sweepAngle = 360f, useCenter = false,
            topLeft = origin, size = box, style = Stroke(stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = accent,
            startAngle = -90f, sweepAngle = 360f * pct.coerceIn(0f, 1f), useCenter = false,
            topLeft = origin, size = box, style = Stroke(stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StorageGlyph(pct: Float, accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val h = size.height
        val stroke = (h * 0.13f).coerceAtLeast(1f)
        val radius = CornerRadius(h * 0.26f)
        val unitH = (h - stroke * 1.6f) / 2f

        drawRoundRect(
            color = accent.copy(alpha = if (pct > 0.02f) 1f else 0.28f),
            topLeft = Offset(0f, 0f), size = Size(size.width, unitH),
            cornerRadius = radius, style = Stroke(stroke)
        )
        drawRoundRect(
            color = accent.copy(alpha = if (pct > 0.5f) 1f else 0.28f),
            topLeft = Offset(0f, unitH + stroke * 1.6f), size = Size(size.width, unitH),
            cornerRadius = radius, style = Stroke(stroke)
        )
    }
}

@Composable
fun NetworkGlyph(pct: Float, accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val h = size.height
        val stroke = (h * 0.14f).coerceAtLeast(1f)
        val cx = size.width / 2f
        val cy = h
        val levels = 3
        for (i in 0 until levels) {
            val active = pct >= (i + 1).toFloat() / levels
            val r = (h * (0.42f + 0.30f * i)) * 2f
            drawArc(
                color = accent.copy(alpha = if (active) 1f else 0.22f),
                startAngle = 200f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(cx - r / 2f, cy - r / 2f),
                size = Size(r, r),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        drawCircle(color = accent, radius = stroke * 0.85f, center = Offset(cx, cy - stroke))
    }
}

/** Ligne de conduite pointillée du croquis (« ─ ─ ─ »). */
@Composable
fun DottedLeader(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = size.height.coerceAtLeast(1f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 6f), 0f)
        )
    }
}

/** Curseur clignotant minimaliste (shell replié). */
@Composable
fun BlinkingCursor(color: Color) {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(760, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor-alpha"
    )
    Box(
        Modifier
            .width(7.dp)
            .height(15.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(color.copy(alpha = alpha))
    )
}

/** Case à cocher du croquis : carré vide laqué. */
@Composable
fun CheckBoxGlyph(checked: Boolean, accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val d = minOf(size.width, size.height)
        val stroke = (d * 0.13f).coerceAtLeast(1f)
        val inset = stroke / 2f
        val side = d - stroke

        drawRoundRect(
            color = accent.copy(alpha = if (checked) 0.35f else 0.7f),
            topLeft = Offset(inset, inset),
            size = Size(side, side),
            cornerRadius = CornerRadius(d * 0.22f),
            style = if (checked) Fill else Stroke(stroke)
        )
        if (checked) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(d * 0.26f, d * 0.53f)
                lineTo(d * 0.44f, d * 0.72f)
                lineTo(d * 0.76f, d * 0.30f)
            }
            drawPath(path, color = accent, style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }
}

/** Petit trait vertical décoratif sous le mot-marque « ISAX ». */
@Composable
fun MarkRule(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxHeight()) {
        drawLine(
            color = color,
            start = Offset(size.width / 2f, 0f),
            end = Offset(size.width / 2f, size.height),
            strokeWidth = size.width.coerceAtLeast(1f)
        )
    }
}

/** Pastille d'application : icône réelle si disponible, sinon initiale. */
@Composable
fun AppDot(label: String, accent: Color, size: androidx.compose.ui.unit.Dp = 20.dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(size / 3f)).background(accent.copy(alpha = 0.16f)))
}
