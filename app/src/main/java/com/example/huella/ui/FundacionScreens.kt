package com.example.huella.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.huella.data.Animal
import com.example.huella.ui.theme.Borde
import com.example.huella.ui.theme.Foco
import com.example.huella.ui.theme.Fondo
import com.example.huella.ui.theme.Linea
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huella.data.Resultado
import com.example.huella.ui.theme.Primario
import com.example.huella.ui.theme.TextoSecundario

// Módulo de la fundación.

// HU-21: solicitudes de los animales de mi fundación, pendientes más antiguas primero.
@Composable
fun FundacionSolicitudesScreen(vm: SolicitudesViewModel, abrir: (Long) -> Unit) {
    LaunchedEffect(Unit) { vm.cargarLista("antiguas") }
    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Solicitudes")
        ListaSolicitudes(vm, orden = "antiguas", vacio = "Todavía no hay solicitudes para tus animales") { s ->
            TarjetaSolicitud(s, s.usuarioNombre, "${s.animalNombre} · ${s.animalEspecie.replaceFirstChar { it.uppercase() }}") { abrir(s.id) }
        }
    }
}

// HU-22: detalle de una solicitud con los botones según su estado actual.
@Composable
fun FundacionSolicitudScreen(vm: SolicitudesViewModel, id: Long, volver: () -> Unit) {
    LaunchedEffect(id) {
        vm.error = null
        vm.cargarDetalle(id)
    }
    var nuevoEstado by remember { mutableStateOf<String?>(null) } // cambio que espera confirmación

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Detalle de solicitud", volver)
        when (val d = vm.detalle) {
            null -> Cargando()
            is Resultado.Error -> ErrorConReintento(d.mensaje) { vm.cargarDetalle(id) }
            is Resultado.Ok -> {
                val s = d.dato
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Caja {
                        Text("Adoptante", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(s.usuarioNombre, fontWeight = FontWeight.SemiBold)
                        Text(listOfNotNull(s.usuarioCorreo, s.usuarioTelefono).joinToString("\n"), fontSize = 14.sp, color = TextoSecundario)
                    }
                    Caja {
                        Text("Animal", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MiniAnimal(s.animalEspecie, s.animalId)
                            Text(s.animalNombre, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Insignia(s.animalEstado)
                        }
                    }
                    Caja {
                        Text("Solicitud", style = MaterialTheme.typography.titleMedium)
                        FilaDato("Fecha") { Text(fechaTexto(s.fecha), fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        FilaDato("Estado") { Insignia(s.estado) }
                        Spacer(Modifier.height(16.dp))
                        Text(s.comentario)
                    }
                    vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
                }

                // pendiente -> en revisión -> aprobada. Finalizar la adopción es del Sprint 3.
                val siguiente = when (s.estado) { "pendiente" -> "en_revision"; "en_revision" -> "aprobada"; else -> null }
                if (s.estado in ESTADOS_ACTIVOS) {
                    BarraFija {
                        siguiente?.let {
                            Boton(if (it == "en_revision") "Pasar a revisión" else "Aprobar", { nuevoEstado = it }, habilitado = !vm.enviando)
                        }
                        Boton("Rechazar", { nuevoEstado = "rechazada" }, estilo = EstiloBoton.Secundario, habilitado = !vm.enviando)
                    }
                }

                nuevoEstado?.let { estado ->
                    val (titulo, texto, boton) = when (estado) {
                        "en_revision" -> Triple("¿Pasar a revisión?", "La solicitud cambiará a En revisión.", "Continuar")
                        "aprobada" -> Triple("¿Aprobar solicitud?", "El animal quedará apartado para ${s.usuarioNombre}.", "Sí, aprobar")
                        else -> Triple("¿Rechazar solicitud?", "El adoptante verá su solicitud como rechazada.", "Sí, rechazar")
                    }
                    DialogoConfirmacion(
                        titulo, texto, boton,
                        alConfirmar = { nuevoEstado = null; vm.cambiarEstado(s.id, estado) },
                        alCerrar = { nuevoEstado = null },
                    )
                }
            }
        }
    }
}

// HU-16: animales de mi fundación en todos los estados, con búsqueda y filtro.
@Composable
fun FundacionAnimalesScreen(vm: AnimalesViewModel, abrir: (Long) -> Unit) {
    LaunchedEffect(Unit) { vm.cargarDeMiFundacion() }
    var busqueda by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("todos") }
    val contexto = LocalContext.current
    val filtros = listOf("todos", "disponible", "apartado", "adoptado", "retirado")

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            BarraSuperior("Animales")
            OutlinedTextField(
                busqueda, { busqueda = it },
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                placeholder = { Text("Buscar por nombre", color = TextoSecundario) },
                leadingIcon = { Text("⌕", fontSize = 20.sp, color = TextoSecundario) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Foco, unfocusedBorderColor = Borde,
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                ),
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                Arrangement.spacedBy(8.dp),
            ) {
                filtros.forEach { clave ->
                    Chip(if (clave == "todos") "Todos" else nombreEstado(clave), filtro == clave) { filtro = clave }
                }
            }

            when (val lista = vm.deMiFundacion) {
                null -> Cargando()
                is Resultado.Error -> ErrorConReintento(lista.mensaje, vm::cargarDeMiFundacion)
                is Resultado.Ok -> {
                    val visibles = lista.dato.filter {
                        (filtro == "todos" || it.estadoVisible() == filtro) && it.nombre.contains(busqueda.trim(), ignoreCase = true)
                    }
                    if (visibles.isEmpty()) {
                        Mensaje("🐾", if (lista.dato.isEmpty()) "Aún no tienes animales registrados" else "Sin resultados")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp)) {
                            items(visibles, key = { it.id }) { FilaAnimal(it) { abrir(it.id) } }
                        }
                    }
                }
            }
        }
        // Registrar un animal con foto es HU-17 (siguiente paso del Sprint 2)
        FloatingActionButton(
            { Toast.makeText(contexto, "Registrar animal: llega con la subida de fotos", Toast.LENGTH_SHORT).show() },
            Modifier.align(Alignment.BottomEnd).padding(16.dp),
            shape = CircleShape, containerColor = Primario, contentColor = Color.White,
        ) { Text("+", fontSize = 30.sp) }
    }
}

@Composable
private fun FilaAnimal(a: Animal, alTocar: () -> Unit) {
    Column(Modifier.clickable(onClick = alTocar)) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(56.dp).background(a.pastel(), RoundedCornerShape(12.dp)), Alignment.Center) {
                Text(a.emoji(), fontSize = 32.sp)
            }
            Column(Modifier.weight(1f)) {
                Text(a.nombre, fontWeight = FontWeight.SemiBold)
                Text("${a.especieTexto()} · ${a.edadTexto()}", fontSize = 14.sp, color = TextoSecundario)
            }
            Insignia(a.estadoVisible())
        }
        HorizontalDivider(color = Linea)
    }
}

// Detalle de un animal para la fundación, con las solicitudes que ha recibido
@Composable
fun FundacionAnimalScreen(
    animales: AnimalesViewModel,
    solicitudes: SolicitudesViewModel,
    id: Long,
    volver: () -> Unit,
    abrirSolicitud: (Long) -> Unit,
) {
    LaunchedEffect(id) {
        animales.cargarDetalle(id)
        solicitudes.cargarDelAnimal(id)
    }
    when (val d = animales.detalle) {
        is Resultado.Ok -> {
            val a = d.dato
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Portada(a, volver)
                Surface(
                    Modifier.fillMaxWidth().offset(y = (-24).dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Fondo,
                ) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(a.nombre, style = MaterialTheme.typography.headlineMedium)
                            Insignia(a.estadoVisible())
                        }
                        Text(a.datosTexto(), Modifier.padding(top = 4.dp), fontSize = 14.sp, color = TextoSecundario)

                        Text("Solicitudes de este animal", Modifier.padding(top = 24.dp, bottom = 12.dp), style = MaterialTheme.typography.titleMedium)
                        when (val s = solicitudes.delAnimal) {
                            null -> CircularProgressIndicator()
                            is Resultado.Error -> Text(s.mensaje, color = MaterialTheme.colorScheme.error)
                            is Resultado.Ok ->
                                if (s.dato.isEmpty()) {
                                    Text("Aún no tiene solicitudes", color = TextoSecundario)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        s.dato.forEach { sol ->
                                            TarjetaSolicitud(sol, sol.usuarioNombre, fechaTexto(sol.fecha)) { abrirSolicitud(sol.id) }
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }
        else -> Column(Modifier.fillMaxSize()) {
            BarraSuperior("Animal", volver)
            if (d is Resultado.Error) ErrorConReintento(d.mensaje) { animales.cargarDetalle(id) } else Cargando()
        }
    }
}

// Cuenta del personal de la fundación, con cerrar sesión
@Composable
fun CuentaScreen(auth: AuthViewModel, alCerrarSesion: () -> Unit) {
    val u = auth.usuario
    var confirmar by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Cuenta")
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(72.dp).background(Primario, CircleShape), Alignment.Center) {
                    Text(u?.nombre?.take(1)?.uppercase() ?: "", color = Color.White, fontSize = 28.sp)
                }
                Text(u?.nombre ?: "", Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium)
            }
            Caja {
                FilaDato("Nombre") { Text(u?.nombre ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                FilaDato("Correo") { Text(u?.correo ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                FilaDato("Teléfono") { Text(u?.telefono ?: "—", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                FilaDato("Rol") { Text(if (u?.esAdmin == true) "Administrador" else "Personal", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
            Boton("Cerrar sesión", { confirmar = true }, estilo = EstiloBoton.Neutro)
        }
    }
    if (confirmar) {
        DialogoConfirmacion(
            "¿Cerrar sesión?", "Tendrás que volver a iniciar sesión para entrar.", "Cerrar sesión",
            alConfirmar = { confirmar = false; alCerrarSesion() },
            alCerrar = { confirmar = false },
        )
    }
}
