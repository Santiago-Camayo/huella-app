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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huella.ui.theme.TextoSecundario

// Perfil del adoptante. Aquí vive el botón de cerrar sesión (HU-04).
@Composable
fun PerfilScreen(auth: AuthViewModel, alCerrarSesion: () -> Unit) {
    val u = auth.usuario
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
            Boton("Cerrar sesión", alCerrarSesion, estilo = EstiloBoton.Neutro)
        }
    }
}
