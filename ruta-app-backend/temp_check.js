const { Client } = require('pg');
require('dotenv').config();

async function checkTables() {
  const client = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false }
  });

  try {
    await client.connect();
    console.log('Connected to Supabase');
    const res = await client.query(`
      SELECT tablename, rowsecurity 
      FROM pg_tables 
      WHERE schemaname = 'public'
    `);
    console.log('Tables found in "public" schema:');
    if (res.rows.length === 0) {
      console.log('No tables found.');
    } else {
      for (const row of res.rows) {
        const countRes = await client.query(`SELECT COUNT(*) FROM "${row.tablename}"`);
        
        // Check for primary key
        const pkRes = await client.query(`
          SELECT a.attname
          FROM   pg_index i
          JOIN   pg_attribute a ON a.attrelid = i.indrelid
                               AND a.attnum = ANY(i.indkey)
          WHERE  i.indrelid = '"${row.tablename}"'::regclass
          AND    i.indisprimary;
        `);
        const hasPk = pkRes.rows.length > 0;

        // Check for policies
        const polRes = await client.query(`
          SELECT count(*) FROM pg_policy WHERE polrelid = '"${row.tablename}"'::regclass
        `);
        const polCount = polRes.rows[0].count;

        console.log(` - ${row.tablename} (${countRes.rows[0].count} rows) | RLS: ${row.rowsecurity ? 'ON' : 'OFF'} | Policies: ${polCount} | PK: ${hasPk ? 'YES' : 'NO'}`);
      }
    }
  } catch (err) {
    console.error('Error connecting to Supabase:', err.message);
  } finally {
    await client.end();
  }
}

checkTables();
