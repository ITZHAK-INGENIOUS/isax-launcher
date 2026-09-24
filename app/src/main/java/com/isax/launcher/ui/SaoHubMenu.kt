package com.isax.launcher.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.skills.SkillRegistry
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader

/** Menu suspendu SAO (glisser vers le BAS) : accès rapide aux outils Isax. */
@Composable
fun SaoHubMenu(
    ctx: Context,
    onDismiss: () -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenFiles: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenSkills: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWindows: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(IsaxColors.Glass),
        contentAlignment = Alignment.TopCenter
    ) {
        NeonPanel(
            Modifier.padding(top = 72.dp).fillMaxWidth(0.86f),
            accent = IsaxColors.Cyan
        ) {
            PanelHeader("SAO HUB", IsaxColors.Cyan, onDismiss)
            Spacer(Modifier.height(8.dp))
            HubRow(">_", "Terminal (minishell)", onOpenTerminal)
            HubRow("▢", "Fenêtres d'apps", onOpenWindows)
            HubRow("🗂", "Fichiers", onOpenFiles)
            HubRow("♻", "Corbeille (.trash)", onOpenTrash)
            HubRow("✦", "Compétences (${SkillRegistry.all().size})", onOpenSkills)
            HubRow("⚙", "Réglages Isax", onOpenSettings)
        }
    }
}

@Composable
private fun HubRow(glyph: String, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(50))
                .background(IsaxColors.Cyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) { Text(glyph, color = IsaxColors.Cyan, fontSize = 16.sp) }
        Text(label, color = IsaxColors.Text, fontSize = 15.sp)
    }
}
