package com.isax.launcher.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Chemins d'accès aux réglages Android — demandé explicitement (« les chemins
 * pour avoir accès aux applications installées et aux paramètres du téléphone »).
 * Chaque entrée ouvre l'écran système correspondant via une Intent officielle.
 */
object DeviceSettings {

    data class Link(val label: String, val intent: Intent)

    fun all(ctx: Context): List<Link> = listOf(
        link("Applications installées", Intent(Settings.ACTION_APPLICATION_SETTINGS)),
        link("Applications par défaut (launcher)", Intent(Settings.ACTION_HOME_SETTINGS)),
        link("Accès aux notifications", nlIntent(ctx)),
        link("Accès à l'utilisation (stats)", Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)),
        link("Affichage & thème", Intent(Settings.ACTION_DISPLAY_SETTINGS)),
        link("Fond d'écran", Intent(Intent.ACTION_SET_WALLPAPER)),
        link("Batterie", Intent(Intent.ACTION_POWER_USAGE_SUMMARY)),
        link("Options développeur", Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)),
        link("Stockage", Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)),
        link("Wi-Fi", Intent(Settings.ACTION_WIFI_SETTINGS)),
        link("Sons & vibrations", Intent(Settings.ACTION_SOUND_SETTINGS)),
        link("Détails de l'app Isax", Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:" + ctx.packageName)))
    )

    private fun link(label: String, i: Intent) = Link(label, i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

    private fun nlIntent(ctx: Context) =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    ComponentName(ctx, IsaxNotificationListener::class.java).flattenToString())
        } else {
            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        }

    fun open(ctx: Context, link: Link) = runCatching { ctx.startActivity(link.intent) }
}
