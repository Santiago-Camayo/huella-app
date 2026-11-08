package com.example.huella.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huella.ui.theme.Primario
import com.example.huella.ui.theme.TextoSecundario

@Composable
fun LoginScreen(auth: AuthViewModel, alEntrar: () -> Unit, irARegistro: () -> Unit, irARegistroFundacion: () -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 32.dp)) {
        Text("🐾", Modifier.fillMaxWidth(), fontSize = 56.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(
            "Bienvenido", Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
            style = MaterialTheme.typography.headlineMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Campo("Correo", correo, { correo = it }, "correo@ejemplo.com", KeyboardType.Email)
            Campo("Contraseña", password, { password = it }, "Tu contraseña", KeyboardType.Password, esClave = true)
        }

        auth.error?.let {
            Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))
        Boton(
            if (auth.cargando) "Entrando..." else "Iniciar sesión",
            {
                if (correo.isBlank() || password.isBlank()) auth.error = "Escribe tu correo y tu contraseña"
                else auth.login(correo, password, alEntrar)
            },
            habilitado = !auth.cargando,
        )

        Row(Modifier.fillMaxWidth().padding(top = 24.dp), Arrangement.Center, Alignment.CenterVertically) {
            Text("¿No tienes cuenta? ", fontSize = 12.sp, color = TextoSecundario)
            Text(
                "Crear cuenta", Modifier.clickable(onClick = irARegistro).padding(vertical = 12.dp),
                fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Primario,
            )
        }
        Text(
            "Registrar fundación",
            Modifier.fillMaxWidth().clickable(onClick = irARegistroFundacion).padding(vertical = 12.dp),
            fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Primario,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
