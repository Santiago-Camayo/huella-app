package com.example.huella.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huella.ui.theme.Borde
import com.example.huella.ui.theme.Deshabilitado
import com.example.huella.ui.theme.Foco
import com.example.huella.ui.theme.Linea
import com.example.huella.ui.theme.Neutro
import com.example.huella.ui.theme.Primario
import com.example.huella.ui.theme.TextoPrincipal
import com.example.huella.ui.theme.TextoSecundario

// Piezas reutilizables del diseño de Figma.

enum class EstiloBoton { Primario, Secundario, Neutro }

@Composable
fun Boton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    estilo: EstiloBoton = EstiloBoton.Primario,
    habilitado: Boolean = true,
) {
    val (fondo, letra) = when (estilo) {
        EstiloBoton.Primario -> Primario to Color.White
        EstiloBoton.Secundario -> Color.White to Primario
        EstiloBoton.Neutro -> Neutro to TextoPrincipal
    }
    Button(
        onClick,
        modifier.fillMaxWidth().height(48.dp),
        enabled = habilitado,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = fondo, contentColor = letra,
            disabledContainerColor = Deshabilitado, disabledContentColor = TextoSecundario,
        ),
        border = if (estilo == EstiloBoton.Secundario && habilitado) BorderStroke(2.dp, Primario) else null,
    ) { Text(texto, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) }
}

// Campo con la etiqueta arriba, como en el diseño
@Composable
fun Campo(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    placeholder: String = "",
    teclado: KeyboardType = KeyboardType.Text,
    esClave: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(
            valor, alCambiar, Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextoSecundario) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Foco, unfocusedBorderColor = Borde,
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            ),
        )
    }
}

@Composable
fun Chip(texto: String, activo: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (activo) Primario else Color.White,
        contentColor = if (activo) Color.White else TextoPrincipal,
        border = BorderStroke(1.5.dp, if (activo) Primario else Borde),
        modifier = Modifier.height(36.dp),
    ) {
        Box(Modifier.padding(horizontal = 14.dp), Alignment.Center) {
            Text(texto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Estado del animal: disponible (verde), apartado (ámbar) o adoptado (azul)
@Composable
fun Insignia(estado: String) {
    val (fondo, letra, icono, nombre) = when (estado) {
        "disponible" -> Insig(Color(0xFFDCFCE7), Color(0xFF166534), "✓", "Disponible")
        "apartado" -> Insig(Color(0xFFFEF3C7), Color(0xFF92400E), "◔", "Apartado")
        else -> Insig(Color(0xFFDBEAFE), Color(0xFF1E40AF), "♥", "Adoptado")
    }
    Text(
        "$icono $nombre",
        Modifier.background(fondo, CircleShape).padding(horizontal = 10.dp, vertical = 2.dp),
        color = letra, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
    )
}

private data class Insig(val fondo: Color, val letra: Color, val icono: String, val nombre: String)

@Composable
fun BarraSuperior(titulo: String, volver: (() -> Unit)? = null) {
    Surface(color = Color.White) {
        Column {
            Box(Modifier.fillMaxWidth().height(56.dp), Alignment.Center) {
                if (volver != null) {
                    Text(
                        "‹", Modifier.align(Alignment.CenterStart).size(44.dp).clickable(onClick = volver),
                        fontSize = 34.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                Text(titulo, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(color = Linea)
        }
    }
}

// Barra de abajo. Por ahora solo Inicio y Perfil; las demás pestañas llegan en los siguientes sprints.
@Composable
fun BarraInferior(actual: String?, ir: (String) -> Unit) {
    Surface(color = Color.White) {
        Column {
            HorizontalDivider(color = Linea)
            Row(Modifier.fillMaxWidth().height(68.dp)) {
                listOf(Triple("inicio", "⌂", "Inicio"), Triple("perfil", "♙", "Perfil")).forEach { (ruta, icono, nombre) ->
                    val color = if (actual == ruta) Primario else TextoSecundario
                    Column(
                        Modifier.weight(1f).height(68.dp).clickable { ir(ruta) },
                        Arrangement.Center, Alignment.CenterHorizontally,
                    ) {
                        Text(icono, fontSize = 20.sp, color = color)
                        Text(nombre, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = color)
                    }
                }
            }
        }
    }
}
