package com.example.huella.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huella.data.Api
import com.example.huella.data.AuthResponse
import com.example.huella.data.DatosAdmin
import com.example.huella.data.DatosFundacion
import com.example.huella.data.LoginRequest
import com.example.huella.data.RegistroFundacionRequest
import com.example.huella.data.RegistroRequest
import com.example.huella.data.Resultado
import com.example.huella.data.Usuario
import com.example.huella.data.llamar
import kotlinx.coroutines.launch
import retrofit2.Response

// Cómo termina la revisión de la sesión al abrir la app
enum class Arranque { CON_SESION, SIN_SESION, SIN_RED }

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

    // HU-05: crea la fundación y su administrador; entra directo con la cuenta nueva
    fun registrarFundacion(
        fundacion: String, ciudad: String, telefono: String,
        nombre: String, correo: String, password: String, alExito: () -> Unit,
    ) = enviar(alExito) {
        Api.servicio.registrarFundacion(
            RegistroFundacionRequest(
                DatosFundacion(fundacion.trim(), ciudad.trim(), telefono.trim().ifBlank { null }),
                DatosAdmin(nombre.trim(), correo.trim(), password),
            )
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

    // HU-03. Al abrir la app: si hay token guardado, GET /usuarios/me dice si sigue siendo válido.
    // Sin red no se borra el token: la app muestra Reintentar.
    suspend fun restaurarSesion(): Arranque {
        if (Api.sesion.token == null) return Arranque.SIN_SESION
        return when (val r = llamar { Api.servicio.me() }) {
            is Resultado.Ok -> {
                usuario = r.dato
                Arranque.CON_SESION
            }
            is Resultado.Error -> {
                if (r.codigo == null) return Arranque.SIN_RED
                Api.sesion.cerrar()
                if (r.codigo == 401) error = "Tu sesión expiró. Inicia sesión de nuevo."
                Arranque.SIN_SESION
            }
        }
    }

    fun cerrarSesion() {
        Api.sesion.cerrar()
        usuario = null
    }
}
