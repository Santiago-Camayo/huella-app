package com.example.huella.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huella.data.Animal
import com.example.huella.data.Api
import com.example.huella.data.Resultado
import com.example.huella.data.llamar
import kotlinx.coroutines.launch
import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Datos de las pantallas de animales. null significa "cargando".
class AnimalesViewModel : ViewModel() {
    var lista by mutableStateOf<Resultado<List<Animal>>?>(null)
        private set
    var detalle by mutableStateOf<Resultado<Animal>?>(null)
        private set
    var deMiFundacion by mutableStateOf<Resultado<List<Animal>>?>(null)
        private set

    var enviando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)

    // HU-17: lee la foto elegida, revisa tipo y tamaño, y envía el formulario multipart
    fun crear(campos: Map<String, String>, foto: Uri, resolver: ContentResolver, alExito: () -> Unit) {
        if (enviando) return
        viewModelScope.launch {
            enviando = true
            error = null
            val tipo = resolver.getType(foto)
            val bytes = withContext(Dispatchers.IO) { resolver.openInputStream(foto)?.use { it.readBytes() } }
            error = when {
                tipo !in listOf("image/jpeg", "image/png", "image/webp") -> "La foto debe ser JPG, PNG o WEBP"
                bytes == null -> "No se pudo leer la foto"
                bytes.size > 5 * 1024 * 1024 -> "La foto no puede pesar más de 5 MB"
                else -> null
            }
            if (error == null && tipo != null && bytes != null) {
                val texto = "text/plain".toMediaType()
                val parte = MultipartBody.Part.createFormData(
                    "foto", "foto.${tipo.substringAfter('/')}", bytes.toRequestBody(tipo.toMediaType()),
                )
                when (val r = llamar { Api.servicio.crearAnimal(campos.mapValues { it.value.toRequestBody(texto) }, parte) }) {
                    is Resultado.Ok -> {
                        lista = null // la lista del adoptante se vuelve a pedir
                        alExito()
                    }
                    is Resultado.Error -> error = r.mensaje
                }
            }
            enviando = false
        }
    }

    // HU-16: con el token de una fundación, la API devuelve solo sus animales, también los retirados
    fun cargarDeMiFundacion() {
        viewModelScope.launch {
            deMiFundacion = null
            deMiFundacion = llamar { Api.servicio.animales(null) }
        }
    }

    // HU-06: solo los disponibles (la API ya excluye los retirados)
    fun cargarLista() {
        viewModelScope.launch {
            lista = null
            lista = llamar { Api.servicio.animales("disponible") }
        }
    }

    // HU-08
    fun cargarDetalle(id: Long) {
        viewModelScope.launch {
            detalle = null
            detalle = llamar { Api.servicio.animal(id) }
        }
    }
}
