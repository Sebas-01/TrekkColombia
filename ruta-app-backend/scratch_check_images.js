const db = require('./db');

async function checkImages() {
  try {
    const { rows } = await db.query('SELECT id, title, imageUrl FROM rutas');
    console.log('Rutas e imágenes actuales:');
    rows.forEach(row => {
      console.log(`ID: ${row.id} | Título: ${row.title} | Imagen: ${row.imageurl}`);
    });
    process.exit(0);
  } catch (err) {
    console.error(err);
    process.exit(1);
  }
}

checkImages();
