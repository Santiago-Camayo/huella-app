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

    // HU-13: favoritos guardados en el teléfono y su estado actual pedido a la API
    var favoritos by mutableStateOf(Api.sesion.favoritos)
        private set
    var listaFavoritos by mutableStateOf<Resultado<List<Animal>>?>(null)
        private set

    fun alternarFavorito(id: Long) {
        favoritos = if (id in favoritos) favoritos - id else favoritos + id
        Api.sesion.favoritos = favoritos
    }

    // Pide cada favorito con GET /animales/{id} para saber si sigue disponible (incluye los retirados)
    fun cargarFavoritos() {
        viewModelScope.launch {
            listaFavoritos = null
            val animales = mutableListOf<Animal>()
            for (id in favoritos) {
                when (val r = llamar { Api.servicio.animal(id) }) {
                    is Resultado.Ok -> animales += r.dato
                    is Resultado.Error -> if (r.codigo != 404) {   // un 404 (ya no existe) simplemente no se muestra
                        listaFavoritos = r
                        return@launch
                    }
                }
            }
            listaFavoritos = Resultado.Ok(animales)
        }
    }

    // Al cerrar sesión: Sesion.cerrar() ya los borró del teléfono; aquí se olvidan los de memoria
    fun olvidarFavoritos() {
        favoritos = emptySet()
        listaFavoritos = null
    }

    var enviando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)

    // HU-17 y HU-18: envía el formulario multipart. Con id == null crea (la foto es obligatoria);
    // con un id edita ese animal (la foto es opcional: sin foto se conserva la actual).
    fun guardar(id: Long?, campos: Map<String, String>, foto: Uri?, resolver: ContentResolver, alExito: () -> Unit) {
        if (enviando) return
        viewModelScope.launch {
            enviando = true
            error = null
            val parte = foto?.let { leerFoto(it, resolver) ?: run { enviando = false; return@launch } }
            val texto = "text/plain".toMediaType()
            val cuerpo = campos.mapValues { it.value.toRequestBody(texto) }
            val r = llamar {
                if (id == null) Api.servicio.crearAnimal(cuerpo, parte!!) else Api.servicio.editarAnimal(id, cuerpo, parte)
            }
            when (r) {
                is Resultado.Ok -> {
                    lista = null // la lista del adoptante se vuelve a pedir
                    alExito()
                }
                is Resultado.Error -> error = r.mensaje
            }
            enviando = false
        }
    }

    // Lee la foto elegida y revisa tipo y tamaño. Si algo falla, deja el mensaje en error y devuelve null.
    private suspend fun leerFoto(foto: Uri, resolver: ContentResolver): MultipartBody.Part? {
        val tipo = resolver.getType(foto)
        val bytes = withContext(Dispatchers.IO) { resolver.openInputStream(foto)?.use { it.readBytes() } }
        error = when {
            tipo == null || tipo !in listOf("image/jpeg", "image/png", "image/webp") -> "La foto debe ser JPG, PNG o WEBP"
            bytes == null -> "No se pudo leer la foto"
            bytes.size > 5 * 1024 * 1024 -> "La foto no puede pesar más de 5 MB"
            else -> null
        }
        if (error != null || tipo == null || bytes == null) return null
        return MultipartBody.Part.createFormData("foto", "foto.${tipo.substringAfter('/')}", bytes.toRequestBody(tipo.toMediaType()))
    }

    // HU-19: retira la publicación; el animal y sus solicitudes se conservan
    fun retirar(id: Long, alExito: () -> Unit) {
        if (enviando) return
        viewModelScope.launch {
            enviando = true
            error = null
            when (val r = llamar { Api.servicio.retirarAnimal(id) }) {
                is Resultado.Ok -> {
                    lista = null
                    cargarDetalle(id)
                    alExito()
                }
                is Resultado.Error -> error = r.mensaje
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
