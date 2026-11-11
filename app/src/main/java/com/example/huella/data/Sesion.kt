package com.example.huella.data

import android.content.Context

// Guarda en el teléfono el token (HU-03) y los favoritos del adoptante (HU-13).
// Cerrar sesión borra las dos cosas (HU-04).
class Sesion(context: Context) {
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(valor) = prefs.edit().putString("token", valor).apply()

    // Ids de los animales favoritos (SharedPreferences guarda conjuntos de texto)
    var favoritos: Set<Long>
        get() = prefs.getStringSet("favoritos", emptySet())!!.map { it.toLong() }.toSet()
        set(ids) = prefs.edit().putStringSet("favoritos", ids.map { it.toString() }.toSet()).apply()

    fun cerrar() = prefs.edit().clear().apply()
}
