package com.isax.launcher.system

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings

/**
 * Contrôle des radios : Wi-Fi, Bluetooth, données mobiles.
 *
 * Android réserve la bascule « silencieuse » de certaines radios aux applis
 * système. On tente donc toujours la bascule directe, puis — si le système la
 * refuse — on ouvre le panneau de réglages correspondant (`Settings.Panel`,
 * identique au centre de contrôle rapide). L'UI affiche laquelle des deux voies
 * a été empruntée, plutôt que de faire semblant.
 */
object ConnectivityController {

    enum class Path { DIRECT, SYSTEM_PANEL, BLOCKED }

    data class Outcome(val ok: Boolean, val path: Path)

    // ─────────────────────────── Wi-Fi ───────────────────────────

    fun wifiEnabled(ctx: Context): Boolean = runCatching {
        wifi(ctx)?.isWifiEnabled == true
    }.getOrDefault(false)

    fun toggleWifi(ctx: Context): Outcome {
        val target = !wifiEnabled(ctx)
        val accepted = runCatching {
            @Suppress("DEPRECATION")
            wifi(ctx)?.setWifiEnabled(target) == true
        }.getOrDefault(false)

        // setWifiEnabled() renvoie toujours false depuis Android 10 : on vérifie
        // l'état réel avant de décider si l'on doit renvoyer l'utilisateur au panneau.
        return if (accepted && wifiEnabled(ctx) == target) {
            Outcome(true, Path.DIRECT)
        } else {
            openInternetPanel(ctx)
            Outcome(false, Path.SYSTEM_PANEL)
        }
    }

    // ───────────────────────── Bluetooth ─────────────────────────

    fun bluetoothEnabled(ctx: Context): Boolean = runCatching {
        adapter(ctx)?.isEnabled == true
    }.getOrDefault(false)

    fun toggleBluetooth(ctx: Context): Outcome {
        val target = !bluetoothEnabled(ctx)
        val accepted = runCatching {
            val a = adapter(ctx) ?: return@runCatching false
            @Suppress("DEPRECATION")
            if (target) a.enable() else a.disable()
        }.getOrDefault(false)

        return if (accepted) Outcome(true, Path.DIRECT)
        else {
            openBluetoothPanel(ctx)
            Outcome(false, Path.SYSTEM_PANEL)
        }
    }

    // ──────────────────────── Données mobiles ────────────────────────
    // Aucune API publique ne permet la bascule : on lit l'état et on délègue
    // toujours au panneau système.

    fun mobileDataActive(ctx: Context): Boolean = runCatching {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return@runCatching false
        val caps = cm.getNetworkCapabilities(net) ?: return@runCatching false
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }.getOrDefault(false)

    fun openMobileData(ctx: Context): Outcome {
        openInternetPanel(ctx)
        return Outcome(false, Path.SYSTEM_PANEL)
    }

    // ───────────────────────── Accès système ─────────────────────────
    private const val ACTION_BLUETOOTH = "android.settings.BLUETOOTH_SETTINGS"

    fun openInternetPanel(ctx: Context) = panel(ctx, Settings.Panel.ACTION_INTERNET_CONNECTIVITY)

    fun openWifiPanel(ctx: Context) = panel(ctx, Settings.Panel.ACTION_WIFI)

    fun openBluetoothPanel(ctx: Context) = panel(ctx, ACTION_BLUETOOTH)

    /**
     * `Settings.Panel` n'existe qu'à partir d'Android 10 ; avant, on retombe sur
     * les écrans de réglages classiques (présents depuis toujours).
     */
    private fun panel(ctx: Context, action: String) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(action)
        } else {
            when (action) {
                Settings.Panel.ACTION_WIFI -> Intent(Settings.ACTION_WIFI_SETTINGS)
                ACTION_BLUETOOTH -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                else -> Intent(Settings.ACTION_WIRELESS_SETTINGS)
            }
        }
        runCatching { ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    private fun wifi(ctx: Context): WifiManager? =
        ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private fun adapter(ctx: Context): BluetoothAdapter? = runCatching {
        (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }.getOrNull()
}
