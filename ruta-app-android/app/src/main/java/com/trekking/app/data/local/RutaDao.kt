package com.trekking.app.data.local

import androidx.room.*

@Dao
interface RutaDao {
    @Query("SELECT * FROM rutas_locales")
    suspend fun getAllRutas(): List<RutaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRutas(rutas: List<RutaEntity>)

    @Query("DELETE FROM rutas_locales")
    suspend fun deleteAllRutas()

    @Query("SELECT * FROM rutas_locales WHERE id = :rutaId")
    suspend fun getRutaById(rutaId: Int): RutaEntity?

    @Query("SELECT * FROM rutas_locales WHERE isFavorite = 1")
    suspend fun getFavoriteRutas(): List<RutaEntity>
}
