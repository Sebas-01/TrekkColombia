const db = require('./db');

async function testExternalLogo() {
    try {
        const testUrl = 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200';
        await db.query("UPDATE empresas SET logo_url = $1 WHERE nombre = 'Trekking Adventures'", [testUrl]);
        console.log('DB actualizada con imagen de prueba externa: ' + testUrl);
    } catch (err) {
        console.error('Error:', err);
    } finally {
        process.exit();
    }
}

testExternalLogo();
