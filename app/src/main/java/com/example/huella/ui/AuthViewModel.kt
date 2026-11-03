package com.example.huella.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huella.data.Api
import com.example.huella.data.AuthResponse
import com.example.huella.data.LoginRequest
import com.example.huella.data.RegistroRequest
import com.example.huella.data.Resultado
import com.example.huella.data.Usuario
import com.example.huella.data.llamar
import kotlinx.coroutines.launch
import retrofit2.Response

// Estado de la sesión: HU-01 (registro), HU-02 (login), HU-03 (recordar sesión) y HU-04 (cerrar).
class AuthViewModel : ViewModel() {
    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)

    fun login(correo: String, password: String, alExito: () -> Unit) = enviar(alExito) {
        Api.servicio.login(LoginRequest(correo.trim(), password))
    }

    fun registrar(nombre: String, correo: String, telefono: String, password: String, alExito: () -> Unit) =
        enviar(alExito) {
            Api.servicio.registrar(
                RegistroRequest(nombre.trim(), correo.trim(), telefono.trim().ifBlank { null }, password)
            )
        }

    // Guarda el token y el usuario si la API acepta; si no, deja el mensaje de error para la pantalla
    private fun enviar(alExito: () -> Unit, peticion: suspend () -> Response<AuthResponse>) {
        viewModelScope.launch {
            cargando = true
            error = null
            when (val r = llamar(peticion)) {
                is Resultado.Ok -> {
                    Api.sesion.token = r.dato.token
                    usuario = r.dato.usuario
                    alExito()
                }
                is Resultado.Error -> error = r.mensaje
            }
            cargando = false
        }
    }

    // Al abrir la app: si hay token guardado, GET /usuarios/me dice si sigue siendo válido
    suspend fun restaurarSesion(): Boolean {
        if (Api.sesion.token != null) {
            when (val r = llamar { Api.servicio.me() }) {
                is Resultado.Ok -> usuario = r.dato
                is Resultado.Error -> Api.sesion.cerrar()
            }
        }
        return usuario != null
    }

    fun cerrarSesion() {
        Api.sesion.cerrar()
        usuario = null
    }
}
