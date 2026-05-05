const db = require('./db');
require('dotenv').config();

async function enrichData() {
  try {
    console.log('Iniciando enriquecimiento de datos...');

    // 1. Enriquecer Empresas
    const empresasUpdates = [
      {
        nombre: 'Andes Adventure',
        descripcion: 'Con más de 15 años de experiencia, somos la operadora líder en expediciones de alta montaña en Colombia. Especializados en el Nevado del Cocuy y el Parque Los Nevados, contamos con guías certificados por la UIAGM y equipos de última tecnología para garantizar tu seguridad y una experiencia inolvidable.',
        contacto: '+57 310 123 4567 | info@andesadventure.co',
        rnt: 'RNT 12345',
        logo_url: 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200'
      },
      {
        nombre: 'Trekking Colombia',
        descripcion: 'Apasionados por la biodiversidad de nuestro país. Ofrecemos caminatas ecológicas diseñadas para conectar con la naturaleza de forma sostenible. Expertos en avistamiento de aves en los Farallones de Cali y senderismo en los páramos de la zona andina. Nuestra misión es la conservación a través del turismo.',
        contacto: '+57 315 987 6543 | contacto@trekkingcolombia.com',
        rnt: 'RNT 67890',
        logo_url: 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200'
      },
      {
        nombre: 'Explora Sierra',
        descripcion: 'Operadora local basada en Santa Marta. Te llevamos al corazón de la Sierra Nevada para descubrir los misterios de la Ciudad Perdida y la cultura de las comunidades Kogui y Arhuaco. Promovemos el turismo comunitario y el respeto por los sitios ancestrales.',
        contacto: '+57 320 555 4444 | reservas@explorasierra.co',
        rnt: 'RNT 11223',
        logo_url: 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200'
      }
    ];

    for (const emp of empresasUpdates) {
      await db.query(
        `UPDATE empresas 
         SET descripcion = $1, contacto = $2, rnt = $3, logo_url = $4 
         WHERE nombre = $5`,
        [emp.descripcion, emp.contacto, emp.rnt, emp.logo_url, emp.nombre]
      );
    }
    console.log('✅ Empresas enriquecidas.');

    // 2. Enriquecer Rutas (Recomendaciones detalladas)
    const rutasUpdates = [
      {
        title: 'Camino del Inca',
        recomendaciones: '• Equipo: Botas de trekking con buen agarre e impermeables. Ropa térmica por capas.\n• Salud: Es vital la aclimatación previa (mínimo 2 días en Cusco). Beber mucha agua y té de coca.\n• Documentación: Llevar el pasaporte original (es obligatorio para los puntos de control).\n• Respeto: No arrojar basura y permanecer en los senderos señalizados.'
      },
      {
        title: 'Nevado del Cocuy',
        recomendaciones: '• Preparación: Esta es una ruta de alta exigencia física. Se recomienda entrenamiento cardiovascular previo.\n• Obligatorio: Registro en Parques Nacionales y contar con seguro de rescate.\n• Equipo: Gafas de sol con protección UV400, bloqueador solar factor 50+, guantes térmicos y chaqueta tipo hard-shell.\n• Importante: El ascenso está prohibido si presentas síntomas de mal de altura agudo.'
      },
      {
        title: 'Ciudad Perdida',
        recomendaciones: '• Clima: Prepárate para humedad extrema y lluvia. Usa bolsas secas para tu ropa.\n• Salud: Recomendamos vacuna contra la fiebre amarilla y llevar repelente contra insectos potente.\n• Calzado: Botas que ya hayas usado antes para evitar ampollas. Sandalias para los descansos en los campamentos.\n• Cultura: Siempre pide permiso antes de tomar fotos a los indígenas locales.'
      },
      {
        title: 'Páramo de Santurbán',
        recomendaciones: '• Ecosistema: El páramo es un sistema frágil. No pises ni toques los frailejones.\n• Clima: Cambia muy rápido. Siempre lleva impermeable aunque el día parezca soleado.\n• Hidratación: Lleva al menos 2 litros de agua. El frío puede disminuir la sensación de sed pero la deshidratación es real.'
      }
    ];

    for (const ruta of rutasUpdates) {
      await db.query(
        `UPDATE rutas 
         SET recomendaciones = $1 
         WHERE title = $2`,
        [ruta.recomendaciones, ruta.title]
      );
    }
    console.log('✅ Rutas enriquecidas con recomendaciones.');

    console.log('Enriquecimiento finalizado exitosamente.');
  } catch (err) {
    console.error('❌ Error al enriquecer datos:', err);
  } finally {
    process.exit();
  }
}

enrichData();
