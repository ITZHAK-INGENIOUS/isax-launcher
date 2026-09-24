package com.isax.launcher.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.quest.QuestRepository
import com.isax.launcher.quest.QuestScheduler
import com.isax.launcher.quest.QuestType
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader

/** Panneau de création/gestion des quêtes (tâches programmables). */
@Composable
fun QuestPanel(ctx: Context, onDismiss: () -> Unit) {
    val quests by QuestRepository.quests.collectAsState()
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("8") }
    var minute by remember { mutableStateOf("0") }
    var daily by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(IsaxColors.Glass), contentAlignment = Alignment.Center) {
        NeonPanel(Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.85f)) {
            PanelHeader("QUÊTES — ${QuestRepository.totalXp()} XP", IsaxColors.Violet, onDismiss)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = title, onValueChange = { title = it },
                label = { Text("Titre de la quête") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = hour, onValueChange = { hour = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Heure") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = minute, onValueChange = { minute = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Min") }, modifier = Modifier.weight(1f), singleLine = true)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                Text(if (daily) "↻ Quotidienne" else "① Unique",
                    color = IsaxColors.Cyan, modifier = Modifier.clickable { daily = !daily }, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        val q = QuestRepository.newQuest(
                            title = title,
                            type = if (daily) QuestType.DAILY else QuestType.ONESHOT,
                            hour = hour.toIntOrNull() ?: 8,
                            minute = minute.toIntOrNull() ?: 0,
                            days = if (daily) setOf(2, 3, 4, 5, 6) else emptySet(),
                            xp = 10
                        )
                        QuestRepository.add(q)
                        QuestScheduler.schedule(ctx, q)
                        title = ""
                    }
                }) { Text("AJOUTER + PLANIFIER", color = IsaxColors.Cyan, fontWeight = FontWeight.Bold) }
            }
            // Les plus récentes en tête, comme le TO-DO du HUD.
            LazyQuestList(
                quests.sortedByDescending { it.createdAt }
                    .map { it.id to ("${if (it.done) "✔" else "○"} ${it.title}  ${"%02d".format(it.hour)}:${"%02d".format(it.minute)}  (+${it.xp} XP)") }
            )
            Spacer(Modifier.height(6.dp))
            Text("Toucher une ligne pour la marquer terminée.",
                color = IsaxColors.Text.copy(alpha = 0.4f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun LazyQuestList(rows: List<Pair<String, String>>) {
    androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth()) {
        items(rows) { (id, label) ->
            Text(
                label, color = IsaxColors.Text, fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().clickable { QuestRepository.toggleDone(id) }.padding(vertical = 8.dp)
            )
        }
    }
}
