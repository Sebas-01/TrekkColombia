const db = require('./db');
require('dotenv').config();

async function enrichData() {
  try {
    console.log('Iniciando enriquecimiento de datos corregido...');

    const empresasUpdates = [
      {
        pattern: '%Andes%',
        descripcion: 'Con más de 15 años de experiencia, somos la operadora líder en expediciones de alta montaña en Colombia. Especializados en el Nevado del Cocuy y el Parque Los Nevados, contamos con guías certificados por la UIAGM y equipos de última tecnología para garantizar tu seguridad y una experiencia inolvidable.',
        contacto: '+57 310 123 4567 | info@andesadventure.co',
        rnt: 'RNT 12345',
        logo_url: 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200'
      },
      {
        pattern: '%Trek%',
        descripcion: 'Apasionados por la biodiversidad de nuestro país. Ofrecemos caminatas ecológicas diseñadas para conectar con la naturaleza de forma sostenible. Expertos en avistamiento de aves en los Farallones de Cali y senderismo en los páramos de la zona andina. Nuestra misión es la conservación a través del turismo.',
        contacto: '+57 315 987 6543 | contacto@trekkingcolombia.com',
        rnt: 'RNT 67890',
        logo_url: 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200'
      },
      {
        pattern: '%Sierra%',
        descripcion: 'Te llevamos al corazón de la Sierra Nevada para descubrir los misterios de la Ciudad Perdida y la cultura de las comunidades Kogui y Arhuaco. Promovemos el turismo comunitario y el respeto por los sitios ancestrales.',
        contacto: '+57 320 555 4444 | reservas@explorasierra.co',
        rnt: 'RNT 11223',
        logo_url: 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200'
      },
      {
        pattern: '%Páramo%',
        descripcion: 'Especialistas en recorridos por ecosistemas de páramo y alta montaña. Nuestra prioridad es la educación ambiental y la protección del agua.',
        contacto: '+57 311 444 5555 | hola@paramotours.co',
        rnt: 'RNT 22334',
        logo_url: 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200'
      },
      {
        pattern: '%Inca%',
        descripcion: 'Expertos en rutas arqueológicas y caminos ancestrales. Nuestra ruta estrella es el Camino del Inca en Colombia.',
        contacto: '+57 300 111 2222 | info@incatrails.co',
        rnt: 'RNT 55667',
        logo_url: 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200'
      }
    ];

    for (const emp of empresasUpdates) {
      await db.query(
        `UPDATE empresas 
         SET descripcion = $1, contacto = $2, rnt = $3, logo_url = $4 
         WHERE nombre LIKE $5`,
        [emp.descripcion, emp.contacto, emp.rnt, emp.logo_url, emp.pattern]
      );
    }
    console.log('✅ Empresas enriquecidas con patrones de nombre.');

    console.log('Enriquecimiento finalizado exitosamente.');
  } catch (err) {
    console.error('❌ Error al enriquecer datos:', err);
  } finally {
    process.exit();
  }
}

enrichData();
