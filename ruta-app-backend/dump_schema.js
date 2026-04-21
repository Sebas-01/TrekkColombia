const { Client } = require('pg');
require('dotenv').config();

async function dumpSchema() {
  const client = new Client({
    user: process.env.DB_USER,
    host: process.env.DB_HOST,
    database: process.env.DB_NAME,
    password: process.env.DB_PASSWORD,
    port: process.env.DB_PORT,
  });

  try {
    await client.connect();
    const tables = ['rutas', 'usuarios', 'favoritos', 'empresas', 'guias'];
    for (const table of tables) {
      console.log(`\n--- Schema for table "${table}" ---`);
      const res = await client.query(`
        SELECT column_name, data_type, is_nullable
        FROM information_schema.columns
        WHERE table_name = '${table}'
        ORDER BY ordinal_position
      `);
      res.rows.forEach(col => {
        console.log(`Column: ${col.column_name} | Type: ${col.data_type} | Nullable: ${col.is_nullable}`);
      });
    }
  } catch (err) {
    console.error('Error:', err.message);
  } finally {
    await client.end();
  }
}

dumpSchema();
