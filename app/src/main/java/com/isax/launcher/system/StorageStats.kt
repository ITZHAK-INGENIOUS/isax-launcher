package com.isax.launcher.system

import android.os.Environment
import android.os.StatFs

/** Occupation du stockage interne (lecture seule, aucune permission requise). */
data class StorageSnapshot(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val usedPct: Float
)

object StorageStats {

    fun sample(): StorageSnapshot = runCatching {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        val used = (total - free).coerceAtLeast(0L)
        StorageSnapshot(
            totalBytes = total,
            freeBytes = free,
            usedBytes = used,
            usedPct = if (total == 0L) 0f else used.toFloat() / total.toFloat()
        )
    }.getOrDefault(StorageSnapshot(0L, 0L, 0L, 0f))

    /** Formatage court et lisible (Go / Mo), sans dépendance externe. */
    fun human(bytes: Long): String = when {
        bytes >= 1L shl 40 -> "%.1f To".format(bytes.toDouble() / (1L shl 40).toDouble())
        bytes >= 1L shl 30 -> "%.1f Go".format(bytes.toDouble() / (1L shl 30).toDouble())
        bytes >= 1L shl 20 -> "%.0f Mo".format(bytes.toDouble() / (1L shl 20).toDouble())
        else -> "%.0f Ko".format(bytes.toDouble() / 1024.0)
    }
}
