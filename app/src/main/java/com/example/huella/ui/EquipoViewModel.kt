package com.example.huella.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huella.data.ActivoRequest
import com.example.huella.data.Api
import com.example.huella.data.CrearUsuarioRequest
import com.example.huella.data.Resultado
import com.example.huella.data.Usuario
import com.example.huella.data.llamar
import kotlinx.coroutines.launch

// HU-24: equipo de la fundación. Solo lo usa el administrador; la API responde 403 a los demás.
class EquipoViewModel : ViewModel() {
    var equipo by mutableStateOf<Resultado<List<Usuario>>?>(null)
        private set
    var enviando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)

    fun cargar() {
        viewModelScope.launch {
            equipo = null
            equipo = llamar { Api.servicio.equipo() }
        }
    }

    // Crea el empleado con contraseña temporal: deberá cambiarla en su primer ingreso (RN-22)
    fun agregar(nombre: String, correo: String, password: String, alExito: () -> Unit) = enviarCambio(alExito) {
        llamar { Api.servicio.crearEmpleado(CrearUsuarioRequest(nombre.trim(), correo.trim(), password)) }
    }

    fun cambiarActivo(id: Long, activo: Boolean) = enviarCambio({}) {
        llamar { Api.servicio.cambiarActivo(id, ActivoRequest(activo)) }
    }

    // Lo común: evita el doble envío, guarda el error y, si salió bien, vuelve a pedir la lista
    private fun enviarCambio(alExito: () -> Unit, peticion: suspend () -> Resultado<Usuario>) {
        if (enviando) return
        viewModelScope.launch {
            enviando = true
            error = null
            when (val r = peticion()) {
                is Resultado.Ok -> {
                    alExito()
                    cargar()
                }
                is Resultado.Error -> error = r.mensaje
            }
            enviando = false
        }
    }
}
