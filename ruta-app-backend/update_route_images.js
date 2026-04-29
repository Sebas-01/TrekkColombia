const db = require('./db');

const BASE_URL = "https://trekking-backend-yxz0.onrender.com";

const IMAGES_MAPPING = [
  { id: 1, filename: "pexels-136932883-10254835.jpg" },
  { id: 2, filename: "pexels-alialcantara-35746728.jpg" },
  { id: 3, filename: "pexels-gala-briela-842980063-35925134.jpg" },
  { id: 4, filename: "pexels-luka-peric-555685716-17317399.jpg" },
  { id: 5, filename: "pexels-marcelo-mora-203572590-33568683.jpg" },
  { id: 6, filename: "pexels-mralexphotography-32278102.jpg" },
  { id: 7, filename: "pexels-ozgur-surmeli-124841273-33984261.jpg" },
  { id: 8, filename: "pexels-136932883-10254835.jpg" },
  { id: 9, filename: "pexels-alialcantara-35746728.jpg" }
];

async function updateRouteImages() {
  console.log('🚀 Iniciando actualización de imágenes de rutas...');
  try {
    for (const item of IMAGES_MAPPING) {
      const fullUrl = `${BASE_URL}/imagenes/${item.filename}`;
      const query = 'UPDATE rutas SET imageUrl = $1 WHERE id = $2';
      await db.query(query, [fullUrl, item.id]);
      console.log(`✅ Ruta ID ${item.id} actualizada con imagen: ${item.filename}`);
    }
    console.log('🏁 Proceso finalizado.');
    process.exit(0);
  } catch (err) {
    console.error('❌ Error actualizando imágenes:', err.message);
    process.exit(1);
  }
}

updateRouteImages();
