package com.trekking.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import okhttp3.Interceptor
import okhttp3.Response

object RetrofitClient {
    // URL de producción en Render
    const val BASE_URL = "https://trekking-backend-yxz0.onrender.com"

    fun getFullUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http")) return path
        
        // --- PARCHE PARA RENDER ---
        // Forzamos que 'logos' sea 'LOGOS' porque el servidor de Render es case-sensitive
        // y actualmente tiene la carpeta en mayúsculas.
        val normalizedPath = path.replace("logos/", "LOGOS/").replace("logos\\", "LOGOS\\")
        
        val cleanPath = if (normalizedPath.startsWith("/")) normalizedPath.substring(1) else normalizedPath
        val cleanBase = if (BASE_URL.endsWith("/")) BASE_URL else "$BASE_URL/"
        
        val finalUrl = "$cleanBase$cleanPath"
        android.util.Log.d("RetrofitClient", "URL Generada: $finalUrl")
        return finalUrl
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = SessionManager.getToken(com.trekking.app.TrekkingApplication.instance)
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        chain.proceed(newRequest)
    }

    val instance: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
