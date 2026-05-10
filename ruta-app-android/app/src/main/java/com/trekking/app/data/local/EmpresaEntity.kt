package com.trekking.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.trekking.app.api.Empresa

@Entity(tableName = "empresas_locales")
data class EmpresaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val logoUrl: String?,
    val rnt: String?,
    val descripcion: String?,
    val contacto: String?
)

fun EmpresaEntity.toEmpresa(): Empresa {
    return Empresa(
        id = this.id,
        nombre = this.nombre,
        logoUrl = this.logoUrl,
        rnt = this.rnt,
        descripcion = this.descripcion,
        contacto = this.contacto
    )
}

fun Empresa.toEntity(): EmpresaEntity {
    return EmpresaEntity(
        id = this.id,
        nombre = this.nombre,
        logoUrl = this.logoUrl,
        rnt = this.rnt,
        descripcion = this.descripcion,
        contacto = this.contacto
    )
}
