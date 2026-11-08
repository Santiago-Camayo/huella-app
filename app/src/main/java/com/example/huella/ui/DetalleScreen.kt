package com.example.huella.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huella.data.Animal
import com.example.huella.data.Resultado
import com.example.huella.ui.theme.Fondo
import com.example.huella.ui.theme.Linea
import com.example.huella.ui.theme.Peligro
import com.example.huella.ui.theme.TextoSecundario
import com.example.huella.ui.theme.Verde

// HU-08: detalle de un animal.
@Composable
fun DetalleScreen(vm: AnimalesViewModel, id: Long, volver: () -> Unit, adoptar: () -> Unit) {
    LaunchedEffect(id) { vm.cargarDetalle(id) }

    when (val d = vm.detalle) {
        is Resultado.Ok -> Contenido(d.dato, volver, adoptar)
        else -> Column(Modifier.fillMaxSize()) {
            BarraSuperior("Detalle", volver)
            if (d is Resultado.Error) ErrorConReintento(d.mensaje) { vm.cargarDetalle(id) } else Cargando()
        }
    }
}

@Composable
private fun Contenido(a: Animal, volver: () -> Unit, adoptar: () -> Unit) {
    val retirado = !a.activo // la fundación retiró la publicación
    val disponible = a.estado == "disponible"

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Portada(a, volver)

            // Panel que sube un poco sobre la imagen, como en el diseño
            Surface(
                Modifier.fillMaxWidth().offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Fondo,
            ) {
                if (retirado) {
                    Column(Modifier.fillMaxWidth().height(400.dp)) {
                        Mensaje("🐾", "Publicación no disponible", "Este animal ya no recibe solicitudes.")
                    }
                } else {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(a.nombre, style = MaterialTheme.typography.headlineMedium)
                            Insignia(a.estado)
                        }
                        Text(a.datosTexto(), Modifier.padding(top = 4.dp), fontSize = 14.sp, color = TextoSecundario)
                        Text("${a.fundacionNombre} · ${a.fundacionCiudad}", fontSize = 14.sp, color = TextoSecundario)

                        Text("Sobre ${a.nombre}", Modifier.padding(top = 24.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium)
                        if (!a.descripcion.isNullOrBlank()) Text(a.descripcion)
                        if (!a.personalidad.isNullOrBlank()) {
                            Text("Personalidad: ${a.personalidad}", Modifier.padding(top = 8.dp), fontSize = 14.sp, color = TextoSecundario)
                        }

                        Text("Compatibilidad", Modifier.padding(top = 24.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium)
                        Compatibilidad("Con niños", a.buenoConNinos)
                        Compatibilidad("Con perros", a.buenoConPerros)
                        Compatibilidad("Con gatos", a.buenoConGatos)
                    }
                }
            }
        }

        // Botón fijo abajo: solo se puede solicitar si el animal está disponible (HU-09)
        if (!retirado) {
            BarraFija {
                Boton(if (disponible) "Quiero adoptarlo" else "No recibe solicitudes", adoptar, habilitado = disponible)
            }
        }
    }
}

// Imagen grande del animal (emoji sobre su color) con el botón de volver flotante
@Composable
fun Portada(a: Animal, volver: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(260.dp)) {
        ImagenAnimal(a, 110, Modifier.fillMaxSize())
        Box(
            Modifier.align(Alignment.TopStart).padding(14.dp).size(44.dp)
                .background(Color.White.copy(alpha = 0.84f), CircleShape).clickable(onClick = volver),
            Alignment.Center,
        ) { Text("‹", fontSize = 34.sp) }
    }
}

@Composable
private fun Compatibilidad(etiqueta: String, valor: Boolean?) {
    Column {
        Row(Modifier.fillMaxWidth().height(44.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(etiqueta)
            when (valor) {
                true -> Text("✓ Sí", color = Verde, fontWeight = FontWeight.Bold)
                false -> Text("✕ No", color = Peligro, fontWeight = FontWeight.Bold)
                null -> Text("Sin dato", color = TextoSecundario, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(color = Linea)
    }
}
