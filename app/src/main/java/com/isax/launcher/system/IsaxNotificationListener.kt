package com.isax.launcher.system

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Capte le flux de notifications système et l'expose au HUD « Status Window »
 * façon Solo Leveling. Nécessite l'autorisation manuelle dans
 * Paramètres > Notifications > Accès aux notifications (voir DeviceSettings).
 */
class IsaxNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeNotifications?.forEach { publish(it) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) = publish(sbn)

    private fun publish(sbn: StatusBarNotification) {
        val ex = sbn.notification?.extras ?: return
        val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = ex.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return
        NotificationBus.push(
            NotifItem(
                packageName = sbn.packageName,
                title = title,
                text = text,
                postedAt = sbn.postTime
            )
        )
    }
}
