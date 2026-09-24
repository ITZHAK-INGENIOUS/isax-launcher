package com.isax.launcher.skills

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Contrat d'une « Compétence » (Skill) Isax : widget ou outil ajouté au
 * launcher. Deux natures :
 *  - EMBARQUÉE : classe Kotlin fournie avec l'APK.
 *  - INSTALLÉE : pack JSON dans /isax/skills/<id>/manifest.json (+ assets
 *    éventuels). Peut contenir un thème, un icon-pack ou une définition de
 *    widget « déclaratif » (pas de code non signé exécuté, par sécurité).
 */
interface IsaxSkill {
    val id: String
    val displayName: String
    val version: String
    val category: SkillCategory
    val description: String
    val accent: Color get() = Color(0xFF00F0FF)

    fun onInstall(context: Context) {}
    fun onUninstall(context: Context) {}

    /** Rendu de la compétence (widget flottant ou écran d'outil). */
    @Composable
    fun Render(context: Context)
}

enum class SkillCategory { WIDGET, TOOL, THEME, ICON_PACK }
