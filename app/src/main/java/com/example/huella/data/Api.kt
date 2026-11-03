package com.example.huella.data

import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// La app solo habla con esta API; nunca con Supabase.
const val URL_API = "https://huella-api-u3fo.onrender.com/"

interface ApiService {
    @POST("auth/register") suspend fun registrar(@Body datos: RegistroRequest): Response<AuthResponse>
    @POST("auth/login") suspend fun login(@Body datos: LoginRequest): Response<AuthResponse>
    @GET("usuarios/me") suspend fun me(): Response<Usuario>
    @GET("animales") suspend fun animales(@Query("estado") estado: String?): Response<List<Animal>>
    @GET("animales/{id}") suspend fun animal(@Path("id") id: Long): Response<Animal>
}

object Api {
    // El token guardado se agrega solo a cada petición (el usuario sale del token, no del cuerpo)
    lateinit var sesion: Sesion

    private val cliente = OkHttpClient.Builder()
        // Render gratis se duerme: la primera petición puede tardar cerca de un minuto
        .callTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { cadena ->
            val peticion = sesion.token?.let {
                cadena.request().newBuilder().header("Authorization", "Bearer $it").build()
            } ?: cadena.request()
            cadena.proceed(peticion)
        }
        .build()

    val servicio: ApiService = Retrofit.Builder()
        .baseUrl(URL_API)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()
        .create(ApiService::class.java)
}

// Resultado simple para las pantallas: el dato, o el mensaje de error de la API
sealed class Resultado<out T> {
    data class Ok<T>(val dato: T) : Resultado<T>()
    data class Error(val mensaje: String) : Resultado<Nothing>()
}

// Ejecuta una llamada y convierte los errores ({ "error": "mensaje" }) y la falta de internet en texto
suspend fun <T> llamar(peticion: suspend () -> Response<T>): Resultado<T> = try {
    val r = peticion()
    val cuerpo = r.body()
    if (r.isSuccessful && cuerpo != null) Resultado.Ok(cuerpo)
    else Resultado.Error(
        runCatching { JsonParser.parseString(r.errorBody()?.string()).asJsonObject["error"].asString }
            .getOrDefault("Error del servidor (${r.code()})")
    )
} catch (e: java.io.IOException) {
    Resultado.Error("No hay conexión con el servidor")
}
