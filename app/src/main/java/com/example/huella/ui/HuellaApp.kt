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
import com.example.huella.data.Usuario
import com.example.huella.ui.theme.Primario
import com.example.huella.ui.theme.TextoSecundario
import kotlinx.coroutines.delay

// Pestañas de cada módulo. Las demás del diseño (Explorar, Favoritos, Dashboard, Animales) llegan después.
private val PESTANAS_ADOPTANTE = listOf(
    Pestana("inicio", "⌂", "Inicio"), Pestana("solicitudes", "▤", "Solicitudes"), Pestana("perfil", "♙", "Perfil"),
)
private val PESTANAS_FUNDACION = listOf(Pestana("f/solicitudes", "📋", "Solicitudes"), Pestana("f/cuenta", "👤", "Cuenta"))

// HU-02: el rol decide a qué módulo entra el usuario
private fun inicioDe(usuario: Usuario?) = if (usuario?.rol == "fundacion") "f/solicitudes" else "inicio"

// Las pantallas de la app y cómo se llega de una a otra.
@Composable
fun HuellaApp() {
    val nav = rememberNavController()
    val auth: AuthViewModel = viewModel()
    val animales: AnimalesViewModel = viewModel()
    val solicitudes: SolicitudesViewModel = viewModel()
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
                    Splash()
                    // Se ve al menos 1,4 s mientras se revisa si hay una sesión guardada (HU-03)
                    LaunchedEffect(Unit) {
                        val inicio = SystemClock.elapsedRealtime()
                        val haySesion = auth.restaurarSesion()
                        delay((1400 - (SystemClock.elapsedRealtime() - inicio)).coerceAtLeast(0))
                        nav.navigate(if (haySesion) inicioDe(auth.usuario) else "login") { popUpTo("splash") { inclusive = true } }
                    }
                }
                composable("login") {
                    LoginScreen(
                        auth,
                        alEntrar = { nav.navigate(inicioDe(auth.usuario)) { popUpTo("login") { inclusive = true } } },
                        irARegistro = { auth.error = null; nav.navigate("registro") },
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
                composable("f/solicitudes") {
                    FundacionSolicitudesScreen(solicitudes, abrir = { id -> nav.navigate("f/solicitud/$id") })
                }
                composable("f/solicitud/{id}", arguments = argumentoId) {
                    FundacionSolicitudScreen(solicitudes, idDe(it), volver = { nav.popBackStack() })
                }
                composable("f/cuenta") { CuentaScreen(auth, alCerrarSesion) }
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
private fun Splash() {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("🐾", fontSize = 72.sp)
        Text("Huella", Modifier.padding(top = 16.dp, bottom = 8.dp), fontSize = 32.sp, color = Primario, style = MaterialTheme.typography.headlineMedium)
        Text("Encuentra a tu compañero", fontSize = 14.sp, color = TextoSecundario)
    }
}
