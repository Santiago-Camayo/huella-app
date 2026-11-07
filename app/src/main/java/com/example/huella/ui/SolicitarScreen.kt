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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.huella.data.Resultado
import com.example.huella.data.Usuario
import com.example.huella.ui.theme.TextoSecundario
import com.example.huella.ui.theme.Verde

// HU-09: formulario para pedir la adopción del animal que se está viendo.
@Composable
fun SolicitarScreen(
    animales: AnimalesViewModel,
    vm: SolicitudesViewModel,
    usuario: Usuario?,
    volver: () -> Unit,
    alEnviar: () -> Unit,
) {
    LaunchedEffect(Unit) { vm.error = null }
    var comentario by rememberSaveable { mutableStateOf("") }
    val animal = (animales.detalle as? Resultado.Ok)?.dato

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Solicitar adopción", volver)
        if (animal == null) {
            Mensaje("🐾", "No se encontró el animal")
            return@Column
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Caja {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniAnimal(animal.especie, animal.id)
                    Column {
                        Text(animal.nombre, fontWeight = FontWeight.SemiBold)
                        Text("${animal.especieTexto()} · ${animal.edadTexto()}", fontSize = 14.sp, color = TextoSecundario)
                    }
                }
            }
            Campo("Nombre", usuario?.nombre ?: "", {}, soloLectura = true)
            Campo("Correo", usuario?.correo ?: "", {}, soloLectura = true)
            Column {
                Campo(
                    "¿Por qué quieres adoptar a ${animal.nombre}?", comentario,
                    { if (it.length <= 500) comentario = it },
                    "Cuéntanos (mínimo 10 caracteres)", lineas = 5,
                )
                Text(
                    "${comentario.length}/500", Modifier.fillMaxWidth().padding(top = 4.dp),
                    fontSize = 12.sp, color = TextoSecundario, textAlign = TextAlign.End,
                )
            }
            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
        }
        BarraFija {
            Boton(
                if (vm.enviando) "Enviando..." else "Enviar solicitud",
                { vm.enviar(animal.id, comentario, alEnviar) },
                habilitado = comentario.trim().length >= 10 && !vm.enviando,
            )
        }
    }
}

// Pantalla de confirmación después de enviar la solicitud
@Composable
fun ConfirmacionScreen(vm: SolicitudesViewModel, verSolicitud: (Long) -> Unit, alInicio: () -> Unit) {
    val s = vm.ultimaEnviada
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(24.dp),
            Arrangement.Center, Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(96.dp).background(Color(0xFFDCFCE7), CircleShape), Alignment.Center) {
                Text("✓", fontSize = 52.sp, color = Verde)
            }
            Text("Solicitud enviada", Modifier.padding(top = 24.dp, bottom = 8.dp), style = MaterialTheme.typography.headlineSmall)
            Text(
                "La fundación revisará tu solicitud para adoptar a ${s?.animalNombre ?: "este animal"}.",
                color = TextoSecundario, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Insignia("pendiente")
        }
        BarraFija {
            Boton("Ver mi solicitud", { s?.let { verSolicitud(it.id) } })
            Boton("Volver al inicio", alInicio, estilo = EstiloBoton.Neutro)
        }
    }
}

// Cuadro pequeño con el emoji del animal sobre su color pastel
@Composable
fun MiniAnimal(especie: String, animalId: Long) {
    Box(Modifier.size(48.dp).background(pastelDe(animalId), RoundedCornerShape(12.dp)), Alignment.Center) {
        Text(emojiDe(especie), fontSize = 28.sp)
    }
}
