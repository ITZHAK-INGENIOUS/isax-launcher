package com.isax.launcher.skills

import android.content.Context
import com.isax.launcher.core.IsaxPaths
import com.isax.launcher.core.JsonStore
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipInputStream

/**
 * Installation/désinstallation de packs de compétences (fichier .isaxskill = zip
 * contenant manifest.json). Volontairement limité aux packs DÉCLARATIFS :
 * on n'exécute pas de code tiers non signé (pas de DexClassLoader automatique),
 * ce qui évite une faille de sécurité majeure.
 */
object SkillInstaller {

    fun installFromZip(ctx: Context, zip: File): Boolean {
        val target = File(IsaxPaths.skills, zip.nameWithoutExtension)
        target.mkdirs()
        ZipInputStream(zip.inputStream()).use { zis ->
            var e = zis.nextEntry
            while (e != null) {
                val out = File(target, e.name)
                if (!out.canonicalPath.startsWith(target.canonicalPath)) { e = zis.nextEntry; continue }
                out.parentFile?.mkdirs()
                out.outputStream().use { zis.copyTo(it) }
                e = zis.nextEntry
            }
        }
        return File(target, "manifest.json").exists()
    }

    fun installFromJson(ctx: Context, json: String): Boolean = runCatching {
        val j = JSONObject(json)
        val dir = File(IsaxPaths.skills, j.getString("id")); dir.mkdirs()
        JsonStore.write(File(dir, "manifest.json"), j)
        true
    }.getOrDefault(false)

    fun uninstall(id: String): Boolean =
        File(IsaxPaths.skills, id).deleteRecursively()

    fun listInstalled(): List<File> =
        IsaxPaths.skills.listFiles()?.filter { File(it, "manifest.json").exists() } ?: emptyList()
}

/** Compétence déclarative générée depuis un manifest.json (pack installé). */
class DeclarativeSkill(
    override val id: String,
    override val displayName: String,
    override val version: String,
    override val category: SkillCategory,
    override val description: String
) : IsaxSkill {
    @Composable
    override fun Render(context: Context) { Text(displayName) }
}
