package com.isax.launcher.skills.builtin

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.isax.launcher.skills.IsaxSkill
import com.isax.launcher.skills.SkillCategory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Widget Horloge SAO : heure + date, rafraîchi chaque seconde. */
class ClockSkill : IsaxSkill {
    override val id = "clock-sao"
    override val displayName = "Horloge SAO"
    override val version = "1.0.0"
    override val category = SkillCategory.WIDGET
    override val description = "Horloge néon flottante, style interface SAO."
    override val accent = Color(0xFF00F0FF)

    @Composable
    override fun Render(context: Context) {
        var now by remember { mutableStateOf(Date()) }
        LaunchedEffect(Unit) { while (true) { now = Date(); delay(1000) } }
        val hh = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        val dd = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(now)
        Column {
            Text(hh, color = accent)
            Text(dd, color = Color.White.copy(alpha = 0.7f))
        }
    }
}
