package com.example.huella.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huella.data.Api
import com.example.huella.data.EstadoRequest
import com.example.huella.data.Resultado
import com.example.huella.data.Resumen
import com.example.huella.data.Solicitud
import com.example.huella.data.SolicitudRequest
import com.example.huella.data.llamar
import kotlinx.coroutines.launch

// Solicitudes de adopción, para el adoptante y para la fundación. null significa "cargando".
class SolicitudesViewModel : ViewModel() {
    var lista by mutableStateOf<Resultado<List<Solicitud>>?>(null)
        private set
    var refrescando by mutableStateOf(false)
        private set
    var detalle by mutableStateOf<Resultado<Solicitud>?>(null)
        private set
    var ultimaEnviada by mutableStateOf<Solicitud?>(null)
        private set
    var enviando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)

    // HU-10 y HU-21. Al refrescar (deslizar hacia abajo) se deja la lista visible mientras carga.
    fun cargarLista(orden: String?, refrescar: Boolean = false) {
        viewModelScope.launch {
            if (refrescar) refrescando = true else lista = null
            lista = llamar { Api.servicio.solicitudes(orden, null) }
            refrescando = false
        }
    }

    // HU-15: panel de la fundación
    var resumen by mutableStateOf<Resultado<Resumen>?>(null)
        private set

    fun cargarResumen() {
        viewModelScope.launch {
            resumen = null
            resumen = llamar { Api.servicio.resumen() }
        }
    }

    // Solicitudes de un solo animal (detalle del animal en el módulo de la fundación)
    var delAnimal by mutableStateOf<Resultado<List<Solicitud>>?>(null)
        private set

    fun cargarDelAnimal(animalId: Long) {
        viewModelScope.launch {
            delAnimal = null
            delAnimal = llamar { Api.servicio.solicitudes("antiguas", animalId) }
        }
    }

    fun cargarDetalle(id: Long) {
        viewModelScope.launch {
            detalle = null
            detalle = llamar { Api.servicio.solicitud(id) }
        }
    }

    // HU-09. "enviando" evita el doble envío
    fun enviar(animalId: Long, comentario: String, alExito: () -> Unit) {
        if (enviando) return
        viewModelScope.launch {
            enviando = true
            error = null
            when (val r = llamar { Api.servicio.crearSolicitud(SolicitudRequest(animalId, comentario.trim())) }) {
                is Resultado.Ok -> {
                    ultimaEnviada = r.dato
                    alExito()
                }
                is Resultado.Error -> error = r.mensaje
            }
            enviando = false
        }
    }

    // HU-12 (cancelar) y HU-22 (revisar, aprobar, rechazar)
    fun cambiarEstado(id: Long, estado: String, alExito: () -> Unit = {}) {
        if (enviando) return
        viewModelScope.launch {
            enviando = true
            error = null
            when (val r = llamar { Api.servicio.cambiarEstado(id, EstadoRequest(estado)) }) {
                is Resultado.Ok -> {
                    detalle = r
                    alExito()
                }
                is Resultado.Error -> error = r.mensaje
            }
            enviando = false
        }
    }
}
