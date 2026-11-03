package com.example.huella.data

import android.content.Context

// Guarda el token en el teléfono para recordar la sesión (HU-03) y lo borra al cerrarla (HU-04).
class Sesion(context: Context) {
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(valor) = prefs.edit().putString("token", valor).apply()

    fun cerrar() = prefs.edit().clear().apply()
}
