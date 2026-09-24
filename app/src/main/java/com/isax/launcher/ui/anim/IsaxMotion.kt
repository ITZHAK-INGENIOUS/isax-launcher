package com.isax.launcher.ui.anim

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Grammaire de mouvement d'Isax — calquée sur le « flow » Apple.
 *
 * Trois règles, appliquées partout dans l'interface :
 *  1. les courbes décélèrent longuement en sortie (le mouvement « atterrit », il
 *     ne s'arrête jamais net) ;
 *  2. les durées restent courtes (170–460 ms) pour préserver la réactivité ;
 *  3. les éléments qui apparaissent gagnent un très léger dépassement, comme les
 *     feuilles du système iOS.
 *
 * Un seul endroit à changer pour retoucher toute l'animation de l'app.
 */
object IsaxMotion {

    /** Courbe signature : entrée franche, sortie longue. Transitions plein écran. */
    val Fluid: Easing = CubicBezierEasing(0.32f, 0.72f, 0.02f, 1f)

    /** Même famille avec un léger rebond : éléments qui « atterrissent ». */
    val Landing: Easing = CubicBezierEasing(0.34f, 1.16f, 0.64f, 1f)

    /** Neutre : fondus, barres de progression, interpolations continues. */
    val Gentle: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    const val Quick = 170
    const val Base = 300
    const val Slow = 460

    /** Ressort doux : déplacements liés au doigt (aucune durée fixe). */
    fun <T> soft() = spring<T>(dampingRatio = 0.84f, stiffness = Spring.StiffnessLow)

    /** Ressort plus ferme : barres, valeurs numériques, petites bascules. */
    fun <T> firm() = spring<T>(dampingRatio = 0.90f, stiffness = Spring.StiffnessMediumLow)

    fun <T> fluid(duration: Int = Base) = tween<T>(durationMillis = duration, easing = Fluid)

    fun <T> landing(duration: Int = Base) = tween<T>(durationMillis = duration, easing = Landing)

    /** Fondu simple, sans déplacement. */
    fun <T> gentle(duration: Int = Quick) = tween<T>(durationMillis = duration, easing = Gentle)
}
