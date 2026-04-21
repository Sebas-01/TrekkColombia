const { Client } = require('pg');
require('dotenv').config();

async function applyPolicies() {
  const client = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false }
  });

  try {
    await client.connect();
    console.log('Connected to Supabase');

    const tables = ['rutas', 'empresas', 'guias', 'usuarios', 'favoritos', 'registros'];
    
    for (const table of tables) {
      console.log(`Setting up policies for "${table}"...`);
      
      // Enable RLS (just in case)
      await client.query(`ALTER TABLE "${table}" ENABLE ROW LEVEL SECURITY;`);

      // Drop existing read policies to avoid duplicates
      await client.query(`DROP POLICY IF EXISTS "Public access to ${table}" ON "${table}";`);

      // Create a policy for public read (Select)
      await client.query(`
        CREATE POLICY "Public access to ${table}" 
        ON "${table}" 
        FOR SELECT 
        USING (true);
      `);
      
      console.log(`✅ Policy created for "${table}"`);
    }

  } catch (err) {
    console.error('❌ Policy error:', err.message);
  } finally {
    await client.end();
  }
}

applyPolicies();
