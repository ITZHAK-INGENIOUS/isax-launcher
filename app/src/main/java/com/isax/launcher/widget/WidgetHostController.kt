package com.isax.launcher.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent

/**
 * Hôte de widgets tiers (AppWidgetHost) : permet à Isax d'accueillir les
 * widgets d'autres applications sur son bureau, comme le fait le launcher AOSP.
 */
class WidgetHostController(private val ctx: Context) {

    val host = AppWidgetHost(ctx, HOST_ID)
    val manager: AppWidgetManager = AppWidgetManager.getInstance(ctx)

    private val bound = mutableMapOf<Int, AppWidgetHostView>()

    fun start() = host.startListening()
    fun stop() = host.stopListening()

    fun allProviders(): List<AppWidgetProviderInfo> = manager.installedProviders

    /** Alloue un ID, puis renvoie l'Intent système de sélection/configuration. */
    fun newPickIntent(): Pair<Int, Intent> {
        val id = host.allocateAppWidgetId()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }
        return id to intent
    }

    fun bind(appWidgetId: Int, info: AppWidgetProviderInfo): AppWidgetHostView? {
        manager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider)
        return host.createView(ctx, appWidgetId, info).also { bound[appWidgetId] = it }
    }

    fun views(): List<AppWidgetHostView> = bound.values.toList()

    fun delete(appWidgetId: Int) {
        bound.remove(appWidgetId)?.let { host.deleteAppWidgetId(appWidgetId) }
    }

    companion object {
        // 0x15AX is not valid hex; use a real int id.
        const val HOST_ID = 0x15A0
    }
}
