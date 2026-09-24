package com.isax.launcher.core

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Persistance JSON légère (pas de base de données : le launcher reste minimal). */
object JsonStore {
    fun write(file: File, array: JSONArray) {
        file.parentFile?.mkdirs()
        file.writeText(array.toString())
    }

    fun write(file: File, obj: JSONObject) {
        file.parentFile?.mkdirs()
        file.writeText(obj.toString())
    }

    fun readArray(file: File): JSONArray =
        if (file.exists() && file.length() > 0) JSONArray(file.readText()) else JSONArray()

    fun readObject(file: File): JSONObject =
        if (file.exists() && file.length() > 0) JSONObject(file.readText()) else JSONObject()
}
