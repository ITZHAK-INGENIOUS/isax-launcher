package com.isax.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.core.Prefs
import com.isax.launcher.gesture.SGestureDetector
import com.isax.launcher.home.AppInfo
import com.isax.launcher.home.AppRepository
import com.isax.launcher.quest.QuestRepository
import com.isax.launcher.quest.QuestScheduler
import com.isax.launcher.skills.SkillCategory
import com.isax.launcher.skills.SkillRegistry
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.theme.IsaxTheme
import com.isax.launcher.ui.*
import com.isax.launcher.ui.anim.IsaxMotion
import kotlinx.coroutines.delay

/**
 * Point d'entrée du launcher (HOME) — surface HUD.
 *
 * Architecture conservée à l'identique : les gestes SAO alimentent le même
 * détecteur unique, les overlays existants (SAO Hub, fenêtres système, fichiers,
 * corbeille, compétences, quêtes) sont inchangés, et le pont natif `minishell`
 * reste la seule couche basse.
 *
 * Ajouts de cette version :
 *  - HUD reproduisant le croquis (horloge/jauges · TO-DO · Notifs · shell · dock) ;
 *  - shell repliable : il s'étend vers le haut, la partie haute se translate ;
 *  - raccourcis vivants : horloge → Horloge, Réseau → radios, CPU → CPU,
 *    Stockage → Stockage, appui sur une notif → l'application concernée ;
 *  - animation de lancement (SplashOverlay) ;
 *  - tiroir d'applications (alphabétique + recherche + récemment utilisés).
 */
class MainActivity : ComponentActivity() {

    private enum class Overlay {
        NONE, SAO_HUB, SYSTEM_STATUS, TERMINAL, FILES, TRASH, SKILLS, WINDOWS, QUESTS,
        APP_DRAWER, CLOCK, NETWORK, CPU_PANEL, STORAGE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        setContent {
            val activity = this@MainActivity
            var overlay by remember { mutableStateOf(Overlay.NONE) }
            var apps by remember { mutableStateOf(AppRepository.loadAll(activity)) }
            var shellOpen by remember { mutableStateOf(false) }
            var splash by remember { mutableStateOf(true) }

            val quests by QuestRepository.quests.collectAsState()

            IsaxTheme(accent = Color(Prefs.accentColor)) {
                Box(Modifier.fillMaxSize().background(IsaxColors.Deep)) {

                    val detector = remember { SGestureDetector(Prefs.sSensitivity) }
                    val pad = Prefs.swipeThreshold.toFloat()

                    // Progression du déploiement du shell : anime poids, translation
                    // et opacité de la partie haute d'une seule valeur.
                    val shellProg by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (shellOpen) 1f else 0f,
                        animationSpec = IsaxMotion.soft(),
                        label = "shell-progress"
                    )
                    val shellFrac = 0.15f + 0.45f * shellProg

                    Column(
                        Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { detector.reset() },
                                    onDragEnd = {
                                        overlay = when (detector.classify(pad)) {
                                            SGestureDetector.Result.SWIPE_DOWN -> Overlay.SAO_HUB
                                            SGestureDetector.Result.SWIPE_UP -> Overlay.QUESTS
                                            SGestureDetector.Result.S_SHAPE -> Overlay.SYSTEM_STATUS
                                            else -> overlay
                                        }
                                    },
                                    onDragCancel = { detector.reset() },
                                    onDrag = { change, _ ->
                                        detector.addPoint(change.position.x, change.position.y, change.uptimeMillis)
                                    }
                                )
                            }
                    ) {
                        // ── Partie haute : se translate vers le haut quand le shell s'ouvre ──
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight((1f - shellFrac).coerceAtLeast(0.15f))
                                .clipToBounds()
                        ) {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationY = -size.height * 0.20f * shellProg
                                        alpha = 1f - 0.30f * shellProg
                                    }
                            ) {
                                HudHeader(
                                    xp = QuestRepository.totalXp(),
                                    onClock = { overlay = Overlay.CLOCK },
                                    onBattery = {
                                        runCatching {
                                            activity.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY))
                                        }
                                    },
                                    onNetwork = { overlay = Overlay.NETWORK },
                                    onCpu = { overlay = Overlay.CPU_PANEL },
                                    onStorage = { overlay = Overlay.STORAGE }
                                )
                                SkillStrip()
                                Spacer(Modifier.height(12.dp))
                                HudPanels(
                                    ctx = activity,
                                    todoQuests = quests,
                                    onToggleTodo = { id -> QuestRepository.toggleDone(id) },
                                    onDeleteTodo = { id ->
                                        QuestScheduler.cancel(activity, id)
                                        QuestRepository.remove(id)
                                    },
                                    onAddTodo = { overlay = Overlay.QUESTS },
                                    onOpenApp = { pkg -> AppRepository.launchPackage(activity, pkg) }
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        }

                        // ── Shell : replié en une ligne, se déploie vers le haut ──
                        ShellPanel(
                            ctx = activity,
                            expanded = shellOpen,
                            onToggle = { shellOpen = !shellOpen },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(shellFrac)
                        )

                        // ── Dock : inchangé, toujours visible ──
                        HudDock(
                            ctx = activity,
                            onOpenApps = { overlay = Overlay.APP_DRAWER },
                            shellExpanded = shellOpen
                        )
                    }

                    when (overlay) {
                        Overlay.SAO_HUB -> SaoHubMenu(
                            ctx = activity,
                            onDismiss = { overlay = Overlay.NONE },
                            onOpenTerminal = { overlay = Overlay.TERMINAL },
                            onOpenFiles = { overlay = Overlay.FILES },
                            onOpenTrash = { overlay = Overlay.TRASH },
                            onOpenSkills = { overlay = Overlay.SKILLS },
                            onOpenSettings = { activity.startActivity(Intent(activity, SettingsActivity::class.java)) },
                            onOpenWindows = { overlay = Overlay.WINDOWS }
                        )
                        Overlay.SYSTEM_STATUS -> SystemStatusWindow(activity) { overlay = Overlay.NONE }
                        Overlay.TERMINAL -> TerminalOverlay(activity) { overlay = Overlay.NONE }
                        Overlay.FILES -> FileManagerOverlay { overlay = Overlay.NONE }
                        Overlay.TRASH -> TrashOverlay { overlay = Overlay.NONE }
                        Overlay.SKILLS -> SkillStoreOverlay(activity) { overlay = Overlay.NONE }
                        Overlay.WINDOWS -> WindowBoard(activity, { overlay = Overlay.NONE }, apps)
                        Overlay.QUESTS -> QuestPanel(activity) { overlay = Overlay.NONE }
                        Overlay.APP_DRAWER -> AppDrawerOverlay(
                            ctx = activity,
                            apps = apps,
                            onDismiss = { overlay = Overlay.NONE },
                            onLaunch = { app -> AppRepository.launch(activity, app) },
                            onLongPress = { app ->
                                com.isax.launcher.window.WindowSession.add(app, 1080, 1920)
                                overlay = Overlay.WINDOWS
                            }
                        )
                        Overlay.CLOCK -> ClockWindow { overlay = Overlay.NONE }
                        Overlay.NETWORK -> NetworkWindow(activity) { overlay = Overlay.NONE }
                        Overlay.CPU_PANEL -> CpuWindow(activity) { overlay = Overlay.NONE }
                        Overlay.STORAGE -> StorageWindow(
                            ctx = activity,
                            onDismiss = { overlay = Overlay.NONE },
                            onOpenFiles = { overlay = Overlay.FILES }
                        )
                        Overlay.NONE -> {}
                    }

                    // Rafraîchit l'inventaire quand des apps sont installées/désinstallées.
                    LaunchedEffect(Unit) {
                        while (true) {
                            apps = AppRepository.loadAll(activity)
                            delay(30_000)
                        }
                    }
                }
            }

            if (splash) SplashOverlay { splash = false }
        }
    }

    /** Bandeau des compétences de catégorie WIDGET — architecture préservée. */
    @Composable
    private fun SkillStrip() {
        val widgets = SkillRegistry.byCategory(SkillCategory.WIDGET)
        if (widgets.isEmpty()) return
        LazyRow(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(widgets) { sk ->
                Row(
                    Modifier
                        .background(IsaxColors.Deep2)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    sk.Render(this@MainActivity)
                }
            }
        }
    }
}
