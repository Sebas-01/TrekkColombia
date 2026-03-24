package com.trekking.app.api

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("idusuario") val idUsuario: Int,
    val nombre: String,
    val telefono: String?,
    val correo: String,
    val foto: String?,
    val rol: String,
    @SerializedName("fecha_creacion") val fechaCreacion: String?
) {
    // Comprobación de roles más robusta
    val isSuperAdmin: Boolean get() = rol.equals("super_admin", ignoreCase = true) || rol.equals("admin", ignoreCase = true)
    val isCompanyAdmin: Boolean get() = rol.equals("administrador_empresa", ignoreCase = true)
}

data class LoginRequest(
    val correo: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    @SerializedName("idUsuario") val idUsuario: Int,
    val nombre: String,
    @SerializedName("rol") val rol: String?, // Aseguramos que mapee 'rol'
    @SerializedName("fechaCreacion") val fechaCreacion: String?
)

data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val password: String,
    val rol: String,
    val foto: String? = ""
)

data class UpdateRequest(
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val password: String? = null,
    val foto: String? = ""
)

data class MessageResponse(
    val message: String
)

data class TrekkingRoute(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("imageurl") val imageUrl: String,
    @SerializedName("description") val description: String,
    @SerializedName("height") val height: Int,
    @SerializedName("companyname") val companyName: String,
    @SerializedName("difficulty") val difficulty: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("guidename") val guideName: String,
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
    @SerializedName("geojson") val geoJson: String? = null,
    @SerializedName("isfavorite") val isFavorite: Boolean = false
)

data class FavoriteRequest(
    val idUsuario: Int,
    val idRuta: Int
)
