const { Client } = require('pg');
require('dotenv').config();

async function migrate() {
  const localClient = new Client({
    user: process.env.DB_USER,
    host: process.env.DB_HOST,
    database: process.env.DB_NAME,
    password: process.env.DB_PASSWORD,
    port: process.env.DB_PORT,
  });

  const remoteClient = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false }
  });

  try {
    await localClient.connect();
    await remoteClient.connect();
    console.log('Connected to both databases');

    // 1. Migrate Empresas
    console.log('Migrating empresas...');
    const empresasRes = await localClient.query('SELECT * FROM empresas');
    for (const row of empresasRes.rows) {
      await remoteClient.query(`
        INSERT INTO empresas (id, nombre, identificacion)
        VALUES ($1, $2, $3)
        ON CONFLICT (id) DO UPDATE SET
          nombre = EXCLUDED.nombre,
          identificacion = EXCLUDED.identificacion
      `, [row.id, row.nombre, row.identificacion]);
    }
    console.log(`✅ Migrated ${empresasRes.rows.length} empresas`);

    // 2. Migrate Guias
    console.log('Migrating guias...');
    const guiasRes = await localClient.query('SELECT * FROM guias');
    for (const row of guiasRes.rows) {
      await remoteClient.query(`
        INSERT INTO guias (id, nombre, cedula, telefono, correo, foto, id_empresa)
        VALUES ($1, $2, $3, $4, $5, $6, $7)
        ON CONFLICT (id) DO UPDATE SET
          nombre = EXCLUDED.nombre,
          cedula = EXCLUDED.cedula,
          telefono = EXCLUDED.telefono,
          correo = EXCLUDED.correo,
          foto = EXCLUDED.foto,
          id_empresa = EXCLUDED.id_empresa
      `, [row.id, row.nombre, row.cedula, row.telefono, row.correo, row.foto, row.id_empresa]);
    }
    console.log(`✅ Migrated ${guiasRes.rows.length} guias`);

    // 3. Migrate Rutas
    console.log('Migrating rutas...');
    // We fetch geometry as WKT
    const rutasRes = await localClient.query('SELECT *, ST_AsText(geom) as geom_wkt FROM rutas');
    for (const row of rutasRes.rows) {
      await remoteClient.query(`
        INSERT INTO rutas (id, title, imageurl, description, height, difficulty, duration, guidename, latitude, longitude, geom, id_empresa, recomendaciones)
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, ST_GeomFromText($11, 4326), $12, $13)
        ON CONFLICT (id) DO UPDATE SET
          title = EXCLUDED.title,
          imageurl = EXCLUDED.imageurl,
          description = EXCLUDED.description,
          height = EXCLUDED.height,
          difficulty = EXCLUDED.difficulty,
          duration = EXCLUDED.duration,
          guidename = EXCLUDED.guidename,
          latitude = EXCLUDED.latitude,
          longitude = EXCLUDED.longitude,
          geom = EXCLUDED.geom,
          id_empresa = EXCLUDED.id_empresa,
          recomendaciones = EXCLUDED.recomendaciones
      `, [row.id, row.title, row.imageurl, row.description, row.height, row.difficulty, row.duration, row.guidename, row.latitude, row.longitude, row.geom_wkt, row.id_empresa, row.recomendaciones]);
    }
    console.log(`✅ Migrated ${rutasRes.rows.length} rutas`);

    // 4. Migrate Usuarios
    console.log('Migrating usuarios...');
    const usuariosRes = await localClient.query('SELECT * FROM usuarios');
    for (const row of usuariosRes.rows) {
      await remoteClient.query(`
        INSERT INTO usuarios (idusuario, nombre, telefono, correo, password, foto, fecha_creacion)
        VALUES ($1, $2, $3, $4, $5, $6, $7)
        ON CONFLICT (idusuario) DO UPDATE SET
          nombre = EXCLUDED.nombre,
          telefono = EXCLUDED.telefono,
          correo = EXCLUDED.correo,
          password = EXCLUDED.password,
          foto = EXCLUDED.foto,
          fecha_creacion = EXCLUDED.fecha_creacion
      `, [row.idusuario, row.nombre, row.telefono, row.correo, row.password, row.foto, row.fecha_creacion]);
    }
    console.log(`✅ Migrated ${usuariosRes.rows.length} usuarios`);

    // 5. Migrate Favoritos
    console.log('Migrating favoritos...');
    const favoritosRes = await localClient.query('SELECT * FROM favoritos');
    for (const row of favoritosRes.rows) {
      await remoteClient.query(`
        INSERT INTO favoritos (idusuario, idruta)
        VALUES ($1, $2)
        ON CONFLICT (idusuario, idruta) DO NOTHING
      `, [row.idusuario, row.idruta]);
    }
    console.log(`✅ Migrated ${favoritosRes.rows.length} favoritos`);

  } catch (err) {
    console.error('❌ Migration error:', err.message);
  } finally {
    await localClient.end();
    await remoteClient.end();
  }
}

migrate();
