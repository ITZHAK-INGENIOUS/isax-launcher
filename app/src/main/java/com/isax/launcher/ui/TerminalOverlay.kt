package com.isax.launcher.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.terminal.MinishellBridge
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader
import kotlinx.coroutines.flow.onEach

/** Terminal intégré : parle au binaire natif `minishell` (NDK). */
@Composable
fun TerminalOverlay(ctx: Context, onDismiss: () -> Unit) {
    val bridge = remember { MinishellBridge(ctx) }
    val lines = remember { mutableStateListOf("[Isax] minishell prêt. Tape `help`.") }
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        bridge.start()
        bridge.output.onEach { lines.add(it) }.collect {}
    }
    DisposableEffect(Unit) { onDispose { bridge.stop() } }
    LaunchedEffect(lines.size) { listState.animateScrollToItem((lines.size - 1).coerceAtLeast(0)) }

    Box(Modifier.fillMaxSize().background(IsaxColors.Glass), contentAlignment = Alignment.Center) {
        NeonPanel(Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.82f)) {
            PanelHeader("TERMINAL", IsaxColors.Cyan, onDismiss)
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState) {
                items(lines) { line ->
                    Text(line, color = IsaxColors.Cyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace, color = IsaxColors.Text, fontSize = 13.sp
                    )
                )
                TextButton(onClick = {
                    if (input.isNotBlank()) {
                        lines.add("\$ $input")
                        bridge.sendCommand(input)
                        input = ""
                    }
                }) { Text("ENVOYER", color = IsaxColors.Cyan, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
