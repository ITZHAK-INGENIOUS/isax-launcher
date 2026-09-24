package com.isax.launcher.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.isax.launcher.core.Prefs
import com.isax.launcher.gesture.SGestureDetector
import com.isax.launcher.home.AppInfo
import com.isax.launcher.home.AppRepository
import com.isax.launcher.theme.IsaxColors

/**
 * Bureau d'Isax : fond + grille d'applications + cadre de gestes.
 * Un SEUL flux d'événements alimente le détecteur (correctif v0.2) ; on ne
 * consomme pas le drag pour que la grille reste cliquable.
 */
@Composable
fun HomeScreen(
    ctx: Context,
    apps: List<AppInfo>,
    onGesture: (SGestureDetector.Result) -> Unit,
    header: @Composable () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongPress: (AppInfo) -> Unit
) {
    val detector = remember { SGestureDetector(Prefs.sSensitivity) }
    val pad = Prefs.swipeThreshold.toFloat()

    Box(
        Modifier
            .fillMaxSize()
            .background(IsaxColors.Deep.copy(alpha = Prefs.wallpaperDim))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { detector.reset() },
                    onDragEnd = { onGesture(detector.classify(pad)) },
                    onDragCancel = { detector.reset() },
                    onDrag = { change, _ ->
                        detector.addPoint(change.position.x, change.position.y, change.uptimeMillis)
                    }
                )
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            header()
            Spacer(Modifier.height(10.dp))
            Text(
                "INVENTAIRE",
                color = IsaxColors.Cyan,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(apps) { app -> AppTile(ctx, app, onAppClick, onAppLongPress) }
            }
        }
    }
}

@Composable
private fun AppTile(
    ctx: Context,
    app: AppInfo,
    onClick: (AppInfo) -> Unit,
    onLong: (AppInfo) -> Unit
) {
    val icon = remember(app.packageName) {
        AppRepository.icon(ctx, app.packageName)?.let { d ->
            runCatching { d.toBitmap(128, 128).asImageBitmap() }.getOrNull()
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.pointerInput(app.packageName) {
            detectTapGestures(onTap = { onClick(app) }, onLongPress = { onLong(app) })
        }
    ) {
        if (icon != null) {
            Image(bitmap = icon, contentDescription = app.label, modifier = Modifier.size(52.dp))
        } else {
            Box(Modifier.size(52.dp).background(IsaxColors.Deep2), contentAlignment = Alignment.Center) {
                Text(app.label.take(1), color = IsaxColors.Cyan)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(app.label, color = IsaxColors.Text, fontSize = 10.sp, maxLines = 1)
    }
}
