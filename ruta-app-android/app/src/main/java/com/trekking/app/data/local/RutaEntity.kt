package com.trekking.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rutas_locales")
data class RutaEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String?,
    val description: String?,
    val height: Int?,
    val companyId: Int?,
    val companyName: String,
    val difficulty: String?,
    val duration: String?,
    val latitude: Double?,
    val longitude: Double?,
    val geoJson: String?,
    val isFavorite: Boolean,
    val recomendaciones: String?,
    val companyLogo: String?,
    val companyDescription: String?
)

fun RutaEntity.toTrekkingRoute(): com.trekking.app.api.TrekkingRoute {
    return com.trekking.app.api.TrekkingRoute(
        id = this.id,
        title = this.title,
        imageUrl = this.imageUrl,
        description = this.description,
        height = this.height,
        companyId = this.companyId, 
        companyName = this.companyName,
        difficulty = this.difficulty,
        duration = this.duration,
        latitude = this.latitude,
        longitude = this.longitude,
        geoJson = this.geoJson,
        isFavorite = this.isFavorite,
        recomendaciones = this.recomendaciones,
        companyLogo = this.companyLogo,
        companyDescription = this.companyDescription,
        images = emptyList() // No soportamos lista de imágenes en caché local por ahora sin TypeConverter
    )
}
