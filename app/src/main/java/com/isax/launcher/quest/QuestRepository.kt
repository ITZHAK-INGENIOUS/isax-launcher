package com.isax.launcher.quest

import android.content.Context
import com.isax.launcher.core.IsaxPaths
import com.isax.launcher.core.JsonStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** Persistance + diffusion des quêtes (source de vérité de l'UI et des widgets). */
object QuestRepository {

    private lateinit var file: File
    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests

    fun init(ctx: Context) {
        file = File(IsaxPaths.root, "quests.json")
        _quests.value = decode(JsonStore.readArray(file))
    }

    fun add(q: Quest) { _quests.value = _quests.value + q; persist() }

    fun newQuest(title: String, type: QuestType, hour: Int, minute: Int, days: Set<Int>, xp: Int) =
        Quest(
            id = UUID.randomUUID().toString(),
            title = title, type = type, hour = hour, minute = minute,
            repeatDays = days, xp = xp,
            createdAt = System.currentTimeMillis()
        )

    /** Les quêtes, de la plus récemment créée à la plus ancienne. */
    fun newestFirst(): List<Quest> = _quests.value.sortedByDescending { it.createdAt }

    fun toggleDone(id: String) {
        _quests.value = _quests.value.map { if (it.id == id) it.copy(done = !it.done) else it }
        persist()
    }

    fun remove(id: String) { _quests.value = _quests.value.filterNot { it.id == id }; persist() }

    fun markFired(id: String) {
        _quests.value = _quests.value.map { if (it.id == id) it.copy(firedAt = System.currentTimeMillis()) else it }
        persist()
    }

    /** Score de chasseur : XP cumulée sur les quêtes terminées. */
    fun totalXp(): Int = _quests.value.filter { it.done }.sumOf { it.xp }

    fun active(): List<Quest> = _quests.value.filter { !it.done }

    private fun persist() { JsonStore.write(file, encode(_quests.value)) }

    private fun encode(list: List<Quest>): JSONArray {
        val arr = JSONArray()
        list.forEach { q ->
            arr.put(JSONObject().apply {
                put("id", q.id); put("title", q.title); put("detail", q.detail)
                put("type", q.type.name); put("hour", q.hour); put("minute", q.minute)
                put("repeatDays", JSONArray(q.repeatDays.toList().sorted()))
                put("xp", q.xp); put("done", q.done); put("firedAt", q.firedAt)
                put("createdAt", q.createdAt)
            })
        }
        return arr
    }

    private fun decode(arr: JSONArray): List<Quest> = (0 until arr.length()).map { i ->
        val j = arr.getJSONObject(i)
        val days = j.optJSONArray("repeatDays")?.let { a -> (0 until a.length()).map { a.getInt(it) }.toSet() } ?: emptySet()
        Quest(
            id = j.getString("id"),
            title = j.getString("title"),
            detail = j.optString("detail"),
            type = runCatching { QuestType.valueOf(j.optString("type")) }.getOrDefault(QuestType.ONESHOT),
            hour = j.optInt("hour", 8), minute = j.optInt("minute", 0),
            repeatDays = days, xp = j.optInt("xp", 10),
            done = j.optBoolean("done"), firedAt = j.optLong("firedAt"),
            createdAt = j.optLong("createdAt")
        )
    }
}
