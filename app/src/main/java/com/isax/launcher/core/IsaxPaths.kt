package com.isax.launcher.core

import android.content.Context
import java.io.File

/** Arborescence de travail d'Isax, calquée sur un home Unix minimal. */
object IsaxPaths {
    lateinit var root: File
        private set
    lateinit var home: File
        private set
    lateinit var trash: File
        private set
    lateinit var skills: File
        private set
    lateinit var widgets: File
        private set

    fun ensure(ctx: Context) {
        root = File(ctx.filesDir, "isax")
        home = File(root, "home")
        trash = File(root, ".trash")
        skills = File(root, "skills")
        widgets = File(root, "widgets")
        listOf(root, home, trash, skills, widgets).forEach { if (!it.exists()) it.mkdirs() }
    }
}
