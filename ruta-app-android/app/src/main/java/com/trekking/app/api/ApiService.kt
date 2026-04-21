package com.trekking.app.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/usuarios")
    suspend fun getUsuarios(): Response<List<Usuario>>

    @POST("/usuarios")
    suspend fun registerUsuario(@Body request: RegisterRequest): Response<Map<String, Int>>

    @PUT("/usuarios/{id}")
    suspend fun updateUsuario(@Path("id") id: Int, @Body request: UpdateRequest): Response<MessageResponse>

    @DELETE("/usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Int): Response<MessageResponse>

    @GET("/rutas")
    suspend fun getRutas(@Query("idUsuario") idUsuario: Int?): Response<List<TrekkingRoute>>

    @GET("/favoritos/{idUsuario}")
    suspend fun getFavoritos(@Path("idUsuario") idUsuario: Int): Response<List<TrekkingRoute>>

    @POST("/favoritos")
    suspend fun addFavorito(@Body request: FavoriteRequest): Response<MessageResponse>

    @DELETE("/favoritos/{idUsuario}/{idRuta}")
    suspend fun removeFavorito(@Path("idUsuario") idUsuario: Int, @Path("idRuta") idRuta: Int): Response<MessageResponse>

    // --- Guías ---
    @GET("/guias")
    suspend fun getGuias(): Response<List<Guia>>

    @GET("/guias/{id}")
    suspend fun getGuiaById(@Path("id") id: Int): Response<Guia>

    @GET("/guias/empresa/{idEmpresa}")
    suspend fun getGuiasByEmpresa(@Path("idEmpresa") idEmpresa: Int): Response<List<Guia>>

    @POST("/guias")
    suspend fun createGuia(@Body request: GuiaRequest): Response<Map<String, Any>>

    @PUT("/guias/{id}")
    suspend fun updateGuia(@Path("id") id: Int, @Body request: GuiaRequest): Response<MessageResponse>

    @DELETE("/guias/{id}")
    suspend fun deleteGuia(@Path("id") id: Int): Response<MessageResponse>
}
