package com.isax.launcher.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val isSystem: Boolean
)

/** Accès à l'inventaire des applications installées (nécessite QUERY_ALL_PACKAGES). */
object AppRepository {

    fun loadAll(ctx: Context): List<AppInfo> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val flags = PackageManager.MATCH_ALL
        return pm.queryIntentActivities(intent, flags)
            .asSequence()
            .map { ri ->
                AppInfo(
                    label = ri.loadLabel(pm).toString(),
                    packageName = ri.activityInfo.packageName,
                    activityName = ri.activityInfo.name,
                    isSystem = (ri.activityInfo.applicationInfo.flags and
                        android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .distinctBy { it.packageName + "/" + it.activityName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun icon(ctx: Context, pkg: String): Drawable? = runCatching {
        ctx.packageManager.getApplicationIcon(pkg)
    }.getOrNull()

    /**
     * Lance une application normalement (APK de launcher : pas de freeform par
     * défaut) et note son usage — c'est ce qui alimente « récents en haut ».
     */
    fun launch(ctx: Context, app: AppInfo) {
        val direct = Intent().setClassName(app.packageName, app.activityName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        val ok = runCatching { ctx.startActivity(direct) }.isSuccess
        if (!ok) {
            ctx.packageManager.getLaunchIntentForPackage(app.packageName)?.let { fallback ->
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { ctx.startActivity(fallback) }
            }
        }
        UsageStore.record(app.packageName)
    }

    /** Lance par nom de paquet (utilisé par la liste de notifications du HUD). */
    fun launchPackage(ctx: Context, pkg: String) {
        val i = ctx.packageManager.getLaunchIntentForPackage(pkg) ?: return
        runCatching { ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        UsageStore.record(pkg)
    }
}
