package com.isax.launcher.window

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import com.isax.launcher.core.Prefs
import com.isax.launcher.home.AppInfo
import com.isax.launcher.home.AppRepository

/**
 * Lancement d'une app dans une fenêtre libre (multi-window freeform).
 *
 * Sans root, `setLaunchBounds` + `FLAG_ACTIVITY_LAUNCH_ADJACENT` ne prennent
 * effet que si la fonctionnalité freeform est autorisée par le système
 * (Options développeur > « Forcer le redimensionnement des activités »).
 * Si ce n'est pas le cas, l'appel est ignoré proprement par le framework et
 * l'app s'ouvre en plein écran — aucun crash : on retombe sur un lancement
 * normal. C'est la limite réelle d'Android, pas un bug du launcher.
 */
object FreeformLauncher {

    /** Combien d'apps le device peut réellement afficher en fenêtres simultanées. */
    val maxWindows: Int
        get() = if (Prefs.freeformEnabled) 4 else 1

    fun launch(ctx: Context, app: AppInfo, bounds: Rect? = null) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = android.content.ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
        }
        val opts = if (bounds != null && Prefs.freeformEnabled) {
            ActivityOptions.makeBasic().apply { setLaunchBounds(bounds) }.toBundle()
        } else null
        runCatching { ctx.startActivity(intent, opts) }
    }

    fun launchAdjacent(ctx: Context, left: AppInfo, right: AppInfo, screenW: Int, screenH: Int) {
        launch(ctx, left, Rect(0, 0, screenW / 2, screenH))
        launch(ctx, right, Rect(screenW / 2, 0, screenW, screenH))
    }

    fun openAppSettings(ctx: Context, app: AppInfo) {
        val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(android.net.Uri.parse("package:" + app.packageName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { ctx.startActivity(i) }
    }

    fun icon(ctx: Context, app: AppInfo) = AppRepository.icon(ctx, app.packageName)
}
