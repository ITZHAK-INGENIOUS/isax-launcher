package com.isax.launcher.home

import android.content.Context
import com.isax.launcher.core.IsaxPaths
import com.isax.launcher.core.JsonStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.io.File

/**
 * Fréquentation des applications : horodatage du dernier lancement + compteur.
 *
 * Sert au tri « Récents en haut » de l'inventaire et du tiroir d'applications.
 * Persistance JSON comme le reste du launcher (aucune base de données).
 */
object UsageStore {

    private lateinit var file: File
    private val _lastUsed = MutableStateFlow<Map<String, Long>>(emptyMap())
    val lastUsed: StateFlow<Map<String, Long>> = _lastUsed

    fun init(ctx: Context) {
        file = File(IsaxPaths.root, "usage.json")
        val obj = JsonStore.readObject(file)
        val map = mutableMapOf<String, Long>()
        obj.keys().forEach { k -> map[k] = obj.optLong(k, 0L) }
        _lastUsed.value = map
    }

    /** À appeler à chaque lancement d'app (tap sur une tuile, tuile ou dock). */
    fun record(pkg: String) {
        val map = _lastUsed.value.toMutableMap()
        map[pkg] = System.currentTimeMillis()
        _lastUsed.value = map
        persist()
    }

    fun timestamp(pkg: String): Long = _lastUsed.value[pkg] ?: 0L

    /** Paquets, du plus récemment utilisé au plus ancien. */
    fun recentPackages(limit: Int = 12): List<String> =
        _lastUsed.value.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }

    /** Position d'un paquet dans l'ordre d'usage (-1 s'il n'a jamais été lancé). */
    fun rank(pkg: String): Int = recentPackages(Int.MAX_VALUE.coerceAtMost(2000)).indexOf(pkg)

    private fun persist() {
        val obj = JSONObject()
        _lastUsed.value.forEach { (k, v) -> obj.put(k, v) }
        JsonStore.write(file, obj)
    }
}
