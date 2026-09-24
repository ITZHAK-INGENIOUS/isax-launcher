package com.isax.launcher.ui

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.system.ConnectivityController
import com.isax.launcher.system.StorageStats
import com.isax.launcher.system.SystemStats
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.ui.anim.IsaxMotion
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader
import com.isax.launcher.ui.components.StatBar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Ouverture des écrans système réels demandés par le croquis. */
object LaunchIntent {

    fun dialer(ctx: Context) = open(ctx, android.content.Intent(android.content.Intent.ACTION_DIAL))

    fun sms(ctx: Context) = open(
        ctx,
        android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_APP_MESSAGING)
    )

    private fun open(ctx: Context, i: android.content.Intent) = runCatching {
        ctx.startActivity(i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

// ─────────────────────────── Horloge ───────────────────────────

@Composable
fun ClockWindow(onDismiss: () -> Unit) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))
    val secs = SimpleDateFormat("ss", Locale.getDefault()).format(Date(now))
    val date = SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault()).format(Date(now))

    OverlayFrame(onDismiss) {
        PanelHeader("HORLOGE", IsaxColors.Cyan, onDismiss)
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(time, color = IsaxColors.Text, fontSize = 54.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.width(8.dp))
            Text(
                secs,
                color = IsaxColors.Violet,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Text(date, color = IsaxColors.Text.copy(alpha = 0.6f), fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
        Text(
            "Chaque quête cochée ajoute de l'XP au chasseur : l'horloge et le score avancent ensemble.",
            color = IsaxColors.Text.copy(alpha = 0.45f),
            fontSize = 11.sp
        )
    }
}

// ─────────────────────────── Réseau ───────────────────────────

@Composable
fun NetworkWindow(ctx: Context, onDismiss: () -> Unit) {
    var wifi by remember { mutableStateOf(ConnectivityController.wifiEnabled(ctx)) }
    var bt by remember { mutableStateOf(ConnectivityController.bluetoothEnabled(ctx)) }
    var data by remember { mutableStateOf(ConnectivityController.mobileDataActive(ctx)) }
    var lastPath by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            wifi = ConnectivityController.wifiEnabled(ctx)
            bt = ConnectivityController.bluetoothEnabled(ctx)
            data = ConnectivityController.mobileDataActive(ctx)
            delay(2500)
        }
    }

    OverlayFrame(onDismiss) {
        PanelHeader("RÉSEAU", IsaxColors.Cyan, onDismiss)
        Spacer(Modifier.height(6.dp))
        Text(
            "Bascule directe quand Android l'autorise ; sinon Isax ouvre le panneau système et te dit lequel des deux chemins a été pris.",
            color = IsaxColors.Text.copy(alpha = 0.45f),
            fontSize = 10.5.sp
        )
        Spacer(Modifier.height(14.dp))

        RadioRow("Wi-Fi", wifi) {
            lastPath = describe(ConnectivityController.toggleWifi(ctx).path)
            wifi = ConnectivityController.wifiEnabled(ctx)
        }
        RadioRow("Bluetooth", bt) {
            lastPath = describe(ConnectivityController.toggleBluetooth(ctx).path)
            bt = ConnectivityController.bluetoothEnabled(ctx)
        }
        RadioRow("Données mobiles", data) {
            lastPath = describe(ConnectivityController.openMobileData(ctx).path)
        }

        if (lastPath.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(lastPath, color = IsaxColors.Violet, fontSize = 10.5.sp)
        }
    }
}

private fun describe(path: ConnectivityController.Path): String = when (path) {
    ConnectivityController.Path.DIRECT -> "Bascule appliquée directement."
    ConnectivityController.Path.SYSTEM_PANEL -> "Panneau système ouvert — valide la bascule d'un geste."
    ConnectivityController.Path.BLOCKED -> "Action non autorisée par le système pour cette application."
}

@Composable
private fun RadioRow(label: String, on: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = IsaxColors.Text, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        TogglePill(on = on, onClick = onToggle)
    }
}

// ─────────────────────────── CPU / Stockage ───────────────────────────

@Composable
fun CpuWindow(ctx: Context, onDismiss: () -> Unit) {
    var stats by remember { mutableStateOf(SystemStats.sample(ctx)) }
    val storage = remember { StorageStats.sample() }
    LaunchedEffect(Unit) {
        while (true) {
            stats = SystemStats.sample(ctx)
            delay(1500)
        }
    }

    OverlayFrame(onDismiss) {
        PanelHeader("CPU", IsaxColors.Violet, onDismiss)
        Spacer(Modifier.height(14.dp))
        StatBar("Charge CPU", stats.cpuPct, IsaxColors.Violet)
        StatBar("Mémoire (MP)", stats.ramUsedPct, IsaxColors.Cyan)
        Spacer(Modifier.height(8.dp))
        Text(
            "${stats.ramUsedMb} Mo utilisés / ${stats.ramTotalMb} Mo au total",
            color = IsaxColors.Text.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(18.dp))
        Text("STOCKAGE", color = IsaxColors.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        StatBar("Interne", storage.usedPct, IsaxColors.Cyan)
        Text(
            "${StorageStats.human(storage.usedBytes)} utilisés — ${StorageStats.human(storage.freeBytes)} libres",
            color = IsaxColors.Text.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
    }
}

// ─────────────────────────── Pièces communes ───────────────────────────

@Composable
private fun OverlayFrame(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(IsaxColors.Glass),
        contentAlignment = Alignment.Center
    ) {
        NeonPanel(
            Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.78f).verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
private fun TogglePill(on: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (on) IsaxColors.Cyan.copy(alpha = 0.22f) else IsaxColors.Text.copy(alpha = 0.07f),
        animationSpec = IsaxMotion.gentle(),
        label = "pill-bg"
    )
    val fg by animateColorAsState(
        targetValue = if (on) IsaxColors.Cyan else IsaxColors.Text.copy(alpha = 0.5f),
        animationSpec = IsaxMotion.gentle(),
        label = "pill-fg"
    )
    val dotScale by animateFloatAsState(
        targetValue = if (on) 1f else 0.7f,
        animationSpec = IsaxMotion.soft(),
        label = "pill-dot"
    )
    Row(
        Modifier
            .background(bg, RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(7.dp * dotScale).background(fg, RoundedCornerShape(50)))
        Text(
            if (on) "activé" else "désactivé",
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
