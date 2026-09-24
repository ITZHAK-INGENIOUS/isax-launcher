package com.isax.launcher.ui

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.core.IsaxPaths
import com.isax.launcher.terminal.MinishellBridge
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.anim.IsaxMotion
import kotlinx.coroutines.flow.onEach

/**
 * Shell d'Isax — la ligne `>|` du croquis.
 *
 * Replié : une seule rangée, caret clignotant, conduite pointillée.
 * Déplié (tap) : le panneau s'étend vers le HAUT (le poids du shell est piloté
 * par MainActivity) ; les touches logicielles — CTRL, SHIFT, ESC, TAB, flèches,
 * retour arrière — apparaissent au-dessus du dock, qui lui ne bouge pas.
 *
 * Le moteur reste le binaire natif `minishell` du projet. S'il est absent
 * (environnement sans NDK), le panneau le signale au lieu de planter.
 */
@Composable
fun ShellPanel(
    ctx: Context,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bridge = remember { MinishellBridge(ctx) }
    val lines = remember { mutableStateListOf("[Isax] minishell prêt. Tape `help`.") }
    var input by remember { mutableStateOf("") }
    var engineOk by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { bridge.start(IsaxPaths.home) }
            .onFailure {
                engineOk = false
                lines.add("[Isax] moteur natif indisponible ici — compilation NDK requise.")
            }
        bridge.output.onEach { lines.add(it) }.collect {}
    }
    DisposableEffect(Unit) { onDispose { bridge.stop() } }
    LaunchedEffect(lines.size) {
        if (expanded) listState.animateScrollToItem((lines.size - 1).coerceAtLeast(0))
    }
    LaunchedEffect(expanded) {
        if (expanded) runCatching { focus.requestFocus() }
    }

    fun submit() {
        if (input.isNotBlank()) {
            lines.add("\$ $input")
            bridge.sendCommand(input)
            input = ""
        }
    }

    val caretFade by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
        animationSpec = IsaxMotion.gentle(),
        label = "shell-caret"
    )

    Column(
        modifier
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(IsaxColors.Deep2.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Rangée de tête : « >| » toujours visible ; un tap bascule le shell.
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(">", color = IsaxColors.Cyan, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            if (!expanded) {
                BlinkingCursor(IsaxColors.Cyan.copy(alpha = 0.2f + 0.8f * caretFade))
                DottedLeader(IsaxColors.Text.copy(alpha = 0.18f), Modifier.weight(1f))
                Text(
                    if (engineOk) "shell" else "shell — moteur absent",
                    color = IsaxColors.Text.copy(alpha = 0.35f),
                    fontSize = 9.5.sp
                )
            } else {
                Text(
                    "SHELL — tap pour replier",
                    color = IsaxColors.Text.copy(alpha = 0.45f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        if (expanded) {
            Spacer(Modifier.height(6.dp))
            LazyColumn(Modifier.fillMaxWidth().weight(1f), state = listState) {
                items(lines) { line ->
                    Text(
                        line,
                        color = IsaxColors.Cyan.copy(alpha = 0.9f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\$", color = IsaxColors.Cyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    singleLine = true,
                    enabled = engineOk,
                    textStyle = TextStyle(
                        color = IsaxColors.Text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    cursorBrush = SolidColor(IsaxColors.Cyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }),
                    modifier = Modifier.weight(1f).focusRequester(focus)
                )
                Text(
                    "ENVOYER",
                    color = IsaxColors.Cyan,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { submit() }
                        )
                        .padding(start = 10.dp, top = 4.dp, bottom = 4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            SoftKeyRow { token -> input += token }
        }
    }
}

/** Touches logicielles : CTRL, SHIFT, ESC, TAB, flèches directionnelles, ⌫. */
@Composable
private fun SoftKeyRow(onKey: (String) -> Unit) {
    val keys = listOf(
        "CTRL" to "CTRL ",
        "SHIFT" to "SHIFT ",
        "ESC" to "\u001b",
        "TAB" to "\t",
        "←" to "\u001b[D",
        "↑" to "\u001b[A",
        "↓" to "\u001b[B",
        "→" to "\u001b[C",
        "⌫" to "\b"
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        keys.forEach { (label, token) ->
            Box(
                Modifier
                    .weight(1f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(IsaxColors.Text.copy(alpha = 0.06f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onKey(token) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = IsaxColors.Text.copy(alpha = 0.7f), fontSize = 8.5.sp, maxLines = 1)
            }
        }
    }
}

/** Teinte du rappel d'état affiché sous le dock quand le shell est déployé. */
internal val ShellHintColor: Color get() = Color(0x66E8F6FF)
