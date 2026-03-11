-- Script para crear la tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    idUsuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    correo VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    foto TEXT,
    rol VARCHAR(50) DEFAULT 'usuario',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Verificación de la estructura
-- \d usuarios