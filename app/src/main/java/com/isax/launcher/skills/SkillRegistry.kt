package com.isax.launcher.skills

import android.content.Context
import com.isax.launcher.core.IsaxPaths
import org.json.JSONObject
import java.io.File

/** Registre central des compétences actives. */
object SkillRegistry {

    private val skills = LinkedHashMap<String, IsaxSkill>()

    fun register(skill: IsaxSkill) { skills[skill.id] = skill }
    fun unregister(id: String) { skills.remove(id) }
    fun all(): List<IsaxSkill> = skills.values.toList()
    fun byCategory(c: SkillCategory) = skills.values.filter { it.category == c }

    /** Charge les packs déclaratifs installés dans /isax/skills/<id>/manifest.json. */
    fun loadInstalled(ctx: Context) {
        IsaxPaths.skills.listFiles()?.forEach { dir ->
            val manifest = File(dir, "manifest.json")
            if (manifest.exists()) {
                runCatching {
                    val j = JSONObject(manifest.readText())
                    val cat = SkillCategory.valueOf(j.optString("category", "WIDGET"))
                    register(DeclarativeSkill(
                        id = j.optString("id", dir.name),
                        displayName = j.optString("name", dir.name),
                        version = j.optString("version", "0.0.1"),
                        category = cat,
                        description = j.optString("description", "")
                    ))
                }
            }
        }
    }
}
