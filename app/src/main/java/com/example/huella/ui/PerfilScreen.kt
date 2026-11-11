package com.example.huella.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huella.ui.theme.TextoSecundario

// HU-14: perfil del adoptante con sus datos y accesos a Mis solicitudes y Favoritos.
// También cambiar contraseña (HU-25) y cerrar sesión (HU-04).
@Composable
fun PerfilScreen(
    auth: AuthViewModel,
    alCerrarSesion: () -> Unit,
    irASolicitudes: () -> Unit,
    irAFavoritos: () -> Unit,
    cambiarPassword: () -> Unit,
) {
    val u = auth.usuario
    var confirmar by remember { mutableStateOf(false) }
    if (confirmar) {
        DialogoConfirmacion(
            "¿Cerrar sesión?", "Tendrás que volver a iniciar sesión para entrar.", "Cerrar sesión",
            alConfirmar = { confirmar = false; alCerrarSesion() },
            alCerrar = { confirmar = false },
        )
    }
    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Perfil")
        Column(Modifier.padding(16.dp), Arrangement.spacedBy(12.dp)) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(u?.nombre ?: "", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        listOfNotNull(u?.correo, u?.telefono).joinToString(" · "),
                        Modifier.padding(top = 4.dp), fontSize = 14.sp, color = TextoSecundario,
                    )
                }
            }
            Boton("Mis solicitudes", irASolicitudes, estilo = EstiloBoton.Secundario)
            Boton("Favoritos", irAFavoritos, estilo = EstiloBoton.Secundario)
            Boton("Cambiar contraseña", cambiarPassword, estilo = EstiloBoton.Secundario)
            Boton("Cerrar sesión", { confirmar = true }, estilo = EstiloBoton.Neutro)
        }
    }
}
