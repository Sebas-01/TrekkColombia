const db = require('./db');

const mapping = {
  "Desert Guides": "desertGuides-LOGO.png",
  "Quindío Nature": "QuindioNature.LOGO.png",
  "Andes Adventures": "AndesAdventures-LOGO.png",
  "TrekColombia Adventures": "trekkColombia-adventure-LOGO.png",
  "Sierra Treks": "SierraTreks-LOGO.png",
  "Páramo Tours": "paramoTours-LOGO.png",
  "Inca Trails Ltd.": "InkaTrails-LOGO.png"
};

const baseUrl = "https://trekking-backend-yxz0.onrender.com/logos/";

async function updateLogos() {
  for (const [nombre, logo] of Object.entries(mapping)) {
    const url = baseUrl + logo;
    try {
      await db.query("UPDATE empresas SET logo_url = $1 WHERE nombre = $2", [url, nombre]);
      console.log(`Actualizado logo para ${nombre}: ${url}`);
    } catch (err) {
      console.error(`Error actualizando ${nombre}:`, err);
    }
  }
  process.exit(0);
}

updateLogos();
