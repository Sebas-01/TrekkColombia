package com.trekking.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EmpresaDao {
    @Query("SELECT * FROM empresas_locales WHERE id = :id")
    suspend fun getEmpresaById(id: Int): EmpresaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmpresa(empresa: EmpresaEntity)

    @Query("DELETE FROM empresas_locales")
    suspend fun deleteAll()
}
