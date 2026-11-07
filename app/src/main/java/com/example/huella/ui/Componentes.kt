package com.example.huella.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

// Campo con la etiqueta arriba, como en el diseño. Con lineas > 1 es un área de texto.
@Composable
fun Campo(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    placeholder: String = "",
    teclado: KeyboardType = KeyboardType.Text,
    esClave: Boolean = false,
    soloLectura: Boolean = false,
    lineas: Int = 1,
) {
    val fondo = if (soloLectura) Color(0xFFF6EEE5) else Color.White
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(
            valor, alCambiar, Modifier.fillMaxWidth(),
            readOnly = soloLectura,
            placeholder = { Text(placeholder, color = TextoSecundario) },
            singleLine = lineas == 1,
            minLines = lineas,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Foco, unfocusedBorderColor = Borde,
                focusedContainerColor = fondo, unfocusedContainerColor = fondo,
                focusedTextColor = if (soloLectura) TextoSecundario else TextoPrincipal,
                unfocusedTextColor = if (soloLectura) TextoSecundario else TextoPrincipal,
            ),
        )
    }
}

// Tarjeta blanca con bordes redondeados (la "box" del diseño)
@Composable
fun Caja(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) { Column(Modifier.padding(16.dp), content = contenido) }
}

// Fila "etiqueta ... valor" con una línea debajo
@Composable
fun FilaDato(etiqueta: String, valor: @Composable () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().height(48.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(etiqueta, fontSize = 14.sp, color = TextoSecundario)
            valor()
        }
        HorizontalDivider(color = Linea)
    }
}

// Zona fija abajo de la pantalla para los botones principales
@Composable
fun BarraFija(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(color = Color.White) {
        Column {
            HorizontalDivider(color = Linea)
            Column(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = contenido,
            )
        }
    }
}

// Diálogo "¿Seguro?" antes de un cambio que no se puede deshacer
@Composable
fun DialogoConfirmacion(titulo: String, texto: String, confirmar: String, alConfirmar: () -> Unit, alCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = alCerrar,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = { Text(titulo, style = MaterialTheme.typography.titleMedium) },
        text = { Text(texto, color = TextoSecundario) },
        confirmButton = {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                Boton("Volver", alCerrar, Modifier.weight(1f), EstiloBoton.Neutro)
                Boton(confirmar, alConfirmar, Modifier.weight(1f))
            }
        },
    )
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

// Nombre para mostrar de un estado de animal o de solicitud
fun nombreEstado(estado: String): String = when (estado) {
    "en_revision" -> "En revisión"
    else -> estado.replaceFirstChar { it.uppercase() }
}

// Estado de un animal o de una solicitud, con el color del diseño
@Composable
fun Insignia(estado: String) {
    val verde = Color(0xFFDCFCE7) to Color(0xFF166534)
    val ambar = Color(0xFFFEF3C7) to Color(0xFF92400E)
    val azul = Color(0xFFDBEAFE) to Color(0xFF1E40AF)
    val (colores, icono) = when (estado) {
        "disponible", "aprobada" -> verde to "✓"
        "apartado", "pendiente" -> ambar to "◔"
        "en_revision" -> ambar to "◉"
        "rechazada" -> (Color(0xFFFEE2E2) to Color(0xFF991B1B)) to "✕"
        "cancelada", "retirado" -> (Color(0xFFE2E8F0) to Color(0xFF475569)) to "—"
        else -> azul to "♥" // adoptado, finalizada
    }
    val (fondo, letra) = colores
    Text(
        "$icono ${nombreEstado(estado)}",
        Modifier.background(fondo, CircleShape).padding(horizontal = 10.dp, vertical = 2.dp),
        color = letra, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1,
    )
}

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

// Una pestaña de la barra de abajo: ruta, icono y nombre
data class Pestana(val ruta: String, val icono: String, val nombre: String)

// Barra de abajo. Las pestañas cambian según el módulo (adoptante o fundación).
@Composable
fun BarraInferior(pestanas: List<Pestana>, actual: String?, ir: (String) -> Unit) {
    Surface(color = Color.White) {
        Column {
            HorizontalDivider(color = Linea)
            Row(Modifier.fillMaxWidth().height(68.dp)) {
                pestanas.forEach { (ruta, icono, nombre) ->
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
