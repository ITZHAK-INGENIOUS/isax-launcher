package com.isax.launcher.quest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/** Réveil d'une quête : notification système + reprogrammation si récurrente. */
class QuestAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val id = intent.getStringExtra(QuestScheduler.EXTRA_ID) ?: return
        val q = QuestRepository.quests.value.firstOrNull { it.id == id } ?: return
        if (q.done) return

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel("isax_quests", "Quêtes Isax", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        nm.notify(
            id.hashCode(),
            NotificationCompat.Builder(ctx, "isax_quests")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Quête : ${q.title}")
                .setContentText("+${q.xp} XP — touchez pour valider depuis Isax")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        )
        QuestRepository.markFired(id)
        if (q.type == QuestType.DAILY) QuestScheduler.schedule(ctx, q)
    }
}
