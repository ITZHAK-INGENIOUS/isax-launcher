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
import com.isax.launcher.system.SystemStats
import kotlinx.coroutines.delay

/** Widget Vitalité : HP (batterie), MP (RAM), AGI (CPU). */
class SystemPulseSkill : IsaxSkill {
    override val id = "system-pulse"
    override val displayName = "Vitalité système"
    override val version = "1.0.0"
    override val category = SkillCategory.WIDGET
    override val description = "Surveille batterie, RAM et charge CPU."
    override val accent = Color(0xFF7A5CFF)

    @Composable
    override fun Render(context: Context) {
        var s by remember { mutableStateOf(SystemStats.sample(context)) }
        LaunchedEffect(Unit) { while (true) { s = SystemStats.sample(context); delay(2000) } }
        Column {
            Text("HP ${(s.batteryPct * 100).toInt()}%", color = accent)
            Text("MP ${(s.ramUsedPct * 100).toInt()}%", color = Color.White.copy(alpha = 0.8f))
            Text("AGI ${(s.cpuPct).toInt()}%", color = Color.White.copy(alpha = 0.6f))
        }
    }
}
