package com.trekking.app.api

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("idusuario") val idUsuario: Int,
    val nombre: String,
    val telefono: String?,
    val correo: String,
    val foto: String?,
    @SerializedName("fecha_creacion") val fechaCreacion: String?
)

data class LoginRequest(
    val correo: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    @SerializedName("idUsuario") val idUsuario: Int,
    val nombre: String,
    val correo: String?,
    val telefono: String?,
    val foto: String?,
    @SerializedName("fechaCreacion") val fechaCreacion: String?
)

data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val password: String,
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

data class Empresa(
    val id: Int,
    val nombre: String,
    val identificacion: Long
)

data class Guia(
    val id: Int,
    val nombre: String,
    val cedula: Long,
    val telefono: String?,
    val correo: String?,
    val foto: String?,
    @SerializedName("id_empresa") val idEmpresa: Int?,
    @SerializedName("empresa_nombre") val empresaNombre: String?
)

data class GuiaRequest(
    val nombre: String,
    val cedula: Long,
    val telefono: String?,
    val correo: String?,
    val foto: String?,
    @SerializedName("id_empresa") val idEmpresa: Int?
)

data class TrekkingRoute(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("imageurl") val imageUrl: String,
    @SerializedName("description") val description: String,
    @SerializedName("height") val height: Int,
    @SerializedName("id_empresa") val companyId: Int?,
    @SerializedName("companyname") val companyName: String,
    @SerializedName("companyidentification") val companyIdentification: Long?,
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
