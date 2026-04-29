const fs = require('fs');
const path = require('path');
const db = require('./db');

const MAPPING = [
  { id: 4, filename: '0428191731-63230.gpx' },
  { id: 5, filename: '0428191854-63230.gpx' },
  { id: 6, filename: '0428192237-63230.gpx' }
];

async function updateRouteGeom(id, filename) {
  const filePath = path.join(__dirname, 'public', 'rutas', filename);
  if (!fs.existsSync(filePath)) {
    console.error(`Archivo no encontrado: ${filePath}`);
    return;
  }

  const gpx = fs.readFileSync(filePath, 'utf8');
  
  const trkptRegex = /<trkpt lat="([-0-9.]+)" lon="([-0-9.]+)">/g;
  const points = [];
  let match;
  while ((match = trkptRegex.exec(gpx)) !== null) {
    points.push(`${match[2]} ${match[1]}`); 
  }

  if (points.length < 2) return;

  const geomWkt = `LINESTRING(${points.join(', ')})`;
  
  try {
    const query = `
      UPDATE rutas 
      SET geom = ST_GeomFromText($1, 4326),
          latitude = $2,
          longitude = $3
      WHERE id = $4
    `;
    const firstPoint = points[0].split(' ');
    const lon = parseFloat(firstPoint[0]);
    const lat = parseFloat(firstPoint[1]);

    await db.query(query, [geomWkt, lat, lon, id]);
    console.log(`✅ Ruta ID ${id} actualizada con el archivo ${filename}`);
  } catch (err) {
    console.error(`❌ Error actualizando ruta ID ${id}:`, err.message);
  }
}

async function run() {
  console.log('🚀 Actualizando el resto de las rutas...');
  for (const item of MAPPING) {
    await updateRouteGeom(item.id, item.filename);
  }
  console.log('🏁 Proceso finalizado.');
  process.exit(0);
}

run();
