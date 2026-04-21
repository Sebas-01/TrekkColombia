const { Client } = require('pg');
require('dotenv').config();

async function runSchemaSync() {
  const client = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false }
  });

  try {
    await client.connect();
    console.log('Connected to Supabase');

    console.log('--- Enabling PostGIS extension ---');
    await client.query('CREATE EXTENSION IF NOT EXISTS postgis;');
    console.log('✅ PostGIS extension enabled (spatial_ref_sys created)');

    console.log('--- Creating table "registros" ---');
    await client.query(`
      CREATE TABLE IF NOT EXISTS registros (
        id SERIAL PRIMARY KEY,
        nombre_item TEXT NOT NULL,
        valor NUMERIC,
        creado_el TIMESTAMP DEFAULT NOW()
      );
    `);
    console.log('✅ Table "registros" created');

  } catch (err) {
    console.error('❌ Schema sync error:', err.message);
  } finally {
    await client.end();
  }
}

runSchemaSync();
