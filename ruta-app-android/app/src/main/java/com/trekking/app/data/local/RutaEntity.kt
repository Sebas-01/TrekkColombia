package com.trekking.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rutas_locales")
data class RutaEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String,
    val description: String,
    val height: Int,
    val companyName: String,
    val difficulty: String,
    val duration: String,
    val guideName: String,
    val latitude: Double,
    val longitude: Double,
    val geoJson: String?,
    val isFavorite: Boolean
)

fun RutaEntity.toTrekkingRoute(): com.trekking.app.api.TrekkingRoute {
    return com.trekking.app.api.TrekkingRoute(
        id = this.id,
        title = this.title,
        imageUrl = this.imageUrl,
        description = this.description,
        height = this.height,
        companyId = null, // No guardamos el ID de empresa en la entidad simple por ahora
        companyName = this.companyName,
        companyIdentification = null,
        difficulty = this.difficulty,
        duration = this.duration,
        guideName = this.guideName,
        latitude = this.latitude,
        longitude = this.longitude,
        geoJson = this.geoJson,
        isFavorite = this.isFavorite
    )
}
