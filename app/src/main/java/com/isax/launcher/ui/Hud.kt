package com.isax.launcher.ui

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.isax.launcher.home.AppRepository
import com.isax.launcher.home.UsageStore
import com.isax.launcher.quest.Quest
import com.isax.launcher.system.ConnectivityController
import com.isax.launcher.system.NotificationBus
import com.isax.launcher.system.StorageStats
import com.isax.launcher.system.SystemStats
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.anim.IsaxMotion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Le HUD — transposition directe du croquis.
 *
 *   ┌ horloge · secondes · marque ISAX │ batterie / CPU / stockage / réseau
 *   ├ TO-DO (+)                        │ Notifs (application + nombre)
 *   ├ >|  (shell replié → se déploie vers le haut)
 *   └ dock : téléphone · messages · applications
 *
 * Chaque valeur est vivante : les jauges rejoignent leur cible en ressort, la
 * seconde défile, les listes s'allongent en douceur à l'insertion.
 */

// ───────────────────────────── en-tête ─────────────────────────────

@Composable
fun HudHeader(
    xp: Int,
    onClock: () -> Unit,
    onBattery: () -> Unit,
    onNetwork: () -> Unit,
    onCpu: () -> Unit,
    onStorage: () -> Unit
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var stats by remember { mutableStateOf(SystemStats.sample(ctx)) }
    var storage by remember { mutableStateOf(StorageStats.sample()) }
    var wifiOn by remember { mutableStateOf(ConnectivityController.wifiEnabled(ctx)) }
    var btOn by remember { mutableStateOf(ConnectivityController.bluetoothEnabled(ctx)) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            stats = SystemStats.sample(ctx)
            storage = StorageStats.sample()
            wifiOn = ConnectivityController.wifiEnabled(ctx)
            btOn = ConnectivityController.bluetoothEnabled(ctx)
            kotlinx.coroutines.delay(4000)
        }
    }

    val clock = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))
    val seconds = SimpleDateFormat("ss", Locale.getDefault()).format(Date(now))
    val network = wifiOn || btOn

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ClockBlock(clock, seconds, xp, onClock, Modifier.weight(0.76f))
        Column(Modifier.weight(1.24f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            StatRow("Batterie", stats.batteryPct, "${(stats.batteryPct * 100).toInt()}%",
                IsaxColors.Cyan, onBattery) { p ->
                BatteryGlyph(p, IsaxColors.Cyan, Modifier.size(width = 26.dp, height = 13.dp))
            }
            StatRow("CPU", stats.cpuPct, "${stats.cpuPct.toInt()}%",
                IsaxColors.Violet, onCpu) { p ->
                CpuGlyph(p, IsaxColors.Violet, Modifier.size(14.dp))
            }
            StatRow("Stockage", storage.usedPct, "${(storage.usedPct * 100).toInt()}%",
                IsaxColors.Cyan, onStorage) { p ->
                StorageGlyph(p, IsaxColors.Cyan, Modifier.size(width = 15.dp, height = 14.dp))
            }
            StatRow(
                if (network) "Réseau" else "Réseau —",
                if (network) 1f else 0.28f,
                if (network) "actif" else "coupé",
                if (network) IsaxColors.Cyan else IsaxColors.Text.copy(alpha = 0.4f),
                onNetwork
            ) { p ->
                NetworkGlyph(
                    p,
                    if (network) IsaxColors.Cyan else IsaxColors.Text.copy(alpha = 0.4f),
                    Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun ClockBlock(
    clock: String,
    seconds: String,
    xp: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                clock,
                color = IsaxColors.Text,
                fontSize = 34.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                seconds,
                color = IsaxColors.Violet,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text("ISAX", color = IsaxColors.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) {
                Box(
                    Modifier.width(14.dp).height(1.5.dp)
                        .background(IsaxColors.Cyan.copy(alpha = 0.55f), RoundedCornerShape(1.dp))
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("$xp XP", color = IsaxColors.Violet.copy(alpha = 0.85f), fontSize = 10.sp)
    }
}

/** Une ligne de stat : glyphe · libellé · conduite pointillée · valeur. */
@Composable
private fun StatRow(
    label: String,
    value: Float,
    display: String,
    accent: Color,
    onClick: () -> Unit,
    glyph: @Composable (Float) -> Unit
) {
    val animated by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = IsaxMotion.firm(),
        label = "stat-$label"
    )
    Row(
        Modifier.fillMaxWidth().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        glyph(animated)
        Text(label, color = IsaxColors.Text.copy(alpha = 0.72f), fontSize = 10.5.sp, maxLines = 1)
        DottedLeader(IsaxColors.Text.copy(alpha = 0.18f), Modifier.weight(1f))
        Text(display, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ──────────────────────── TO-DO / Notifications ────────────────────────

@Composable
fun HudPanels(
    ctx: Context,
    todoQuests: List<Quest>,
    onToggleTodo: (String) -> Unit,
    onDeleteTodo: (String) -> Unit,
    onAddTodo: () -> Unit,
    onOpenApp: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TodoBlock(
            quests = todoQuests,
            onToggle = onToggleTodo,
            onDelete = onDeleteTodo,
            onAdd = onAddTodo,
            modifier = Modifier.weight(1f)
        )
        NotifBlock(ctx = ctx, onOpenApp = onOpenApp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TodoBlock(
    quests: List<Quest>,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Les 3 tâches les PLUS RÉCENTES, la plus neuve en tête.
    val shown = remember(quests) { quests.sortedByDescending { it.createdAt }.take(3) }

    Column(modifier.animateContentSize(animationSpec = IsaxMotion.soft())) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("TO-DO", color = IsaxColors.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.weight(1f))
            Text(
                "+",
                color = IsaxColors.Cyan,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(IsaxColors.Cyan.copy(alpha = 0.14f), RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAdd
                    )
                    .padding(horizontal = 8.dp, vertical = 1.dp)
            )
        }
        Spacer(Modifier.height(3.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(IsaxColors.Cyan.copy(alpha = 0.28f)))
        Spacer(Modifier.height(6.dp))

        if (shown.isEmpty()) {
            Text(
                "aucune quête",
                color = IsaxColors.Text.copy(alpha = 0.35f),
                fontSize = 10.5.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        shown.forEach { q ->
            key(q.id) {
                var entered by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { entered = true }
                val presence by animateFloatAsState(
                    targetValue = if (entered) 1f else 0f,
                    animationSpec = IsaxMotion.fluid(IsaxMotion.Base),
                    label = "todo-row"
                )
                Row(
                    Modifier.fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onToggle(q.id) }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    CheckBoxGlyph(checked = q.done, accent = IsaxColors.Cyan, modifier = Modifier.size(13.dp))
                    Text(
                        q.title,
                        color = IsaxColors.Text.copy(alpha = 0.40f + 0.60f * presence),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "✕",
                        color = IsaxColors.Text.copy(alpha = 0.28f),
                        fontSize = 11.sp,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onDelete(q.id) }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun NotifBlock(
    ctx: Context,
    onOpenApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notifs by NotificationBus.items.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    // Regroupement par application : c'est le « Nombre de Notifications » du croquis.
    val grouped = remember(notifs) {
        notifs.groupBy { it.packageName }.entries
            .map { it.key to it.value }
            .sortedByDescending { it.second.size }
    }
    val visible = if (expanded) grouped else grouped.take(2)

    Column(modifier.animateContentSize(animationSpec = IsaxMotion.soft())) {
        Text("Notifs", color = IsaxColors.Violet, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(3.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(IsaxColors.Violet.copy(alpha = 0.28f)))
        Spacer(Modifier.height(6.dp))

        if (grouped.isEmpty()) {
            Text(
                "flux vide",
                color = IsaxColors.Text.copy(alpha = 0.35f),
                fontSize = 10.5.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        visible.forEach { (pkg, items) ->
            Row(
                Modifier.fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onOpenApp(pkg) }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                AppBadge(ctx, pkg, 16.dp)
                Text(
                    appLabel(ctx, pkg),
                    color = IsaxColors.Text.copy(alpha = 0.72f),
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                DottedLeader(IsaxColors.Text.copy(alpha = 0.18f), Modifier.width(14.dp))
                Text("${items.size}", color = IsaxColors.Violet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // « … ↑ » : déploie le reste du flux sans changer d'écran.
        if (grouped.size > 2) {
            Row(
                Modifier.fillMaxWidth()
                    .padding(top = 2.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { expanded = !expanded }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("…", color = IsaxColors.Text.copy(alpha = 0.45f), fontSize = 12.sp)
                Text(
                    "↑",
                    color = IsaxColors.Violet,
                    fontSize = 11.sp,
                    modifier = Modifier.rotate(if (expanded) 180f else 0f)
                )
            }
        }
    }
}

/** Icône d'application mise en cache (Drawable système → Bitmap). */
@Composable
fun AppBadge(ctx: Context, pkg: String, size: androidx.compose.ui.unit.Dp) {
    val icon = remember(pkg) {
        AppRepository.icon(ctx, pkg)?.let { d ->
            runCatching { d.toBitmap(96, 96).asImageBitmap() }.getOrNull()
        }
    }
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = Modifier.size(size).clip(RoundedCornerShape(size / 4f))
        )
    } else {
        Box(
            Modifier.size(size).clip(RoundedCornerShape(size / 4f))
                .background(IsaxColors.Violet.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                appLabel(ctx, pkg).take(1).uppercase(),
                color = IsaxColors.Violet,
                fontSize = (size.value * 0.55f).sp
            )
        }
    }
}

private fun appLabel(ctx: Context, pkg: String): String = runCatching {
    val pm = ctx.packageManager
    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
}.getOrDefault(pkg.substringAfterLast('.'))

// ───────────────────────────── dock ─────────────────────────────

/**
 * Barre basse du croquis : téléphone, messages, applications.
 * Les deux premières ouvrent l'appli système réelle ; la troisième, le tiroir.
 */
@Composable
fun HudDock(
    ctx: Context,
    onOpenApps: () -> Unit,
    shellExpanded: Boolean
) {
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(1.dp)
                .background(IsaxColors.Text.copy(alpha = 0.10f))
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockGlyph("✆", "Téléphone") { LaunchIntent.dialer(ctx) }
            DockGlyph("✉", "Messages") { LaunchIntent.sms(ctx) }
            DockGlyph("❖", "Applications", onClick = onOpenApps)
        }
        if (shellExpanded) {
            Text(
                "shell déployé — les touches sont au-dessus du dock",
                color = ShellHintColor,
                fontSize = 8.5.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun DockGlyph(glyph: String, label: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = IsaxMotion.soft(),
        label = "dock-$label"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { pressed = true; onClick() }
            )
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(glyph, color = IsaxColors.Cyan, fontSize = 20.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, color = IsaxColors.Text.copy(alpha = 0.45f), fontSize = 8.5.sp, maxLines = 1)
    }
    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(180)
            pressed = false
        }
    }
}

/** Repli de secours : sans historique d'usage, on garde l'ordre alphabétique. */
internal fun fallbackRecents(apps: List<String>): List<String> =
    if (UsageStore.lastUsed.value.isEmpty()) apps.take(12) else UsageStore.recentPackages(12)
