package com.isax.launcher.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.home.AppInfo
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader
import com.isax.launcher.window.FreeformLauncher
import com.isax.launcher.window.WindowSession
import kotlin.math.roundToInt

/**
 * Tableau des fenêtres : liste des apps ouvertes en mode fenêtré, avec
 * dispositions (mosaïque / côte à côte) et relance freeform.
 *
 * Rappel honnête : sans root, le vrai rendu multi-fenêtre dépend du support
 * freeform du système (Options développeur > redimensionnement forcé). Isax
 * pilote la géométrie et le lancement ; l'affichage simultané effectif est
 * décidé par Android.
 */
@Composable
fun WindowBoard(ctx: Context, onDismiss: () -> Unit, allApps: List<AppInfo>) {
    var screenW by remember { mutableStateOf(1080) }
    var screenH by remember { mutableStateOf(1920) }
    val wins = WindowSession.windows.toList()

    Box(Modifier.fillMaxSize().background(IsaxColors.Glass), contentAlignment = Alignment.Center) {
        NeonPanel(Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.82f)) {
            PanelHeader("FENÊTRES (${wins.size})", IsaxColors.Cyan, onDismiss)
            Text("Capacité freeform : ${FreeformLauncher.maxWindows} — dépend du système",
                color = IsaxColors.Text.copy(alpha = 0.5f), fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Btn("Mosaïque") { WindowSession.tile(4, screenW, screenH) }
                Btn("Tout fermer") { WindowSession.clear() }
                Btn("Côte à côte") {
                    if (allApps.size >= 2) FreeformLauncher.launchAdjacent(ctx, allApps[0], allApps[1], screenW, screenH)
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(allApps) { app: AppInfo ->
                    val open = wins.any { it.app.packageName == app.packageName }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(app.label, color = IsaxColors.Text, fontSize = 13.sp)
                        Spacer(Modifier.weight(1f))
                        Btn(if (open) "Focaliser" else "Fenêtrer") {
                            WindowSession.add(app, screenW, screenH)
                            FreeformLauncher.launch(ctx, app)
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxSize().onGloballyPositioned {
            screenW = it.size.width; screenH = it.size.height
        })
    }
}

@Composable
private fun Btn(label: String, onClick: () -> Unit) {
    Text(
        label,
        color = IsaxColors.Cyan,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, IsaxColors.Cyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .background(IsaxColors.Cyan.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .pointerInput(label) { detectDragGestures { _, _ -> } }
    )
}
