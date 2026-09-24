package com.isax.launcher.quest

/** Une « Quête » = une tâche programmable, style Solo Leveling. */
data class Quest(
    val id: String,
    val title: String,
    val detail: String = "",
    val type: QuestType = QuestType.ONESHOT,
    val hour: Int = 8,
    val minute: Int = 0,
    val repeatDays: Set<Int> = emptySet(), // 1=lundi … 7=dimanche (vide = unique)
    val xp: Int = 10,
    val done: Boolean = false,
    val firedAt: Long = 0L,
    /** Horodatage de création : sert au tri « les plus récents » du HUD. */
    val createdAt: Long = 0L
)

enum class QuestType { ONESHOT, DAILY, TIMED }
