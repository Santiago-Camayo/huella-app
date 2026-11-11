package com.example.huella.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.huella.data.Resultado
import com.example.huella.data.Usuario
import com.example.huella.ui.theme.Primario
import com.example.huella.ui.theme.TextoSecundario
import kotlinx.coroutines.delay

// Pestañas de cada módulo. Las demás del diseño (Explorar, Favoritos, Dashboard, Animales) llegan después.
private val PESTANAS_ADOPTANTE = listOf(
    Pestana("inicio", "⌂", "Inicio"), Pestana("solicitudes", "▤", "Solicitudes"), Pestana("perfil", "♙", "Perfil"),
)
private val PESTANAS_FUNDACION = listOf(
    Pestana("f/panel", "📊", "Dashboard"), Pestana("f/animales", "🐾", "Animales"),
    Pestana("f/solicitudes", "📋", "Solicitudes"), Pestana("f/cuenta", "👤", "Cuenta"),
)

// HU-02: el rol decide a qué módulo entra el usuario
// HU-25: con contraseña temporal, primero hay que cambiarla
private fun inicioDe(usuario: Usuario?) = when {
    usuario?.debeCambiarPassword == true -> "clave/obligatoria"
    usuario?.rol == "fundacion" -> "f/panel"
    else -> "inicio"
}

// Las pantallas de la app y cómo se llega de una a otra.
@Composable
fun HuellaApp() {
    val nav = rememberNavController()
    val auth: AuthViewModel = viewModel()
    val animales: AnimalesViewModel = viewModel()
    val solicitudes: SolicitudesViewModel = viewModel()
    val equipo: EquipoViewModel = viewModel()
    val ruta = nav.currentBackStackEntryAsState().value?.destination?.route

    val alCerrarSesion = {
        auth.cerrarSesion() // HU-04
        nav.navigate("login") { popUpTo(0) }
    }
    val idDe = { entrada: androidx.navigation.NavBackStackEntry -> entrada.arguments!!.getLong("id") }
    val argumentoId = listOf(navArgument("id") { type = NavType.LongType })

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
            NavHost(nav, startDestination = "splash", modifier = Modifier.weight(1f)) {
                composable("splash") {
                    var intento by remember { mutableIntStateOf(0) }
                    var sinRed by remember { mutableStateOf(false) }
                    Splash(sinRed) { sinRed = false; intento++ }
                    // Se ve al menos 1,4 s mientras se revisa si hay una sesión guardada (HU-03)
                    LaunchedEffect(intento) {
                        val inicio = SystemClock.elapsedRealtime()
                        val arranque = auth.restaurarSesion()
                        delay((1400 - (SystemClock.elapsedRealtime() - inicio)).coerceAtLeast(0))
                        if (arranque == Arranque.SIN_RED) {
                            sinRed = true
                        } else {
                            val destino = if (arranque == Arranque.CON_SESION) inicioDe(auth.usuario) else "login"
                            nav.navigate(destino) { popUpTo("splash") { inclusive = true } }
                        }
                    }
                }
                composable("login") {
                    LoginScreen(
                        auth,
                        alEntrar = { nav.navigate(inicioDe(auth.usuario)) { popUpTo("login") { inclusive = true } } },
                        irARegistro = { auth.error = null; nav.navigate("registro") },
                        irARegistroFundacion = { auth.error = null; nav.navigate("registro-fundacion") },
                    )
                }
                composable("registro-fundacion") {
                    RegistroFundacionScreen(
                        auth,
                        alRegistrar = { nav.navigate("f/panel") { popUpTo("login") { inclusive = true } } },
                        volver = { auth.error = null; nav.popBackStack() },
                    )
                }
                composable("registro") {
                    RegistroScreen(
                        auth,
                        alRegistrar = { nav.navigate("inicio") { popUpTo("login") { inclusive = true } } },
                        volver = { auth.error = null; nav.popBackStack() },
                    )
                }

                // Adoptante
                composable("inicio") {
                    AnimalesScreen(animales, auth.usuario?.nombre ?: "", abrir = { id -> nav.navigate("animal/$id") })
                }
                composable("animal/{id}", arguments = argumentoId) {
                    DetalleScreen(animales, idDe(it), volver = { nav.popBackStack() }, adoptar = { nav.navigate("solicitar") })
                }
                composable("solicitar") {
                    SolicitarScreen(
                        animales, solicitudes, auth.usuario,
                        volver = { nav.popBackStack() },
                        alEnviar = { nav.navigate("confirmacion") { popUpTo("inicio") } },
                    )
                }
                composable("confirmacion") {
                    ConfirmacionScreen(
                        solicitudes,
                        verSolicitud = { id -> nav.navigate("solicitud/$id") { popUpTo("inicio") } },
                        alInicio = { nav.popBackStack("inicio", inclusive = false) },
                    )
                }
                composable("solicitudes") {
                    MisSolicitudesScreen(solicitudes, abrir = { id -> nav.navigate("solicitud/$id") })
                }
                composable("solicitud/{id}", arguments = argumentoId) {
                    SolicitudScreen(solicitudes, idDe(it), volver = { nav.popBackStack() })
                }
                composable("perfil") { PerfilScreen(auth, alCerrarSesion) }

                // Fundación
                composable("f/panel") {
                    FundacionPanelScreen(solicitudes, auth.usuario?.nombre ?: "", abrir = { id -> nav.navigate("f/solicitud/$id") })
                }
                composable("f/solicitudes") {
                    FundacionSolicitudesScreen(solicitudes, abrir = { id -> nav.navigate("f/solicitud/$id") })
                }
                composable("f/solicitud/{id}", arguments = argumentoId) {
                    FundacionSolicitudScreen(solicitudes, idDe(it), volver = { nav.popBackStack() })
                }
                composable("f/animales") {
                    FundacionAnimalesScreen(
                        animales,
                        abrir = { id -> nav.navigate("f/animal/$id") },
                        registrar = { nav.navigate("f/registrar") },
                    )
                }
                composable("f/registrar") {
                    RegistrarAnimalScreen(animales, animal = null, volver = { nav.popBackStack() }, alGuardar = { nav.popBackStack() })
                }
                // HU-18: el mismo formulario con los datos del animal que se está viendo
                composable("f/editar") {
                    RegistrarAnimalScreen(
                        animales, animal = (animales.detalle as? Resultado.Ok)?.dato,
                        volver = { nav.popBackStack() }, alGuardar = { nav.popBackStack() },
                    )
                }
                composable("f/animal/{id}", arguments = argumentoId) {
                    FundacionAnimalScreen(
                        animales, solicitudes, idDe(it),
                        volver = { nav.popBackStack() },
                        abrirSolicitud = { id -> nav.navigate("f/solicitud/$id") },
                        editar = { nav.navigate("f/editar") },
                    )
                }
                composable("f/cuenta") {
                    CuentaScreen(
                        auth, alCerrarSesion,
                        cambiarPassword = { nav.navigate("clave") },
                        irAEquipo = { nav.navigate("f/equipo") },
                    )
                }
                composable("f/equipo") { EquipoScreen(equipo, auth.usuario, volver = { nav.popBackStack() }) }

                // HU-25: cambio de contraseña obligatorio (temporal) o voluntario (desde Cuenta o Perfil)
                composable("clave/obligatoria") {
                    CambiarPasswordScreen(
                        auth, obligatorio = true,
                        alTerminar = { nav.navigate(inicioDe(auth.usuario)) { popUpTo(0) } },
                        volver = alCerrarSesion,
                    )
                }
                composable("clave") {
                    CambiarPasswordScreen(auth, obligatorio = false, alTerminar = { nav.popBackStack() }, volver = { nav.popBackStack() })
                }
            }

            // La barra de abajo solo se ve en las pantallas principales de cada módulo
            val pestanas = listOf(PESTANAS_ADOPTANTE, PESTANAS_FUNDACION).firstOrNull { grupo -> grupo.any { it.ruta == ruta } }
            if (pestanas != null) {
                BarraInferior(pestanas, ruta) { destino ->
                    nav.navigate(destino) { popUpTo(pestanas.first().ruta); launchSingleTop = true }
                }
            }
        }
    }
}

@Composable
private fun Splash(sinRed: Boolean, reintentar: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("🐾", fontSize = 72.sp)
        Text("Huella", Modifier.padding(top = 16.dp, bottom = 8.dp), fontSize = 32.sp, color = Primario, style = MaterialTheme.typography.headlineMedium)
        Text("Encuentra a tu compañero", fontSize = 14.sp, color = TextoSecundario)
        if (sinRed) {
            Text(
                "No hay conexión con el servidor", Modifier.padding(top = 32.dp, bottom = 16.dp),
                color = MaterialTheme.colorScheme.error, fontSize = 14.sp,
            )
            Boton("Reintentar", reintentar, Modifier.fillMaxWidth(0.6f))
        }
    }
}
