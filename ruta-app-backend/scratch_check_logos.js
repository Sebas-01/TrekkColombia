const db = require('./db');

async function checkLogos() {
    try {
        const { rows } = await db.query('SELECT id, nombre, logo_url FROM empresas');
        console.log('DATOS DE EMPRESAS EN DB:');
        console.table(rows);
    } catch (err) {
        console.error('Error:', err);
    } finally {
        process.exit();
    }
}

checkLogos();
