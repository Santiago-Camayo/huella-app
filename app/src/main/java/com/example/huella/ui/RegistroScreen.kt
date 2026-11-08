package com.example.huella.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistroScreen(auth: AuthViewModel, alRegistrar: () -> Unit, volver: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Crear cuenta", volver)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Campo("Nombre", nombre, { nombre = it }, "Tu nombre")
                Campo("Correo", correo, { correo = it }, "correo@ejemplo.com", KeyboardType.Email)
                Campo("Teléfono", telefono, { telefono = it }, "300 000 0000", KeyboardType.Phone)
                Campo("Contraseña", password, { password = it }, "Mínimo 8 caracteres", KeyboardType.Password, esClave = true)
                Campo("Confirmar contraseña", confirmacion, { confirmacion = it }, "Repite la contraseña", KeyboardType.Password, esClave = true)
            }

            auth.error?.let {
                Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))
            Boton(
                if (auth.cargando) "Creando cuenta..." else "Crear cuenta",
                {
                    when {
                        nombre.isBlank() || correo.isBlank() -> auth.error = "Escribe tu nombre y tu correo"
                        password.length < 8 -> auth.error = "La contraseña debe tener al menos 8 caracteres"
                        password != confirmacion -> auth.error = "Las contraseñas no coinciden"
                        else -> auth.registrar(nombre, correo, telefono, password, alRegistrar)
                    }
                },
                habilitado = !auth.cargando,
            )
        }
    }
}

// HU-05: registra la fundación y la cuenta de su administrador en un solo formulario
@Composable
fun RegistroFundacionScreen(auth: AuthViewModel, alRegistrar: () -> Unit, volver: () -> Unit) {
    var fundacion by rememberSaveable { mutableStateOf("") }
    var ciudad by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Crear cuenta - Fundación", volver)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Datos de la fundación", style = MaterialTheme.typography.titleMedium)
                Campo("Nombre de la fundación", fundacion, { fundacion = it }, "Nombre de la fundación")
                Campo("Ciudad", ciudad, { ciudad = it }, "Ciudad")
                Campo("Teléfono", telefono, { telefono = it }, "300 000 0000", KeyboardType.Phone)
                Text("Cuenta del administrador", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleMedium)
                Campo("Nombre", nombre, { nombre = it }, "Tu nombre")
                Campo("Correo del administrador", correo, { correo = it }, "correo@fundacion.org", KeyboardType.Email)
                Campo("Contraseña", password, { password = it }, "Mínimo 8 caracteres", KeyboardType.Password, esClave = true)
                Campo("Confirmar contraseña", confirmacion, { confirmacion = it }, "Repite la contraseña", KeyboardType.Password, esClave = true)
            }

            auth.error?.let {
                Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))
            Boton(
                if (auth.cargando) "Registrando..." else "Registrar fundación",
                {
                    when {
                        listOf(fundacion, ciudad, nombre, correo).any { it.isBlank() } -> auth.error = "Completa todos los campos obligatorios"
                        password.length < 8 -> auth.error = "La contraseña debe tener al menos 8 caracteres"
                        password != confirmacion -> auth.error = "Las contraseñas no coinciden"
                        else -> auth.registrarFundacion(fundacion, ciudad, telefono, nombre, correo, password, alRegistrar)
                    }
                },
                habilitado = !auth.cargando,
            )
        }
    }
}
