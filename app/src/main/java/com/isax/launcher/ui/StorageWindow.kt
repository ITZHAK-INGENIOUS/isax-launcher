package com.isax.launcher.ui

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.system.StorageStats
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.anim.IsaxMotion
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader
import com.isax.launcher.ui.components.StatBar
import kotlinx.coroutines.delay

/**
 * Fenêtre « Stockage » — ouverte depuis le libellé Stockage du HUD.
 * Lecture seule : Isax ne supprime rien du système ; le navigateur /isax/home
 * reste joignable depuis le SAO Hub.
 */
@Composable
fun StorageWindow(ctx: Context, onDismiss: () -> Unit, onOpenFiles: () -> Unit) {
    var snapshot by remember { mutableStateOf(StorageStats.sample()) }
    LaunchedEffect(Unit) {
        while (true) {
            snapshot = StorageStats.sample()
            delay(3000)
        }
    }

    Box(
        Modifier.fillMaxSize().background(IsaxColors.Glass),
        contentAlignment = Alignment.Center
    ) {
        NeonPanel(
            Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.72f).verticalScroll(rememberScrollState())
        ) {
            PanelHeader("STOCKAGE", IsaxColors.Cyan, onDismiss)
            Spacer(Modifier.height(14.dp))

            SegmentedBar(snapshot.usedPct)

            Spacer(Modifier.height(12.dp))
            Text(
                "Rempli ${(snapshot.usedPct * 100).toInt()} %",
                color = IsaxColors.Cyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${StorageStats.human(snapshot.usedBytes)} utilisés · " +
                    "${StorageStats.human(snapshot.freeBytes)} libres · " +
                    "${StorageStats.human(snapshot.totalBytes)} au total",
                color = IsaxColors.Text.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(Modifier.height(18.dp))
            StatBar("Occupation", snapshot.usedPct, IsaxColors.Cyan)
            Spacer(Modifier.height(6.dp))
            Text(
                "Lecture seule — la mesure se rafraîchit toutes les 3 s.",
                color = IsaxColors.Text.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "PARCOURIR /isax/home  →",
                color = IsaxColors.Violet,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun SegmentedBar(usedPct: Float) {
    val pct by animateFloatAsState(
        targetValue = usedPct.coerceIn(0f, 1f),
        animationSpec = IsaxMotion.firm(),
        label = "storage-bar"
    )
    Row(
        Modifier.fillMaxWidth().height(22.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(12) { i ->
            val active = pct >= (i + 1).toFloat() / 12f
            val partial = pct >= i.toFloat() / 12f
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        when {
                            active -> IsaxColors.Cyan
                            partial -> IsaxColors.Cyan.copy(alpha = 0.45f)
                            else -> IsaxColors.Text.copy(alpha = 0.08f)
                        },
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}
