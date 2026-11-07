package com.example.huella.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huella.data.Resultado
import com.example.huella.data.Solicitud
import com.example.huella.ui.theme.Peligro
import com.example.huella.ui.theme.TextoPrincipal
import com.example.huella.ui.theme.TextoSecundario
import com.example.huella.ui.theme.Verde
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Estados en los que la solicitud sigue abierta (se puede cancelar o atender)
val ESTADOS_ACTIVOS = listOf("pendiente", "en_revision", "aprobada")

// HU-10: las solicitudes del adoptante, más recientes primero.
@Composable
fun MisSolicitudesScreen(vm: SolicitudesViewModel, abrir: (Long) -> Unit) {
    LaunchedEffect(Unit) { vm.cargarLista(null) }
    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Mis solicitudes")
        ListaSolicitudes(vm, orden = null, vacio = "Aún no has enviado solicitudes") { s ->
            TarjetaSolicitud(s, s.animalNombre, "Solicitud #${s.id}") { abrir(s.id) }
        }
    }
}

// Lista con filtro por estado y "deslizar para refrescar". La usan el adoptante y la fundación.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudes(vm: SolicitudesViewModel, orden: String?, vacio: String, tarjeta: @Composable (Solicitud) -> Unit) {
    var filtro by rememberSaveable { mutableStateOf("todos") }
    val filtros = listOf("todos" to "Todos", "pendiente" to "Pendiente", "en_revision" to "En revisión", "aprobada" to "Aprobada", "cerradas" to "Cerradas")

    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        Arrangement.spacedBy(8.dp),
    ) {
        filtros.forEach { (clave, nombre) -> Chip(nombre, filtro == clave) { filtro = clave } }
    }

    when (val lista = vm.lista) {
        null -> Cargando()
        is Resultado.Error -> ErrorConReintento(lista.mensaje) { vm.cargarLista(orden) }
        is Resultado.Ok -> {
            val visibles = lista.dato.filter {
                when (filtro) {
                    "todos" -> true
                    "cerradas" -> it.estado !in ESTADOS_ACTIVOS
                    else -> it.estado == filtro
                }
            }
            PullToRefreshBox(vm.refrescando, { vm.cargarLista(orden, refrescar = true) }, Modifier.fillMaxSize()) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (visibles.isEmpty()) {
                        item { Mensaje("📋", if (lista.dato.isEmpty()) vacio else "No hay solicitudes con ese estado") }
                    }
                    items(visibles, key = { it.id }) { tarjeta(it) }
                }
            }
        }
    }
}

@Composable
fun TarjetaSolicitud(s: Solicitud, titulo: String, subtitulo: String, alTocar: (() -> Unit)? = null) {
    Card(
        onClick = { alTocar?.invoke() },
        enabled = alTocar != null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White, contentColor = TextoPrincipal,
            disabledContainerColor = Color.White, disabledContentColor = TextoPrincipal,
        ),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniAnimal(s.animalEspecie, s.animalId)
            Column(Modifier.weight(1f)) {
                Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitulo, fontSize = 13.sp, color = TextoSecundario)
            }
            Insignia(s.estado)
        }
    }
}

// HU-12: detalle de la solicitud del adoptante, con su línea de tiempo y la opción de cancelar.
@Composable
fun SolicitudScreen(vm: SolicitudesViewModel, id: Long, volver: () -> Unit) {
    LaunchedEffect(id) {
        vm.error = null
        vm.cargarDetalle(id)
    }
    var confirmar by remember { mutableStateOf(false) }
    val contexto = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Solicitud #$id", volver)
        when (val d = vm.detalle) {
            null -> Cargando()
            is Resultado.Error -> ErrorConReintento(d.mensaje) { vm.cargarDetalle(id) }
            is Resultado.Ok -> {
                val s = d.dato
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TarjetaSolicitud(s, s.animalNombre, "Solicitud #${s.id}")
                    Caja { Text(mensajeEstado(s.estado)) }
                    Caja {
                        Text("Seguimiento", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        LineaDeTiempo(s.estado)
                    }
                    vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
                }
                if (s.estado in ESTADOS_ACTIVOS) {
                    BarraFija {
                        Boton("Cancelar solicitud", { confirmar = true }, estilo = EstiloBoton.Secundario, habilitado = !vm.enviando)
                    }
                }
                if (confirmar) {
                    DialogoConfirmacion(
                        "¿Cancelar solicitud?",
                        if (s.estado == "aprobada") "Tu solicitud está aprobada; si cancelas, el animal volverá a estar disponible."
                        else "Podrás volver a solicitar más adelante.",
                        "Sí, cancelar",
                        alConfirmar = {
                            confirmar = false
                            vm.cambiarEstado(s.id, "cancelada") {
                                Toast.makeText(contexto, "Solicitud cancelada", Toast.LENGTH_SHORT).show()
                            }
                        },
                        alCerrar = { confirmar = false },
                    )
                }
            }
        }
    }
}

private fun mensajeEstado(estado: String) = when (estado) {
    "pendiente" -> "La fundación aún no revisa tu solicitud."
    "en_revision" -> "La fundación está revisando tu solicitud."
    "aprobada" -> "¡Buenas noticias! Tu solicitud fue aprobada. La fundación te contactará."
    "finalizada" -> "¡Felicidades! La adopción se completó."
    "rechazada" -> "La fundación no pudo aprobar esta solicitud."
    else -> "Cancelaste esta solicitud."
}

// Pasos de la solicitud. Si fue rechazada o cancelada, el segundo paso queda en rojo.
@Composable
private fun LineaDeTiempo(estado: String) {
    val pasos = listOf("Solicitud enviada", "En revisión", "Aprobada", "Adopción finalizada")
    val avance = mapOf("pendiente" to 0, "en_revision" to 1, "aprobada" to 2, "finalizada" to 3)[estado] ?: 1
    val terminal = estado == "rechazada" || estado == "cancelada"

    pasos.take(if (terminal) 2 else 4).forEachIndexed { i, paso ->
        val fallo = terminal && i == 1
        val hecho = i <= avance && !fallo
        Row(Modifier.height(44.dp), Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(22.dp).background(
                    when { fallo -> Peligro; hecho -> Verde; else -> Color(0xFFE2D5C8) }, CircleShape,
                ),
                Alignment.Center,
            ) {
                Text(
                    when { fallo -> "✕"; hecho -> "✓"; else -> "·" },
                    color = if (fallo || hecho) Color.White else TextoPrincipal,
                    fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                )
            }
            Text(if (fallo) nombreEstado(estado) else paso, fontSize = 14.sp)
        }
    }
}

// "2026-11-07T15:30:00.000Z" -> "7 de noviembre de 2026"
fun fechaTexto(iso: String): String = runCatching {
    val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO")).format(entrada.parse(iso.take(19))!!)
}.getOrDefault(iso)
