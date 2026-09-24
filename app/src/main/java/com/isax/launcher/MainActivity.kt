package com.isax.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.core.Prefs
import com.isax.launcher.gesture.SGestureDetector
import com.isax.launcher.home.AppInfo
import com.isax.launcher.home.AppRepository
import com.isax.launcher.quest.QuestRepository
import com.isax.launcher.skills.SkillCategory
import com.isax.launcher.skills.SkillRegistry
import com.isax.launcher.system.SystemStats
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.theme.IsaxTheme
import com.isax.launcher.ui.*
import com.isax.launcher.window.FreeformLauncher
import kotlinx.coroutines.delay

/**
 * Point d'entrée du launcher (HOME).
 *
 * Gestes SAO (issus de Sword Art Online), tous dérivés du MÊME flux d'événements :
 *  - glisser vers le BAS  -> SaoHubMenu (Terminal, Fichiers, Corbeille, Skills…)
 *  - glisser vers le HAUT -> QuestBoard (système de quêtes Solo Leveling)
 *  - tracé du « S »       -> SystemStatusWindow (stats + notifications + quêtes)
 *  - appui long sur une app -> WindowBoard (fenêtres déplaçables / freeform)
 */
class MainActivity : ComponentActivity() {

    private enum class Overlay {
        NONE, SAO_HUB, SYSTEM_STATUS, TERMINAL, FILES, TRASH, SKILLS, WINDOWS, QUESTS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        setContent {
            var overlay by remember { mutableStateOf(Overlay.NONE) }
            var apps by remember { mutableStateOf(AppRepository.loadAll(this)) }
            var showWall by remember { mutableStateOf(false) }

            IsaxTheme(accent = androidx.compose.ui.graphics.Color(Prefs.accentColor)) {
                Box(Modifier.fillMaxSize().background(IsaxColors.Deep)) {
                    HomeScreen(
                        ctx = this,
                        apps = apps,
                        onGesture = { r ->
                            overlay = when (r) {
                                SGestureDetector.Result.SWIPE_DOWN -> Overlay.SAO_HUB
                                SGestureDetector.Result.SWIPE_UP -> Overlay.QUESTS
                                SGestureDetector.Result.S_SHAPE -> Overlay.SYSTEM_STATUS
                                else -> overlay
                            }
                        },
                        header = { HeaderBar() },
                        onAppClick = { app -> FreeformLauncher.launch(this, app) },
                        onAppLongPress = { app ->
                            com.isax.launcher.window.WindowSession.add(app, 1080, 1920)
                            overlay = Overlay.WINDOWS
                        }
                    )

                    when (overlay) {
                        Overlay.SAO_HUB -> SaoHubMenu(
                            ctx = this,
                            onDismiss = { overlay = Overlay.NONE },
                            onOpenTerminal = { overlay = Overlay.TERMINAL },
                            onOpenFiles = { overlay = Overlay.FILES },
                            onOpenTrash = { overlay = Overlay.TRASH },
                            onOpenSkills = { overlay = Overlay.SKILLS },
                            onOpenSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                            onOpenWindows = { overlay = Overlay.WINDOWS }
                        )
                        Overlay.SYSTEM_STATUS -> SystemStatusWindow(this) { overlay = Overlay.NONE }
                        Overlay.TERMINAL -> TerminalOverlay(this) { overlay = Overlay.NONE }
                        Overlay.FILES -> FileManagerOverlay { overlay = Overlay.NONE }
                        Overlay.TRASH -> TrashOverlay { overlay = Overlay.NONE }
                        Overlay.SKILLS -> SkillStoreOverlay(this) { overlay = Overlay.NONE }
                        Overlay.WINDOWS -> WindowBoard(this, { overlay = Overlay.NONE }, apps)
                        Overlay.QUESTS -> QuestPanel(this) { overlay = Overlay.NONE }
                        Overlay.NONE -> {}
                    }
                }
            }
        }
    }

    /** Bandeau supérieur : horloge, XP de chasseur, widgets de compétences. */
    @Composable
    private fun HeaderBar() {
        val widgets = SkillRegistry.byCategory(SkillCategory.WIDGET)
        var stat by remember { mutableStateOf(SystemStats.sample(this)) }
        LaunchedEffect(Unit) { while (true) { stat = SystemStats.sample(this); delay(3000) } }
        Column(Modifier.fillMaxWidth().background(IsaxColors.Deep.copy(alpha = 0.85f)).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ISAX", color = IsaxColors.Cyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("${QuestRepository.totalXp()} XP", color = IsaxColors.Violet, fontSize = 13.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "HP ${(stat.batteryPct * 100).toInt()}%  •  MP ${(stat.ramUsedPct * 100).toInt()}%  •  AGI ${stat.cpuPct.toInt()}%",
                color = IsaxColors.Text.copy(alpha = 0.7f), fontSize = 11.sp
            )
            if (widgets.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(widgets) { sk ->
                        Box(
                            Modifier.background(IsaxColors.Deep2)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) { sk.Render(this@MainActivity) }
                    }
                }
            }
        }
    }
}
