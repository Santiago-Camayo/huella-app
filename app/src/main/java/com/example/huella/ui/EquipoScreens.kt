package com.example.huella.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huella.data.Resultado
import com.example.huella.data.Usuario
import com.example.huella.ui.theme.Linea
import com.example.huella.ui.theme.Primario
import com.example.huella.ui.theme.TextoSecundario
import com.example.huella.ui.theme.Verde

// HU-25: cambiar la contraseña propia. Si es obligatoria (contraseña temporal), no se puede salir sin cambiarla.
@Composable
fun CambiarPasswordScreen(auth: AuthViewModel, obligatorio: Boolean, alTerminar: () -> Unit, volver: () -> Unit) {
    LaunchedEffect(Unit) { auth.error = null }
    val contexto = LocalContext.current
    var actual by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Cambiar contraseña", if (obligatorio) null else volver)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(16.dp)) {
            if (obligatorio) {
                Caja { Text("Tu cuenta tiene una contraseña temporal. Cámbiala para continuar.") }
            }
            Campo("Contraseña actual", actual, { actual = it }, "Tu contraseña actual", KeyboardType.Password, esClave = true)
            Campo("Contraseña nueva", nueva, { nueva = it }, "Mínimo 8 caracteres", KeyboardType.Password, esClave = true)
            Campo("Confirmar contraseña nueva", confirmacion, { confirmacion = it }, "Repite la contraseña nueva", KeyboardType.Password, esClave = true)
            auth.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
            Boton(
                if (auth.cargando) "Guardando..." else "Cambiar contraseña",
                {
                    auth.error = when {
                        actual.isBlank() -> "Escribe tu contraseña actual"
                        nueva.length < 8 -> "La contraseña nueva debe tener al menos 8 caracteres"
                        nueva == actual -> "La contraseña nueva debe ser distinta de la actual"
                        nueva != confirmacion -> "Las contraseñas no coinciden"
                        else -> null
                    }
                    if (auth.error == null) {
                        auth.cambiarPassword(actual, nueva) {
                            Toast.makeText(contexto, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                            alTerminar()
                        }
                    }
                },
                habilitado = !auth.cargando,
            )
            // Con la contraseña temporal, la única otra salida es cerrar sesión
            if (obligatorio) Boton("Cerrar sesión", volver, estilo = EstiloBoton.Neutro)
        }
    }
}

// HU-24: personal de la fundación. Solo el administrador llega aquí.
@Composable
fun EquipoScreen(vm: EquipoViewModel, yo: Usuario?, volver: () -> Unit) {
    LaunchedEffect(Unit) {
        vm.error = null
        vm.cargar()
    }
    var agregando by remember { mutableStateOf(false) }
    var desactivar by remember { mutableStateOf<Usuario?>(null) } // espera confirmación

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Equipo", volver)
        when (val e = vm.equipo) {
            null -> Cargando()
            is Resultado.Error -> ErrorConReintento(e.mensaje, vm::cargar)
            is Resultado.Ok -> Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                Text("Personal de la fundación", Modifier.padding(bottom = 16.dp), style = MaterialTheme.typography.headlineSmall)
                Caja {
                    e.dato.forEachIndexed { i, persona ->
                        if (i > 0) HorizontalDivider(color = Linea)
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(persona.nombre + if (persona.esAdmin) " (admin)" else "", fontWeight = FontWeight.SemiBold)
                                Text(persona.correo, fontSize = 12.sp, color = TextoSecundario)
                            }
                            EstadoCuenta(persona.activo != false)
                            // RN-16: el administrador no cambia su propia cuenta
                            if (persona.id != yo?.id) {
                                TextButton(
                                    onClick = { if (persona.activo != false) desactivar = persona else vm.cambiarActivo(persona.id, true) },
                                    enabled = !vm.enviando,
                                ) { Text(if (persona.activo != false) "Desactivar" else "Activar", color = Primario, fontSize = 13.sp) }
                            }
                        }
                    }
                }
                vm.error?.takeIf { !agregando }?.let { Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
                Spacer(Modifier.height(20.dp))
                Boton("Agregar persona", { vm.error = null; agregando = true })
            }
        }
    }

    if (agregando) DialogoAgregar(vm) { agregando = false }
    desactivar?.let { persona ->
        DialogoConfirmacion(
            "¿Desactivar a ${persona.nombre}?", "No podrá iniciar sesión hasta que vuelvas a activar su cuenta.", "Desactivar",
            alConfirmar = { desactivar = null; vm.cambiarActivo(persona.id, false) },
            alCerrar = { desactivar = null },
        )
    }
}

@Composable
private fun EstadoCuenta(activa: Boolean) {
    val (fondo, letra) = if (activa) Color(0xFFDCFCE7) to Verde else Color(0xFFE2E8F0) to Color(0xFF475569)
    Text(
        if (activa) "Activo" else "Inactivo",
        Modifier.background(fondo, CircleShape).padding(horizontal = 8.dp, vertical = 2.dp),
        color = letra, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
    )
}

// Formulario para agregar un empleado con contraseña temporal
@Composable
private fun DialogoAgregar(vm: EquipoViewModel, cerrar: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val contexto = LocalContext.current

    AlertDialog(
        onDismissRequest = cerrar,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Agregar miembro al equipo", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Campo("Nombre", nombre, { nombre = it }, "Nombre completo")
                Campo("Correo", correo, { correo = it }, "correo@fundacion.org", KeyboardType.Email)
                Campo("Contraseña temporal", password, { password = it }, "Mínimo 8 caracteres", KeyboardType.Password)
                Text("Deberá cambiarla en su primer ingreso.", fontSize = 12.sp, color = TextoSecundario)
                vm.error?.let { Text("⚠ $it", color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            }
        },
        confirmButton = {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                Boton("Cancelar", cerrar, Modifier.weight(1f), EstiloBoton.Neutro)
                Boton(
                    "Agregar",
                    {
                        vm.error = when {
                            nombre.isBlank() || correo.isBlank() -> "Escribe el nombre y el correo"
                            password.length < 8 -> "La contraseña temporal debe tener al menos 8 caracteres"
                            else -> null
                        }
                        if (vm.error == null) {
                            vm.agregar(nombre, correo, password) {
                                Toast.makeText(contexto, "Persona agregada", Toast.LENGTH_SHORT).show()
                                cerrar()
                            }
                        }
                    },
                    Modifier.weight(1f),
                    habilitado = !vm.enviando,
                )
            }
        },
    )
}
