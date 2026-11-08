package com.example.huella.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.huella.ui.theme.Borde
import com.example.huella.ui.theme.Primario

// HU-17: la fundación registra un animal. La foto es obligatoria y no se puede publicar sin ella.
@Composable
fun RegistrarAnimalScreen(vm: AnimalesViewModel, volver: () -> Unit, alRegistrar: () -> Unit) {
    LaunchedEffect(Unit) { vm.error = null }
    val contexto = LocalContext.current

    var nombre by rememberSaveable { mutableStateOf("") }
    var especie by rememberSaveable { mutableStateOf("perro") }
    var especieOtra by rememberSaveable { mutableStateOf("") }
    var raza by rememberSaveable { mutableStateOf("") }
    var edad by rememberSaveable { mutableStateOf("") }
    var sexo by rememberSaveable { mutableStateOf<String?>(null) }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var personalidad by rememberSaveable { mutableStateOf("") }
    var ninos by rememberSaveable { mutableStateOf(false) }
    var perros by rememberSaveable { mutableStateOf(false) }
    var gatos by rememberSaveable { mutableStateOf(false) }
    var foto by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Selector de fotos del sistema: no pide permisos de almacenamiento
    val elegirFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) foto = uri
    }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Registrar animal", volver)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Campo("Nombre", nombre, { nombre = it }, "Nombre del animal")
            Opciones("Especie", listOf("perro" to "Perro", "gato" to "Gato", "otro" to "Otro"), especie) { especie = it }
            if (especie == "otro") Campo("¿Qué especie es?", especieOtra, { especieOtra = it }, "Escribe la especie")
            Campo("Raza", raza, { raza = it }, "Raza")
            Campo("Edad (0-30)", edad, { nuevo -> if (nuevo.all { it.isDigit() } && nuevo.length <= 2) edad = nuevo }, "Edad en años", KeyboardType.Number)
            Opciones("Sexo", listOf("macho" to "Macho", "hembra" to "Hembra"), sexo) { sexo = it }
            Campo("Descripción", descripcion, { descripcion = it }, "Describe al animal", lineas = 4)
            Campo("Personalidad", personalidad, { personalidad = it }, "Rasgos separados por comas")
            Interruptor("Bueno con niños", ninos) { ninos = it }
            Interruptor("Bueno con perros", perros) { perros = it }
            Interruptor("Bueno con gatos", gatos) { gatos = it }

            // Zona para subir la foto; muestra la vista previa cuando ya hay una
            Surface(
                onClick = { elegirFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, Borde),
                modifier = Modifier.fillMaxWidth().height(if (foto == null) 112.dp else 200.dp),
            ) {
                if (foto == null) {
                    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                        Text("＋", fontSize = 28.sp, color = Primario)
                        Text("Subir foto", color = Primario, fontWeight = FontWeight.Bold)
                        Text("JPG, PNG o WEBP de hasta 5 MB", fontSize = 12.sp)
                    }
                } else {
                    Box {
                        AsyncImage(foto, "Foto del animal", Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                        Text(
                            "Cambiar foto", Modifier.align(Alignment.BottomCenter).padding(8.dp),
                            color = Color.White, fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
        }
        BarraFija {
            Boton(
                if (vm.enviando) "Registrando..." else "Registrar animal",
                {
                    val edadNumero = edad.toIntOrNull()
                    vm.error = when {
                        nombre.isBlank() -> "El nombre es obligatorio"
                        especie == "otro" && especieOtra.isBlank() -> "Indica qué especie es"
                        edadNumero == null || edadNumero > 30 -> "La edad debe ser un número entre 0 y 30"
                        sexo == null -> "Elige el sexo"
                        foto == null -> "La foto es obligatoria"
                        else -> null
                    }
                    val fotoElegida = foto
                    if (vm.error == null && fotoElegida != null) {
                        val campos = mapOf(
                            "nombre" to nombre.trim(), "especie" to especie, "especie_otra" to especieOtra.trim(),
                            "raza" to raza.trim(), "edad" to edad, "sexo" to sexo!!,
                            "descripcion" to descripcion.trim(), "personalidad" to personalidad.trim(),
                            "bueno_con_ninos" to ninos.toString(), "bueno_con_perros" to perros.toString(),
                            "bueno_con_gatos" to gatos.toString(),
                        )
                        vm.crear(campos, fotoElegida, contexto.contentResolver) {
                            Toast.makeText(contexto, "Animal registrado", Toast.LENGTH_SHORT).show()
                            alRegistrar()
                        }
                    }
                },
                habilitado = !vm.enviando,
            )
        }
    }
}

// Grupo de chips para elegir una sola opción
@Composable
private fun Opciones(etiqueta: String, opciones: List<Pair<String, String>>, elegida: String?, alElegir: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(etiqueta, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            opciones.forEach { (clave, nombre) -> Chip(nombre, elegida == clave) { alElegir(clave) } }
        }
    }
}

@Composable
private fun Interruptor(etiqueta: String, activo: Boolean, alCambiar: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(etiqueta, fontWeight = FontWeight.Bold)
        Switch(activo, alCambiar, colors = SwitchDefaults.colors(checkedTrackColor = Primario))
    }
}
