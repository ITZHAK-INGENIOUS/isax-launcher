package com.isax.launcher.gesture

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Analyseur de tracé multi-gestes (correctif clé par rapport au scaffold v0.1).
 *
 * Le scaffold d'origine empilait deux `pointerInput` concurrents
 * (detectVerticalDragGestures + detectDragGestures) : le premier consommait
 * l'événement et le détecteur de « S » ne recevait jamais rien. Ici, un seul
 * flux d'événements alimente un unique accumulateur, et la classification
 * (SWIPE_DOWN / SWIPE_UP / S / TAP) se fait une fois, au relâchement.
 */
class SGestureDetector(private var sensitivity: Float = 0.65f) {

    data class Pt(val x: Float, val y: Float, val t: Long)

    enum class Result { NONE, TAP, SWIPE_UP, SWIPE_DOWN, S_SHAPE }

    private val pts = mutableListOf<Pt>()

    fun reset() = pts.clear()

    fun setSensitivity(v: Float) { sensitivity = v.coerceIn(0f, 1f) }

    fun addPoint(x: Float, y: Float, t: Long) {
        val last = pts.lastOrNull()
        if (last == null || hypot((x - last.x).toDouble(), (y - last.y).toDouble()) > 6.0) {
            pts.add(Pt(x, y, t))
        }
    }

    /** Classifie le tracé complet. `threshold` = distance minimale d'un swipe (px). */
    fun classify(threshold: Float = 28f): Result {
        if (pts.size < 3) return Result.NONE
        val dx = pts.last().x - pts.first().x
        val dy = pts.last().y - pts.first().y
        val width = (pts.maxOf { it.x } - pts.minOf { it.x }).coerceAtLeast(1f)
        val height = (pts.maxOf { it.y } - pts.minOf { it.y }).coerceAtLeast(1f)

        // 1) Geste « S » : tracé global plus haut que large ET alternance de courbures
        if (height / width > 1.1f && isSShape()) return Result.S_SHAPE

        // 2) Swipe vertical dominant
        if (abs(dy) > threshold && abs(dy) > abs(dx)) {
            return if (dy > 0) Result.SWIPE_DOWN else Result.SWIPE_UP
        }
        return if (hypot(dx.toDouble(), dy.toDouble()) < threshold) Result.TAP else Result.NONE
    }

    /** Vrai si la séquence de courbures ressemble à un « S » (deux boucles opposées). */
    private fun isSShape(): Boolean {
        if (pts.size < 8) return false
        val signs = mutableListOf<Int>()
        for (i in 2 until pts.size) {
            val a1 = atan2((pts[i - 1].y - pts[i - 2].y).toDouble(), (pts[i - 1].x - pts[i - 2].x).toDouble())
            val a2 = atan2((pts[i].y - pts[i - 1].y).toDouble(), (pts[i].x - pts[i - 1].x).toDouble())
            var d = a2 - a1
            while (d > Math.PI) d -= 2 * Math.PI
            while (d < -Math.PI) d += 2 * Math.PI
            if (abs(d) > 0.05) signs.add(if (d > 0) 1 else -1)
        }
        val compressed = mutableListOf<Int>()
        for (s in signs) if (compressed.isEmpty() || compressed.last() != s) compressed.add(s)
        val alternationOk = compressed.size in 2..4
        val confidence = (if (alternationOk) 0.7f else 0f) + (1f - sensitivity) * 0.3f
        return confidence >= 0.6f
    }
}
