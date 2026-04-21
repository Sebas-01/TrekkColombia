const { Pool } = require('pg');
require('dotenv').config();

// Supabase / Producción: usar DATABASE_URL (connection string completo)
// Desarrollo local: usar variables individuales
const pool = process.env.DATABASE_URL
  ? new Pool({
      connectionString: process.env.DATABASE_URL,
      ssl: { rejectUnauthorized: false }, // requerido por Supabase
    })
  : new Pool({
      user: process.env.DB_USER,
      host: process.env.DB_HOST,
      database: process.env.DB_NAME,
      password: process.env.DB_PASSWORD,
      port: process.env.DB_PORT,
    });

pool.on('connect', () => {
    console.log('Conectado a la base de datos PostgreSQL');
});

module.exports = {
    query: (text, params) => pool.query(text, params),
};
