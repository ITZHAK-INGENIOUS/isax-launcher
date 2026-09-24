package com.isax.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.core.IsaxPaths
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader
import java.io.File

/** Navigateur de fichiers minimal sur /isax/home, + vue corbeille. */
@Composable
fun FileManagerOverlay(onDismiss: () -> Unit) {
    var dir by remember { mutableStateOf(IsaxPaths.home) }
    var listing by remember { mutableStateOf(dir.listFiles()?.sortedBy { it.name } ?: emptyList()) }

    fun refresh() { listing = dir.listFiles()?.sortedBy { it.name } ?: emptyList() }

    Box(Modifier.fillMaxSize().background(IsaxColors.Glass), contentAlignment = Alignment.Center) {
        NeonPanel(Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.8f)) {
            PanelHeader("FICHIERS — ${dir.name}", IsaxColors.Cyan, onDismiss)
            Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (dir.canonicalPath != IsaxPaths.root.canonicalPath) {
                    Text("↑ ..", color = IsaxColors.Violet, modifier = Modifier.clickable {
                        dir = dir.parentFile ?: dir; refresh()
                    })
                }
                Spacer(Modifier.width(16.dp))
                Text("${listing.size} éléments", color = IsaxColors.Text.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(listing) { f: File ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            if (f.isDirectory) { dir = f; refresh() }
                        }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (f.isDirectory) "📁" else "📄", fontSize = 16.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(f.name, color = IsaxColors.Text, fontSize = 13.sp)
                        Spacer(Modifier.weight(1f))
                        if (!f.isDirectory) Text("${f.length()} o", color = IsaxColors.Text.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/** Corbeille (.trash) : liste et purgera les fichiers mis au rebut. */
@Composable
fun TrashOverlay(onDismiss: () -> Unit) {
    var files by remember { mutableStateOf(IsaxPaths.trash.listFiles()?.toList() ?: emptyList()) }
    Box(Modifier.fillMaxSize().background(IsaxColors.Glass), contentAlignment = Alignment.Center) {
        NeonPanel(Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.7f)) {
            PanelHeader("CORBEILLE", IsaxColors.Violet, onDismiss)
            Spacer(Modifier.height(8.dp))
            if (files.isEmpty()) Text("Vide.", color = IsaxColors.Text.copy(alpha = 0.5f), fontSize = 12.sp)
            LazyColumn(Modifier.weight(1f)) {
                items(files) { f: File ->
                    Row(
                        Modifier.fillMaxWidth().clickable { f.delete(); files = IsaxPaths.trash.listFiles()?.toList() ?: emptyList() }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("♻", color = IsaxColors.Violet)
                        Spacer(Modifier.width(12.dp))
                        Text(f.name, color = IsaxColors.Text, fontSize = 13.sp)
                    }
                }
            }
            Text("Toucher un élément = suppression définitive.",
                color = IsaxColors.Text.copy(alpha = 0.4f), fontSize = 10.sp)
        }
    }
}
