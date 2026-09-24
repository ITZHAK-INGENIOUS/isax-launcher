package com.isax.launcher.ui

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.anim.IsaxMotion
import kotlinx.coroutines.delay

/**
 * Écran d'ouverture — animation de lancement.
 *
 * Séquence (≈1,3 s) : le cercle « système » se referme, le losange respire, puis
 * le mot-marque ISAX monte lettre par lettre avant de se dissoudre dans le HUD.
 * Purement décoratif : aucun accès disque, aucune attente réseau.
 */
@Composable
fun SplashOverlay(onFinished: () -> Unit) {
    var ringDone by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(label = "splash")
    val breath by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash-breath"
    )

    LaunchedEffect(Unit) {
        delay(420)
        ringDone = true
        delay(880)
        onFinished()
    }

    Box(
        Modifier.fillMaxSize().background(IsaxColors.Deep),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(96.dp)) {
                val stroke = 2.5.dp.toPx()
                val inset = stroke / 2f
                val side = Size(size.width - stroke, size.height - stroke)

                drawArc(
                    color = IsaxColors.Cyan.copy(alpha = 0.16f),
                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(inset, inset), size = side,
                    style = Stroke(stroke)
                )
                val sweep by animateFloatAsState(
                    targetValue = if (ringDone) 360f else 0f,
                    animationSpec = IsaxMotion.fluid(820),
                    label = "splash-ring"
                )
                drawArc(
                    color = IsaxColors.Cyan,
                    startAngle = -90f, sweepAngle = sweep, useCenter = false,
                    topLeft = Offset(inset, inset), size = side,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )

                // Losange central, comme la marque du chasseur.
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.width * 0.13f * breath
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r, cy)
                    lineTo(cx, cy + r)
                    lineTo(cx - r, cy)
                    close()
                }
                drawPath(path, color = IsaxColors.Cyan.copy(alpha = 0.9f))
            }

            Spacer(Modifier.height(22.dp))
            SplashWordmark()
            Spacer(Modifier.height(8.dp))
            Text(
                "système prêt",
                color = IsaxColors.Violet.copy(alpha = if (ringDone) 0.85f else 0f),
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

/** « ISAX » en lettres qui montent l'une après l'autre. */
@Composable
private fun SplashWordmark() {
    val letters = listOf("I", "S", "A", "X")
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        letters.forEach { letter ->
            val offset by animateFloatAsState(
                targetValue = if (started) 0f else 22f,
                animationSpec = IsaxMotion.landing(520),
                label = "splash-$letter"
            )
            Text(
                letter,
                color = if (letter == "A") IsaxColors.Violet else IsaxColors.Cyan,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = offset.dp)
            )
        }
    }
}
