package com.example.huella.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huella.data.Animal
import com.example.huella.data.Resultado
import com.example.huella.ui.theme.Pasteles
import com.example.huella.ui.theme.TextoSecundario

// Inicio (HU-06): animales disponibles, con estados de carga, vacío y error.
@Composable
fun AnimalesScreen(vm: AnimalesViewModel, nombre: String, abrir: (Long) -> Unit) {
    // Al volver desde el detalle no se vuelve a pedir la lista
    LaunchedEffect(Unit) { if (vm.lista !is Resultado.Ok) vm.cargarLista() }
    var filtro by rememberSaveable { mutableStateOf<String?>(null) } // "perro", "gato" o nada

    when (val lista = vm.lista) {
        null -> Cargando()
        is Resultado.Error -> ErrorConReintento(lista.mensaje, vm::cargarLista)
        is Resultado.Ok -> {
            val visibles = lista.dato.filter { filtro == null || it.especie == filtro }
            LazyVerticalGrid(
                GridCells.Fixed(2),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text("Hola, $nombre 👋", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Encuentra a tu nuevo compañero.",
                            Modifier.padding(top = 4.dp), color = TextoSecundario, fontSize = 14.sp,
                        )
                        Row(Modifier.padding(top = 16.dp, bottom = 20.dp), Arrangement.spacedBy(8.dp)) {
                            Chip("Perros", filtro == "perro") { filtro = if (filtro == "perro") null else "perro" }
                            Chip("Gatos", filtro == "gato") { filtro = if (filtro == "gato") null else "gato" }
                        }
                        Text("Disponibles", style = MaterialTheme.typography.titleMedium)
                    }
                }
                if (visibles.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            if (lista.dato.isEmpty()) "No hay animales disponibles por ahora"
                            else "Sin resultados. Prueba con otros filtros.",
                            Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            textAlign = TextAlign.Center, color = TextoSecundario,
                        )
                    }
                }
                items(visibles, key = { it.id }) { TarjetaAnimal(it) { abrir(it.id) } }
            }
        }
    }
}

@Composable
private fun TarjetaAnimal(animal: Animal, alTocar: () -> Unit) {
    Card(
        onClick = alTocar,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(110.dp).background(animal.pastel()), Alignment.Center) {
            Text(animal.emoji(), fontSize = 56.sp)
        }
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp), Arrangement.spacedBy(4.dp)) {
            Text(animal.nombre, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("${animal.especieTexto()} · ${animal.edadTexto()}", fontSize = 14.sp, color = TextoSecundario)
            Insignia(animal.estado)
        }
    }
}

// Piezas compartidas con las demás pantallas
@Composable
fun Cargando() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            // Render gratis se duerme: la primera carga puede tardar
            Text("Cargando...", fontSize = 14.sp, color = TextoSecundario)
        }
    }
}

@Composable
fun Mensaje(emoji: String, titulo: String, texto: String? = null) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        Arrangement.Center, Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 56.sp)
        Text(titulo, Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        texto?.let { Text(it, Modifier.padding(top = 8.dp), fontSize = 14.sp, color = TextoSecundario, textAlign = TextAlign.Center) }
    }
}

@Composable
fun ErrorConReintento(mensaje: String, reintentar: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        Arrangement.Center, Alignment.CenterHorizontally,
    ) {
        Text(mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.size(16.dp))
        Boton("Reintentar", reintentar, Modifier.fillMaxWidth(0.6f))
    }
}

// El diseño muestra cada animal con un emoji sobre un fondo pastel (el mismo en todas las pantallas)
fun emojiDe(especie: String): String = when (especie) { "perro" -> "🐶"; "gato" -> "🐱"; else -> "🐾" }

fun pastelDe(animalId: Long): Color = Pasteles[(animalId % Pasteles.size).toInt()]

fun Animal.emoji(): String = emojiDe(especie)

fun Animal.pastel(): Color = pastelDe(id)

fun Animal.especieTexto(): String =
    if (especie == "otro") especieOtra ?: "Otro" else especie.replaceFirstChar { it.uppercase() }

fun Animal.edadTexto(): String = when (edad) {
    0 -> "Menos de 1 año"
    1 -> "1 año"
    else -> "$edad años"
}
