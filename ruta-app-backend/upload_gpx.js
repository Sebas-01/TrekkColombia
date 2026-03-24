const fs = require('fs');
const path = require('path');
const http = require('http');

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
    longitude: -75.6366742 // Usando el mismo punto de inicio detectado
  }
];

async function uploadRoute(route) {
  return new Promise((resolve, reject) => {
    const gpxContent = fs.readFileSync(route.path, 'utf8');
    
    const data = JSON.stringify({
      title: route.title,
      imageUrl: `https://picsum.photos/seed/${Math.floor(Math.random() * 1000)}/400/600`,
      description: route.description,
      height: 2500, // Valor aproximado
      companyName: 'TrekColombia Adventures',
      difficulty: route.difficulty,
      duration: route.duration,
      guideName: 'Carlos Montoya',
      latitude: route.latitude,
      longitude: route.longitude,
      gpx: gpxContent
    });

    const options = {
      hostname: 'localhost',
      port: 3000,
      path: '/rutas',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': data.length
      }
    };

    const req = http.request(options, (res) => {
      let responseBody = '';
      res.on('data', (chunk) => { responseBody += chunk; });
      res.on('end', () => {
        if (res.statusCode === 201) {
          console.log(`✅ Ruta "${route.title}" subida correctamente.`);
          resolve();
        } else {
          console.error(`❌ Error al subir "${route.title}": ${res.statusCode}`);
          console.error(responseBody);
          reject(new Error(responseBody));
        }
      });
    });

    req.on('error', (error) => {
      console.error(`❌ Error de conexión al subir "${route.title}":`, error.message);
      reject(error);
    });

    req.write(data);
    req.end();
  });
}

async function run() {
  console.log('🚀 Iniciando subida de archivos GPX...');
  for (const route of GPX_FILES) {
    try {
      await uploadRoute(route);
    } catch (err) {
      console.error(`Saltando "${route.title}" debido a un error.`);
    }
  }
  console.log('🏁 Proceso finalizado.');
}

run();
