package com.isax.launcher.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.quest.Quest
import com.isax.launcher.quest.QuestRepository
import com.isax.launcher.system.NotificationBus
import com.isax.launcher.system.SystemStats
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader
import com.isax.launcher.ui.components.StatBar
import kotlinx.coroutines.delay

/** Fenêtre système Solo Leveling (tracé du « S ») : stats, notifications, quêtes. */
@Composable
fun SystemStatusWindow(ctx: Context, onDismiss: () -> Unit) {
    var stats by remember { mutableStateOf(SystemStats.sample(ctx)) }
    val notifs by NotificationBus.items.collectAsState()
    val quests by QuestRepository.quests.collectAsState()
    LaunchedEffect(Unit) { while (true) { stats = SystemStats.sample(ctx); delay(2000) } }

    Box(Modifier.fillMaxSize().background(IsaxColors.Glass), contentAlignment = Alignment.Center) {
        NeonPanel(
            Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f).verticalScroll(rememberScrollState())
        ) {
            PanelHeader("STATUS WINDOW", IsaxColors.Cyan, onDismiss)
            Spacer(Modifier.height(12.dp))
            Text("Chasseur — ${QuestRepository.totalXp()} XP", color = IsaxColors.Violet, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            StatBar("HP / Batterie", stats.batteryPct, IsaxColors.Cyan)
            StatBar("MP / RAM", stats.ramUsedPct, IsaxColors.Violet)
            StatBar("AGI / CPU", stats.cpuPct, IsaxColors.Cyan)
            Spacer(Modifier.height(16.dp))
            Text("QUÊTES ACTIVES", color = IsaxColors.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            val active = quests.filter { !it.done }
            if (active.isEmpty()) Text("Aucune quête.", color = IsaxColors.Text.copy(alpha = 0.5f), fontSize = 12.sp)
            active.take(5).forEach { q: Quest ->
                Text("• ${q.title}  (+${q.xp} XP)", color = IsaxColors.Text.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("NOTIFICATIONS", color = IsaxColors.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            if (notifs.isEmpty()) Text("Flux vide (autorise l'accès aux notifications dans les Réglages Isax).",
                color = IsaxColors.Text.copy(alpha = 0.5f), fontSize = 11.sp)
            notifs.take(8).forEach { n ->
                Text("• [${n.packageName}] ${n.title}", color = IsaxColors.Text.copy(alpha = 0.85f), fontSize = 11.sp, maxLines = 1)
                if (n.text.isNotBlank()) Text("   ${n.text}", color = IsaxColors.Text.copy(alpha = 0.5f), fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}
