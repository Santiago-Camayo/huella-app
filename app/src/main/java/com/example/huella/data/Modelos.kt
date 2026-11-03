package com.example.huella.data

import com.google.gson.annotations.SerializedName

// Lo que se envía y recibe de la API. Los nombres en snake_case son los del JSON.

data class RegistroRequest(val nombre: String, val correo: String, val telefono: String?, val password: String)

data class LoginRequest(val correo: String, val password: String)

data class Usuario(
    val id: Long,
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val rol: String,
    @SerializedName("fundacion_id") val fundacionId: Long?,
    @SerializedName("es_admin") val esAdmin: Boolean,
    @SerializedName("debe_cambiar_password") val debeCambiarPassword: Boolean,
)

// Respuesta de /auth/register y /auth/login
data class AuthResponse(val token: String, val usuario: Usuario)

data class Animal(
    val id: Long,
    val nombre: String,
    val especie: String,
    @SerializedName("especie_otra") val especieOtra: String?,
    val raza: String?,
    val edad: Int,
    val sexo: String,
    val descripcion: String?,
    val personalidad: String?,
    @SerializedName("bueno_con_ninos") val buenoConNinos: Boolean?,
    @SerializedName("bueno_con_perros") val buenoConPerros: Boolean?,
    @SerializedName("bueno_con_gatos") val buenoConGatos: Boolean?,
    val foto: String,
    val estado: String,
    val activo: Boolean, // false = la fundación retiró la publicación
    @SerializedName("fundacion_nombre") val fundacionNombre: String,
    @SerializedName("fundacion_ciudad") val fundacionCiudad: String,
)
