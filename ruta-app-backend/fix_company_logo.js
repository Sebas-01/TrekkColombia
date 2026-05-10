const db = require('./db');

async function fixCompany() {
    try {
        await db.query(`
            UPDATE empresas 
            SET nombre = 'Trekking Adventures', 
                logo_url = '/logos/trekkColombia-adventure-LOGO.png' 
            WHERE nombre LIKE '%TrekColombia%' OR nombre = 'Trekking Adventures'
        `);
        console.log('Empresa actualizada correctamente en la DB.');
    } catch (err) {
        console.error('Error:', err);
    } finally {
        process.exit();
    }
}

fixCompany();
