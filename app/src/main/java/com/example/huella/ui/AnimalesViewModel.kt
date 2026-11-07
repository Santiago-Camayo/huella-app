package com.example.huella.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huella.data.Animal
import com.example.huella.data.Api
import com.example.huella.data.Resultado
import com.example.huella.data.llamar
import kotlinx.coroutines.launch

// Datos de las pantallas de animales. null significa "cargando".
class AnimalesViewModel : ViewModel() {
    var lista by mutableStateOf<Resultado<List<Animal>>?>(null)
        private set
    var detalle by mutableStateOf<Resultado<Animal>?>(null)
        private set
    var deMiFundacion by mutableStateOf<Resultado<List<Animal>>?>(null)
        private set

    // HU-16: con el token de una fundación, la API devuelve solo sus animales, también los retirados
    fun cargarDeMiFundacion() {
        viewModelScope.launch {
            deMiFundacion = null
            deMiFundacion = llamar { Api.servicio.animales(null) }
        }
    }

    // HU-06: solo los disponibles (la API ya excluye los retirados)
    fun cargarLista() {
        viewModelScope.launch {
            lista = null
            lista = llamar { Api.servicio.animales("disponible") }
        }
    }

    // HU-08
    fun cargarDetalle(id: Long) {
        viewModelScope.launch {
            detalle = null
            detalle = llamar { Api.servicio.animal(id) }
        }
    }
}
