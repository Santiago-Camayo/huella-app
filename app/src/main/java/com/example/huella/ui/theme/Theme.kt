package com.example.huella.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// El diseño solo tiene tema claro, así que no hay modo oscuro ni colores dinámicos.
private val Colores = lightColorScheme(
    primary = Primario,
    onPrimary = Color.White,
    secondary = TextoSecundario,
    background = Fondo,
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    error = Peligro,
    outline = Borde,
    outlineVariant = Linea,
)

@Composable
fun HuellaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, typography = Typography, content = content)
}
