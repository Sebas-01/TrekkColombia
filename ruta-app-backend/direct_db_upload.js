const fs = require('fs');
const path = require('path');
const db = require('./db');
require('dotenv').config();

const GPX_FILES = [
  {
    path: 'C:\\Users\\SEBAS\\Downloads\\0311160258-48007.gpx',
    title: 'Trekking Cerro El Volador',
    description: 'Una ruta escénica por el Cerro El Volador en Medellín, ideal para caminatas ligeras y avistamiento de aves.',
    difficulty: 'Baja',
    duration: '1 hora',
    latitude: 6.2765337,
    longitude: -75.6306016
  },
  {
    path: 'C:\\Users\\SEBAS\\Downloads\\0318151307-48007.gpx',
    title: 'Ruta Pan de Azúcar',
    description: 'Ascenso desafiante al Cerro Pan de Azúcar. Disfruta de las mejores vistas panorámicas de la ciudad.',
    difficulty: 'Media',
    duration: '2.5 horas',
    latitude: 6.2762979,
    longitude: -75.6366742
  },
  {
    path: 'C:\\Users\\SEBAS\\Downloads\\0318151417-48007.gpx',
    title: 'Caminata Santa Elena',
    description: 'Recorrido por los senderos de Santa Elena, cuna de los silleteros. Aire puro y naturaleza exuberante.',
    difficulty: 'Media',
    duration: '3 horas',
    latitude: 6.2762979,
    longitude: -75.6366742
  }
];

function extractLineString(gpxContent) {
  const trkptRegex = /<trkpt lat="([-0-9.]+)" lon="([-0-9.]+)">/g;
  const points = [];
  let match;
  while ((match = trkptRegex.exec(gpxContent)) !== null) {
    points.push(`${match[2]} ${match[1]}`); // Longitud primero para PostGIS
  }
  
  if (points.length > 1) {
    return `LINESTRING(${points.join(', ')})`;
  }
  return null;
}

async function uploadDirectly() {
  console.log('🚀 Iniciando inserción directa en la base de datos...');
  
  for (const route of GPX_FILES) {
    try {
      const gpxContent = fs.readFileSync(route.path, 'utf8');
      const geomWkt = extractLineString(gpxContent);
      
      if (!geomWkt) {
        console.warn(`⚠️ No se encontraron puntos en el archivo para "${route.title}".`);
        continue;
      }

      const imageUrl = `https://picsum.photos/seed/${Math.floor(Math.random() * 1000)}/400/600`;
      
      const query = `
        INSERT INTO rutas (title, imageUrl, description, height, companyName, difficulty, duration, guideName, latitude, longitude, geom)
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, ST_GeomFromText($11, 4326))
        RETURNING id
      `;
      
      const params = [
        route.title, 
        imageUrl, 
        route.description, 
        2500, 
        'TrekColombia Adventures', 
        route.difficulty, 
        route.duration, 
        'Carlos Montoya', 
        route.latitude, 
        route.longitude, 
        geomWkt
      ];

      const result = await db.query(query, params);
      console.log(`✅ Ruta "${route.title}" insertada con ID: ${result.rows[0].id}`);
    } catch (err) {
      console.error(`❌ Error al insertar "${route.title}":`, err.message);
    }
  }
  
  console.log('🏁 Carga finalizada.');
  process.exit(0);
}

uploadDirectly();
