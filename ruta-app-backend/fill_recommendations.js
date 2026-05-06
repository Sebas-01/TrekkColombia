const db = require('./db');

const updates = [
  {
    id: 8,
    reco: "• Preparación: Es una caminata de intensidad media con pendientes pronunciadas. Se recomienda buen estado físico.\n• Hidratación: Llevar al menos 2 litros de agua por persona. No hay puntos de hidratación en la cima.\n• Equipo: Usa calzado con buen agarre, ya que el terreno puede estar resbaladizo si ha llovido.\n• Seguridad: Se recomienda realizar el ascenso en las primeras horas de la mañana para evitar el sol fuerte."
  },
  {
    id: 7,
    reco: "• Comodidad: Es un parque arqueológico urbano, ideal para senderismo ligero. Usa protector solar y gorra.\n• Avistamiento: Lleva binoculares si te gusta el avistamiento de aves; es un punto clave en la ciudad.\n• Horario: El parque tiene horarios de cierre, verifica antes de ir. Es ideal para ver el atardecer.\n• Restricciones: No se permite arrojar basura ni extraer material arqueológico o flora."
  },
  {
    id: 9,
    reco: "• Clima: El clima es frío y nublado frecuentemente. Lleva chaqueta impermeable y ropa abrigada.\n• Respeto: Estás en territorio silletero. Respeta los cultivos de flores y la propiedad privada.\n• Navegación: Los senderos pueden ser confusos con la neblina. Se recomienda ir con guía o GPS cargado.\n• Logística: Considera el transporte de regreso a Medellín con antelación, especialmente los fines de semana."
  }
];

async function updateRecommendations() {
  for (const item of updates) {
    try {
      await db.query("UPDATE rutas SET recomendaciones = $1 WHERE id = $2", [item.reco, item.id]);
      console.log(`Actualizada ruta ${item.id}`);
    } catch (err) {
      console.error(`Error en ruta ${item.id}:`, err);
    }
  }
  process.exit(0);
}

updateRecommendations();
