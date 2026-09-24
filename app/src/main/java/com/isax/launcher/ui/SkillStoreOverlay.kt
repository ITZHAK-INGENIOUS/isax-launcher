package com.isax.launcher.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.skills.IsaxSkill
import com.isax.launcher.skills.SkillCategory
import com.isax.launcher.skills.SkillInstaller
import com.isax.launcher.skills.SkillRegistry
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader

/** Inventaire des compétences + import d'un pack .isaxskill (ou manifest JSON collé). */
@Composable
fun SkillStoreOverlay(ctx: Context, onDismiss: () -> Unit) {
    var version by remember { mutableStateOf(0) }
    val skills = remember(version) { SkillRegistry.all() }
    var status by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(IsaxColors.Glass), contentAlignment = Alignment.Center) {
        NeonPanel(Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.8f)) {
            PanelHeader("COMPÉTENCES", IsaxColors.Cyan, onDismiss)
            Text("$skills compétence(s) active(s)", color = IsaxColors.Text.copy(alpha = 0.5f), fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(skills) { sk: IsaxSkill -> SkillCard(sk) }
            }
            if (status.isNotBlank()) Text(status, color = IsaxColors.Violet, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "+ Importer un pack (manifest JSON)",
                color = IsaxColors.Cyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    val sample = """{"id":"demo-pack","name":"Pack Démo","version":"1.0.0","category":"THEME","description":"Pack d'exemple"}"""
                    val ok = SkillInstaller.installFromJson(ctx, sample)
                    status = if (ok) "Pack installé — relance Isax pour l'activer." else "Échec."
                    version++
                }
            )
        }
    }
}

@Composable
private fun SkillCard(sk: IsaxSkill) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, sk.accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(sk.displayName, color = sk.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Text("v${sk.version}", color = IsaxColors.Text.copy(alpha = 0.4f), fontSize = 10.sp)
        }
        Text("${sk.category} — ${sk.description}", color = IsaxColors.Text.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}
