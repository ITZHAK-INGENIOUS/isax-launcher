package com.isax.launcher.quest

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Minuteur de session (Pomodoro) transformé en « donjon » chronométré.
 * Tourne en service de premier plan avec notification persistante.
 */
class PomodoroService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val minutes = intent?.getIntExtra(EXTRA_MIN, 25) ?: 25
        startForeground(NOTIF_ID, buildNotification(minutes * 60))
        startCountdown(minutes * 60)
        return START_NOT_STICKY
    }

    private fun startCountdown(totalSeconds: Int) {
        _remaining.value = totalSeconds
        job?.cancel()
        job = scope.launch {
            var left = totalSeconds
            while (left > 0) {
                _remaining.value = left
                notification(left)
                delay(1000)
                left--
            }
            stopSelf()
        }
    }

    private fun notification(sec: Int) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(sec))
    }

    private fun buildNotification(sec: Int): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "Session active", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val m = sec / 60; val s = sec % 60
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("Donjon en cours")
            .setContentText(String.format("%02d:%02d restants", m, s))
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() { job?.cancel(); super.onDestroy() }

    companion object {
        const val EXTRA_MIN = "minutes"
        private const val CHANNEL = "isax_pomodoro"
        private const val NOTIF_ID = 0x15A
        val _remaining = MutableStateFlow(0)
        val remaining: StateFlow<Int> get() = _remaining

        fun start(ctx: Context, minutes: Int) {
            val i = Intent(ctx, PomodoroService::class.java).putExtra(EXTRA_MIN, minutes)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
            else ctx.startService(i)
        }
    }
}
