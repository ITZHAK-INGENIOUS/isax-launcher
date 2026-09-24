package com.isax.launcher.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/** Instantané « feuille de personnage » : batterie (HP), RAM (MP), CPU (AGI). */
data class Stats(
    val batteryPct: Float,
    val isCharging: Boolean,
    val ramUsedPct: Float,
    val ramUsedMb: Int,
    val ramTotalMb: Int,
    val cpuPct: Float
)

object SystemStats {

    fun sample(ctx: Context): Stats {
        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) / 100f
        val charging = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.let { it.getIntExtra(BatteryManager.EXTRA_STATUS, -1) }
            ?.let { it == BatteryManager.BATTERY_STATUS_CHARGING || it == BatteryManager.BATTERY_STATUS_FULL }
            ?: false

        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val usedMb = ((mi.totalMem - mi.availMem) / (1024 * 1024)).toInt()
        val totalMb = (mi.totalMem / (1024 * 1024)).toInt()

        return Stats(
            batteryPct = pct,
            isCharging = charging,
            ramUsedPct = if (totalMb == 0) 0f else usedMb.toFloat() / totalMb,
            ramUsedMb = usedMb,
            ramTotalMb = totalMb,
            cpuPct = readCpuLoad() / 100f
        )
    }

    /** Charge CPU approximative via /proc/stat (delta entre deux lectures). */
    private fun readCpuLoad(): Float = runCatching {
        val line = java.io.File("/proc/stat").readLines().first()
        val v = line.split(" ").filter { it.isNotBlank() }.drop(1).map { it.toLong() }
        val idle = v[3] + (v.getOrNull(4) ?: 0)
        val total = v.sum()
        val load = (1f - idle.toFloat() / total.toFloat()) * 100f
        load.coerceIn(0f, 100f)
    }.getOrDefault(0f)
}
