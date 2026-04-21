const { Client } = require('pg');
require('dotenv').config();

async function checkLocalDB() {
  const client = new Client({
    user: process.env.DB_USER,
    host: process.env.DB_HOST,
    database: process.env.DB_NAME,
    password: process.env.DB_PASSWORD,
    port: process.env.DB_PORT,
  });

  try {
    await client.connect();
    console.log('Connected to LOCAL Database');
    const res = await client.query(`
      SELECT tablename 
      FROM pg_tables 
      WHERE schemaname = 'public'
    `);
    console.log('Tables found in LOCAL "public" schema:');
    for (const row of res.rows) {
      const countRes = await client.query(`SELECT COUNT(*) FROM "${row.tablename}"`);
      console.log(` - ${row.tablename} (${countRes.rows[0].count} rows)`);
    }

    console.log('\n--- Schema for missing table "registros" ---');
    const schemaRes = await client.query(`
      SELECT column_name, data_type, is_nullable, column_default
      FROM information_schema.columns
      WHERE table_name = 'registros'
      ORDER BY ordinal_position
    `);
    schemaRes.rows.forEach(col => {
      console.log(`Column: ${col.column_name} | Type: ${col.data_type} | Nullable: ${col.is_nullable} | Default: ${col.column_default}`);
    });
  } catch (err) {
    console.error('Error connecting to LOCAL Database:', err.message);
  } finally {
    await client.end();
  }
}

checkLocalDB();
