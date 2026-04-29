const db = require('./db');

async function checkRoutes() {
  try {
    const { rows } = await db.query('SELECT id, title, ST_AsText(geom) as geom_wkt FROM rutas');
    console.log('Rutas actuales:');
    rows.forEach(row => {
      console.log(`ID: ${row.id} | Título: ${row.title} | Geom: ${row.geom_wkt ? 'TIENE' : 'VACÍO'}`);
    });
    process.exit(0);
  } catch (err) {
    console.error(err);
    process.exit(1);
  }
}

checkRoutes();
