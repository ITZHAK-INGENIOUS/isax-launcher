package com.isax.launcher.ui

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.home.AppInfo
import com.isax.launcher.home.UsageStore
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.anim.IsaxMotion

/**
 * Tiroir d'applications : rangement alphabétique, recherche instantanée, et
 * « récemment utilisés » remontés en tête (signalés par une pastille violette).
 *
 * Ordre : paquets déjà lancés (du plus récent au plus ancien), puis le reste par
 * ordre alphabétique. La recherche porte sur le libellé comme sur le nom de
 * paquet, insensible à la casse et aux accents.
 */
@Composable
fun AppDrawerOverlay(
    ctx: Context,
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onLongPress: (AppInfo) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val usage by UsageStore.lastUsed.collectAsState()

    val ordered = remember(apps, usage, query) {
        val recents = UsageStore.recentPackages(64)
        val key = query.trim().lowercase().fold("") { acc, c -> acc + stripAccent(c) }

        val filtered = if (key.isEmpty()) apps else apps.filter { a ->
            a.label.lowercase().fold("") { acc, c -> acc + stripAccent(c) }.contains(key) ||
                a.packageName.lowercase().contains(key)
        }

        filtered.sortedWith(
            compareBy(
                { if (recents.contains(it.packageName)) 0 else 1 },
                { recents.indexOf(it.packageName).takeIf { i -> i >= 0 } ?: Int.MAX_VALUE },
                { it.label.lowercase() }
            )
        )
    }

    val grouped = ordered.groupBy { it.label.firstOrNull()?.uppercase() ?: "#" }

    Box(Modifier.fillMaxSize().background(IsaxColors.Deep.copy(alpha = 0.96f))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "APPLICATIONS",
                    color = IsaxColors.Cyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "FERMER",
                    color = IsaxColors.Text.copy(alpha = 0.55f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(IsaxColors.Text.copy(alpha = 0.07f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        "Rechercher une application",
                        color = IsaxColors.Text.copy(alpha = 0.35f),
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = IsaxColors.Text, fontSize = 13.sp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    query.isNotBlank() -> "${ordered.size} résultat(s)"
                    usage.isNotEmpty() -> "${ordered.size} applications — récents en haut"
                    else -> "${ordered.size} applications — tri alphabétique"
                },
                color = IsaxColors.Text.copy(alpha = 0.4f),
                fontSize = 10.sp
            )

            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                grouped.forEach { (initial, group) ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            initial,
                            color = IsaxColors.Violet.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(group, key = { it.packageName + it.activityName }) { app ->
                        DrawerTile(
                            ctx = ctx,
                            app = app,
                            recent = usage.containsKey(app.packageName),
                            onLaunch = { onLaunch(app) },
                            onLongPress = { onLongPress(app) }
                        )
                    }
                }
            }
        }
    }
}

/** Normalisation d'un caractère accentué (évite toute dépendance externe). */
private fun stripAccent(c: Char): Char = when (c) {
    'à', 'â', 'ä', 'á', 'ã' -> 'a'
    'é', 'è', 'ê', 'ë' -> 'e'
    'î', 'ï', 'í' -> 'i'
    'ô', 'ö', 'ó', 'õ' -> 'o'
    'ù', 'û', 'ü', 'ú' -> 'u'
    'ç' -> 'c'
    'ñ' -> 'n'
    else -> c
}

@Composable
private fun DrawerTile(
    ctx: Context,
    app: AppInfo,
    recent: Boolean,
    onLaunch: () -> Unit,
    onLongPress: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = IsaxMotion.soft(),
        label = "tile-${app.packageName}"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { pressed = true; onLaunch() }
            )
            .padding(vertical = 2.dp)
    ) {
        Box {
            AppBadge(ctx, app.packageName, 46.dp)
            if (recent) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(IsaxColors.Violet)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            app.label,
            color = IsaxColors.Text.copy(alpha = 0.9f),
            fontSize = 9.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(180)
            pressed = false
        }
    }
}
