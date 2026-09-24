package com.isax.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isax.launcher.core.Prefs
import com.isax.launcher.system.DeviceSettings
import com.isax.launcher.theme.IsaxColors
import com.isax.launcher.theme.IsaxTheme
import com.isax.launcher.ui.components.NeonPanel
import com.isax.launcher.ui.components.PanelHeader

/** Réglages Isax : gestes, apparence, et accès direct aux réglages Android. */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var sens by remember { mutableStateOf(Prefs.sSensitivity) }
            var thr by remember { mutableStateOf(Prefs.swipeThreshold.toFloat()) }
            var free by remember { mutableStateOf(Prefs.freeformEnabled) }
            var dyn by remember { mutableStateOf(Prefs.dynamicTheme) }

            IsaxTheme(accent = androidx.compose.ui.graphics.Color(Prefs.accentColor)) {
                Column(
                    Modifier.fillMaxSize().background(IsaxColors.Deep)
                        .verticalScroll(rememberScrollState()).padding(16.dp)
                ) {
                    NeonPanel(Modifier.fillMaxWidth()) {
                        PanelHeader("RÉGLAGES ISAX", IsaxColors.Cyan)
                        Spacer(Modifier.height(10.dp))
                        Label("Sensibilité du geste « S » : ${"%.2f".format(sens)}")
                        Slider(value = sens, onValueChange = { sens = it; Prefs.sSensitivity = it }, valueRange = 0f..1f)
                        Label("Seuil de swipe : ${thr.toInt()} px")
                        Slider(value = thr, onValueChange = { thr = it; Prefs.swipeThreshold = it.toInt() }, valueRange = 12f..120f)
                        ToggleRow("Fenêtres freeform", free) { free = it; Prefs.freeformEnabled = it }
                        ToggleRow("Thème dynamique", dyn) { dyn = it; Prefs.dynamicTheme = it }
                    }
                    Spacer(Modifier.height(16.dp))
                    NeonPanel(Modifier.fillMaxWidth()) {
                        PanelHeader("ACCÈS RÉGLAGES SYSTÈME", IsaxColors.Violet)
                        Spacer(Modifier.height(8.dp))
                        DeviceSettings.all(this@SettingsActivity).forEach { link ->
                            Text(
                                "→ ${link.label}",
                                color = IsaxColors.Text, fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { DeviceSettings.open(this@SettingsActivity, link) }
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun Label(t: String) = Text(t, color = IsaxColors.Text, fontSize = 13.sp)

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = IsaxColors.Text, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}
