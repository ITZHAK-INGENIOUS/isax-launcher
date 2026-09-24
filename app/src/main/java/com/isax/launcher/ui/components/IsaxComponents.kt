package com.isax.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.theme.IsaxColors

/** Panneau vitré néon réutilisable (base de tous les overlays). */
@Composable
fun NeonPanel(
    modifier: Modifier = Modifier,
    accent: Color = IsaxColors.Cyan,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .background(IsaxColors.Deep.copy(alpha = 0.92f))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun PanelHeader(title: String, accent: Color = IsaxColors.Cyan, onClose: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (onClose != null) IconButton(onClick = onClose) { Icon(Icons.Filled.Close, null, tint = accent) }
    }
}

/** Barre de statistique type « fiche de personnage ». */
@Composable
fun StatBar(label: String, value: Float, accent: Color = IsaxColors.Cyan, suffix: String = "") {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = IsaxColors.Text, fontSize = 12.sp)
            Text("${(value * 100).toInt()}%$suffix", color = accent, fontSize = 12.sp)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp)).background(accent)
            )
        }
    }
}
