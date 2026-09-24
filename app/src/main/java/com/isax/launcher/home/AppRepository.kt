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
}
