package com.isax.launcher.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.isax.launcher.quest.QuestScheduler

/** Reprogramme les quêtes planifiées après un redémarrage. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED ->
                QuestScheduler.rescheduleAll(context)
        }
    }
}
