package com.isax.launcher.terminal

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Pont vers le binaire natif `minishell` (src/main/cpp, compilé par le NDK).
 *
 * Android n'accorde pas de pseudo-terminal (PTY) réel sans root ; on relie donc
 * stdin/stdout par des pipes bufferisées et le rendu « terminal » (curseur,
 * couleurs) est simulé côté Compose. Le binaire est installé comme
 * `libminishell.so` dans nativeLibraryDir, ce qui autorise son exécution sous
 * Android 10+ (là où /data/data est monté noexec).
 */
class MinishellBridge(private val context: Context) {

    private var process: Process? = null
    private var writer: OutputStreamWriter? = null
    private val out = Channel<String>(Channel.UNLIMITED)
    private val scope = CoroutineScope(Dispatchers.IO)

    val output: Flow<String> get() = out.receiveAsFlow()

    fun binaryPath(): String =
        File(context.applicationInfo.nativeLibraryDir, "libminishell.so").absolutePath

    fun start(homeDir: File = context.filesDir) {
        if (process != null) return
        val pb = ProcessBuilder(binaryPath()).directory(homeDir).redirectErrorStream(true)
        pb.environment()["HOME"] = homeDir.absolutePath
        pb.environment()["ISAX_TRASH"] = File(homeDir, ".isax_trash").absolutePath
        val proc = pb.start()
        process = proc
        writer = OutputStreamWriter(proc.outputStream)
        scope.launch {
            BufferedReader(InputStreamReader(proc.inputStream)).forEachLine { out.trySend(it) }
            out.trySend("[processus terminé]")
        }
    }

    fun sendCommand(cmd: String) {
        writer?.apply { write(cmd); write("\n"); flush() }
    }

    fun stop() {
        runCatching { writer?.close() }
        process?.destroy()
        process = null
    }
}
