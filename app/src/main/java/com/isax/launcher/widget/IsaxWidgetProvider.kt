package com.isax.launcher.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.isax.launcher.R
import com.isax.launcher.quest.QuestRepository

/**
 * Widget fourni PAR Isax (le launcher est à la fois hôte et fournisseur).
 * Affiche le résumé des quêtes actives sur l'écran d'accueil.
 */
class IsaxWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        val active = QuestRepository.quests.value.filter { !it.done }
        val body = if (active.isEmpty()) "Aucune quête active"
        else active.take(3).joinToString("\n") { "• ${it.title}" }
        ids.forEach { id ->
            val v = RemoteViews(ctx.packageName, R.layout.isax_quest_widget)
            v.setTextViewText(R.id.widget_body, body)
            mgr.updateAppWidget(id, v)
        }
    }
}
