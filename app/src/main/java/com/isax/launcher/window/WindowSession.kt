package com.isax.launcher.window

import android.graphics.Rect
import androidx.compose.runtime.mutableStateListOf
import com.isax.launcher.home.AppInfo

/**
 * Une « fenêtre » gérée par le launcher : une carte flottante déplaçable qui
 * pilote le vrai lancement de l'app en mode fenêtré libre (freeform).
 */
data class LauncherWindow(
    val app: AppInfo,
    val bounds: Rect,
    val z: Int
)

/**
 * Pile de fenêtres. Le launcher ne peut pas embarquer le rendu d'une autre app
 * (réservé au système/root) : il gère la géométrie, les positions et le
 * lancement freeform, ce qui reproduit l'effet « bureau fenêtré » demandé.
 */
object WindowSession {
    val windows = mutableStateListOf<LauncherWindow>()

    fun add(app: AppInfo, screenW: Int, screenH: Int): LauncherWindow {
        val w = (screenW * 0.62f).toInt()
        val h = (screenH * 0.42f).toInt()
        val dx = (24 + windows.size * 36)
        val dy = (140 + windows.size * 48)
        val win = LauncherWindow(
            app = app,
            bounds = Rect(dx, dy, dx + w, dy + h),
            z = windows.size
        )
        windows.add(win)
        return win
    }

    fun move(app: AppInfo, newBounds: Rect) {
        val i = windows.indexOfFirst { it.app.packageName == app.packageName && it.app.activityName == app.activityName }
        if (i >= 0) windows[i] = windows[i].copy(bounds = newBounds)
    }

    fun close(app: AppInfo) {
        windows.removeAll { it.app.packageName == app.packageName && it.app.activityName == app.activityName }
    }

    fun clear() = windows.clear()

    fun tile(count: Int, screenW: Int, screenH: Int) {
        val n = count.coerceAtMost(windows.size)
        for (i in 0 until n) {
            val cols = if (n <= 2) n else 2
            val rows = if (n <= 2) 1 else 2
            val c = i % cols; val r = i / cols
            val w = screenW / cols; val h = (screenH - 120) / rows
            windows[i] = windows[i].copy(bounds = Rect(c * w, 120 + r * h, (c + 1) * w, 120 + (r + 1) * h))
        }
    }
}
