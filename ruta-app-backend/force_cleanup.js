const { Client } = require('pg');
require('dotenv').config();

async function cleanDatabase() {
  const client = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false }
  });

  try {
    await client.connect();
    console.log('Conectado a Supabase para limpieza...');

    const queries = [
      "ALTER TABLE usuarios DROP COLUMN IF EXISTS foto",
      "ALTER TABLE usuarios DROP COLUMN IF EXISTS fecha_creacion",
      "ALTER TABLE usuarios DROP COLUMN IF EXISTS rol",
      "ALTER TABLE rutas DROP COLUMN IF EXISTS guidename",
      "ALTER TABLE guias DROP COLUMN IF EXISTS foto",
      "ALTER TABLE empresas DROP COLUMN IF EXISTS identificacion"
    ];

    for (const q of queries) {
      try {
        await client.query(q);
        console.log(`✅ Ejecutado: ${q}`);
      } catch (e) {
        console.log(`❌ Error en: ${q} -> ${e.message}`);
      }
    }

    console.log('\n--- Verificando esquema final ---');
    const tables = ['usuarios', 'rutas', 'empresas', 'guias'];
    for (const table of tables) {
      const res = await client.query(`
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = '${table}'
      `);
      console.log(`Tabla "${table}":`, res.rows.map(r => r.column_name).join(', '));
    }

  } catch (err) {
    console.error('Error fatal:', err.message);
  } finally {
    await client.end();
  }
}

cleanDatabase();
