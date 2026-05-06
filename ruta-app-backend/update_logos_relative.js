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

async function updateLogosRelative() {
  for (const [nombre, logo] of Object.entries(mapping)) {
    const relativeUrl = "/logos/" + logo;
    try {
      await db.query("UPDATE empresas SET logo_url = $1 WHERE nombre = $2", [relativeUrl, nombre]);
      console.log(`Actualizado logo (relativo) para ${nombre}: ${relativeUrl}`);
    } catch (err) {
      console.error(`Error actualizando ${nombre}:`, err);
    }
  }
  process.exit(0);
}

updateLogosRelative();
