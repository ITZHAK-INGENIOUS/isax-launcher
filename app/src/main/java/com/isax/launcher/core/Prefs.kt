package com.isax.launcher.core

import android.content.Context
import android.content.SharedPreferences

/** Réglages persistants du launcher (gestes, thème, sensibilité). */
object Prefs {
    private lateinit var sp: SharedPreferences

    var sSensitivity: Float
        get() = sp.getFloat("s_sensitivity", 0.65f)
        set(v) = sp.edit().putFloat("s_sensitivity", v).apply()

    var swipeThreshold: Int
        get() = sp.getInt("swipe_threshold", 28)
        set(v) = sp.edit().putInt("swipe_threshold", v).apply()

    var accentColor: Int
        get() = sp.getInt("accent", 0xFF00F0FF.toInt())
        set(v) = sp.edit().putInt("accent", v).apply()

    var dynamicTheme: Boolean
        get() = sp.getBoolean("dynamic_theme", true)
        set(v) = sp.edit().putBoolean("dynamic_theme", v).apply()

    var wallpaperDim: Float
        get() = sp.getFloat("wallpaper_dim", 0.35f)
        set(v) = sp.edit().putFloat("wallpaper_dim", v).apply()

    var freeformEnabled: Boolean
        get() = sp.getBoolean("freeform", true)
        set(v) = sp.edit().putBoolean("freeform", v).apply()

    var hapticsOn: Boolean
        get() = sp.getBoolean("haptics", true)
        set(v) = sp.edit().putBoolean("haptics", v).apply()

    fun init(ctx: Context) { sp = ctx.getSharedPreferences("isax_prefs", Context.MODE_PRIVATE) }
}
