package com.isax.launcher.quest

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * Planificateur de quêtes via AlarmManager (exact, survit au Doze avec
 * setExactAndAllowWhileIdle). Reprogrammé au démarrage par BootReceiver.
 */
object QuestScheduler {

    const val ACTION_FIRE = "com.isax.launcher.QUEST_FIRE"
    const val EXTRA_ID = "quest_id"

    fun schedule(ctx: Context, q: Quest) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pending(ctx, q.id)
        val whenMs = if (q.type == QuestType.DAILY && q.repeatDays.isNotEmpty()) nextWeekly(q) else nextToday(q)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMs, pi)
    }

    fun cancel(ctx: Context, id: String) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(ctx, id))
    }

    fun rescheduleAll(ctx: Context) {
        QuestRepository.active().forEach { if (it.type != QuestType.ONESHOT || it.firedAt == 0L) schedule(ctx, it) }
    }

    private fun pending(ctx: Context, id: String): PendingIntent {
        val i = Intent(ctx, QuestAlarmReceiver::class.java).apply {
            action = ACTION_FIRE; putExtra(EXTRA_ID, id)
        }
        return PendingIntent.getBroadcast(
            ctx, id.hashCode(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextToday(q: Quest): Long {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, q.hour); set(Calendar.MINUTE, q.minute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        if (c.timeInMillis <= System.currentTimeMillis()) c.add(Calendar.DAY_OF_YEAR, 1)
        return c.timeInMillis
    }

    private fun nextWeekly(q: Quest): Long {
        val c = Calendar.getInstance()
        var best = Long.MAX_VALUE
        for (d in q.repeatDays) {
            val t = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, d)
                set(Calendar.HOUR_OF_DAY, q.hour); set(Calendar.MINUTE, q.minute)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            if (t.timeInMillis <= System.currentTimeMillis()) t.add(Calendar.DAY_OF_YEAR, 7)
            if (t.timeInMillis < best) best = t.timeInMillis
        }
        return if (best == Long.MAX_VALUE) nextToday(q) else best
    }
}
