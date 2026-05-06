const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();
const db = require('./db');

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Middleware de log global
app.use((req, res, next) => {
  console.log(`[${new Date().toLocaleTimeString()}] ${req.method} ${req.url}`);
  next();
});

// --- Archivos estáticos ---
// Imágenes de rutas: GET /imagenes/nombre-archivo.jpg
// Coordenadas GPX:   GET /rutas/nombre-ruta.gpx
app.use(express.static('public'));

// --- Inicialización de la Base de Datos ---
const initDb = async () => {
  const createTablesQuery = `
    CREATE EXTENSION IF NOT EXISTS postgis;

    CREATE TABLE IF NOT EXISTS usuarios (
      idusuario SERIAL PRIMARY KEY,
      nombre VARCHAR(100) NOT NULL,
      telefono VARCHAR(20),
      correo VARCHAR(100) UNIQUE NOT NULL,
      password VARCHAR(255) NOT NULL,
      foto TEXT,
      fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS empresas (
      id SERIAL PRIMARY KEY,
      nombre VARCHAR(255) NOT NULL,
      identificacion VARCHAR(50) NOT NULL
    );

    CREATE TABLE IF NOT EXISTS rutas (
      id SERIAL PRIMARY KEY,
      title VARCHAR(255) NOT NULL,
      imageUrl TEXT,
      description TEXT,
      height INTEGER,
      id_empresa INTEGER REFERENCES empresas(id),
      difficulty VARCHAR(50),
      duration VARCHAR(50),
      guideName VARCHAR(255),
      latitude DOUBLE PRECISION DEFAULT 0.0,
      longitude DOUBLE PRECISION DEFAULT 0.0,
      geom GEOMETRY(LineString, 4326),
      recomendaciones TEXT
    );

    CREATE TABLE IF NOT EXISTS guias (
      id SERIAL PRIMARY KEY,
      nombre VARCHAR(255) NOT NULL,
      cedula BIGINT NOT NULL,
      telefono VARCHAR(20),
      correo VARCHAR(100),
      foto TEXT,
      id_empresa INTEGER REFERENCES empresas(id) ON DELETE SET NULL
    );

    CREATE TABLE IF NOT EXISTS favoritos (
      idusuario INTEGER REFERENCES usuarios(idusuario) ON DELETE CASCADE,
      idruta INTEGER REFERENCES rutas(id) ON DELETE CASCADE,
      PRIMARY KEY (idusuario, idruta)
    );
  `;
  try {
    await db.query(createTablesQuery);

    // Migración para bases de datos existentes: Añadir columnas si no existen
    const columns = await db.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'usuarios'");
    const columnNames = columns.rows.map(c => c.column_name);

    if (!columnNames.includes('fecha_creacion')) {
      await db.query("ALTER TABLE usuarios ADD COLUMN fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
      console.log('Columna "fecha_creacion" añadida.');
    }

    // --- Migración de Empresas ---
    const empresasColumns = await db.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'empresas'");
    const empresasColumnNames = empresasColumns.rows.map(c => c.column_name);

    if (!empresasColumnNames.includes('descripcion')) {
      await db.query("ALTER TABLE empresas ADD COLUMN descripcion TEXT");
      console.log('Columna "descripcion" añadida a empresas.');
    }
    if (!empresasColumnNames.includes('contacto')) {
      await db.query("ALTER TABLE empresas ADD COLUMN contacto VARCHAR(255)");
    }
    if (!empresasColumnNames.includes('logo_url')) {
      await db.query("ALTER TABLE empresas ADD COLUMN logo_url TEXT");
    }
    if (!empresasColumnNames.includes('rnt')) {
      await db.query("ALTER TABLE empresas ADD COLUMN rnt VARCHAR(50)");
    }

    // --- Migración de Rutas ---
    const routesColumns = await db.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'rutas'");
    const routesColumnNames = routesColumns.rows.map(c => c.column_name);

    if (!routesColumnNames.includes('id_empresa')) {
      await db.query("ALTER TABLE rutas ADD COLUMN id_empresa INTEGER REFERENCES empresas(id)");
      console.log('Columna "id_empresa" añadida a rutas.');
    }

    if (routesColumnNames.includes('companyname')) {
      // Migrar datos de companyName a la tabla empresas
      console.log('Migrando nombres de empresas a la nueva tabla...');
      const distinctCompanies = await db.query("SELECT DISTINCT companyName FROM rutas WHERE companyName IS NOT NULL AND companyName != ''");
      
      for (const row of distinctCompanies.rows) {
        const companyName = row.companyname;
        // Insertar empresa (usando un NIT ficticio basado en el nombre para la migración)
        const dummyNit = Math.floor(Math.random() * 900000000) + 100000000;
        const empResult = await db.query(
          "INSERT INTO empresas (nombre, identificacion) VALUES ($1, $2) ON CONFLICT DO NOTHING RETURNING id",
          [companyName, dummyNit]
        );
        
        let empId;
        if (empResult.rows.length > 0) {
          empId = empResult.rows[0].id;
        } else {
          const existingEmp = await db.query("SELECT id FROM empresas WHERE nombre = $1", [companyName]);
          empId = existingEmp.rows[0].id;
        }

        await db.query("UPDATE rutas SET id_empresa = $1 WHERE companyName = $2", [empId, companyName]);
      }

      // Eliminar la columna antigua
      await db.query("ALTER TABLE rutas DROP COLUMN companyName");
      console.log('Columna "companyName" eliminada y datos migrados.');
    }

    // Columna geom en rutas (si no existe)
    if (!routesColumnNames.includes('geom')) {
      await db.query("ALTER TABLE rutas ADD COLUMN geom GEOMETRY(LineString, 4326)");
      console.log('Columna "geom" añadida a rutas.');
    }

    if (!routesColumnNames.includes('recomendaciones')) {
      await db.query("ALTER TABLE rutas ADD COLUMN recomendaciones TEXT");
      console.log('Columna "recomendaciones" añadida a rutas.');
    }

    if (!routesColumnNames.includes('images')) {
      await db.query("ALTER TABLE rutas ADD COLUMN images TEXT[]");
      console.log('Columna "images" añadida a rutas.');
      
      // Poblar imágenes aleatorias para rutas existentes
      const fs = require('fs');
      const path = require('path');
      const imagenesDir = path.join(__dirname, 'public', 'imagenes');
      const files = fs.readdirSync(imagenesDir).filter(f => f.endsWith('.jpg') || f.endsWith('.png') || f.endsWith('.jpeg'));
      
      const routesResult = await db.query("SELECT id FROM rutas");
      for (const route of routesResult.rows) {
        // Mezclar y tomar 4
        const shuffled = files.sort(() => 0.5 - Math.random());
        const selected = shuffled.slice(0, 4).map(f => `https://trekking-backend-yxz0.onrender.com/imagenes/${f}`);
        await db.query("UPDATE rutas SET images = $1 WHERE id = $2", [selected, route.id]);
      }
      console.log('Imágenes de carrusel inicializadas para todas las rutas.');
    }

    // --- Migración: Tabla guias ---
    const guiasExists = await db.query(
      "SELECT to_regclass('public.guias') as exists"
    );
    if (!guiasExists.rows[0].exists) {
      console.log('Tabla "guias" no existe todavía, será creada en el siguiente arranque por CREATE TABLE IF NOT EXISTS.');
    } else {
      console.log('Tabla "guias" verificada correctamente.');
    }

    // Seed empresas y rutas si está vacío
    const empresasCheck = await db.query("SELECT count(*) FROM empresas");
    if (parseInt(empresasCheck.rows[0].count) === 0) {
      const seedEmpresasQuery = `
      INSERT INTO empresas (nombre, identificacion, descripcion, contacto, logo_url, rnt) VALUES
      ('Andes Adventure', 900123456, 'Expertos en alta montaña y expediciones en los Andes colombianos.', '+57 310 123 4567', 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200', 'RNT 12345'),
      ('Trekking Colombia', 900987654, 'Líderes en senderismo ecológico y avistamiento de aves.', '+57 315 987 6543', 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200', 'RNT 67890'),
      ('Explora Sierra', 900555444, 'Conectamos aventureros con la cultura y naturaleza de la Sierra Nevada.', '+57 320 555 4444', 'https://images.unsplash.com/photo-1599305090748-36656ca77449?q=80&w=200', 'RNT 11223')
      ON CONFLICT DO NOTHING;
    `;
    await db.query(seedEmpresasQuery);
      
      const seedEmpresas = await db.query("SELECT id, nombre FROM empresas");
      const empMap = {};
      seedEmpresas.rows.forEach(e => empMap[e.nombre] = e.id);

      const seedRoutesQuery = `
        INSERT INTO rutas (title, imageUrl, description, height, id_empresa, difficulty, duration, guideName, recomendaciones) VALUES
        ('Camino del Inca', 'https://picsum.photos/seed/1/400/600', 'Una ruta milenaria que atraviesa los Andes hasta llegar a Machu Picchu.', 300, ${empMap['Andes Adventure']}, 'Alta', '4 días', 'Juan Pérez', 'Llevar botas de trekking impermeables, ropa térmica para la noche y protector solar de alta protección.'),
        ('Nevado del Cocuy', 'https://picsum.photos/seed/2/400/400', 'Nieve en el trópico colombiano. Siente la magia de los glaciares.', 200, ${empMap['Andes Adventure']}, 'Muy Alta', '2 días', 'María García', 'Es obligatorio el uso de crampones y piolet. Recomendamos aclimatación previa en altitud.'),
        ('Ciudad Perdida', 'https://picsum.photos/seed/3/400/700', 'Tesoro arqueológico en la Sierra Nevada de Santa Marta.', 350, ${empMap['Explora Sierra']}, 'Media', '5 días', 'Carlos Ruiz', 'Mucha hidratación, repelente de insectos fuerte y calzado con buen agarre para el barro.'),
        ('Páramo de Santurbán', 'https://picsum.photos/seed/4/400/500', 'Tierra de frailejones y nacimientos de agua cristalina.', 250, ${empMap['Trekking Colombia']}, 'Media', '1 día', 'Elena Blanco', 'Llevar chaqueta rompevientos, guantes y no tocar los frailejones.');
      `;
      await db.query(seedRoutesQuery);
      console.log('Empresas y Rutas iniciales creadas.');
    }

    // Sincronizar secuencias para evitar conflictos de primary key
    await db.query("SELECT setval('usuarios_idusuario_seq', COALESCE((SELECT MAX(idusuario) FROM usuarios), 0) + 1, false)");
    await db.query("SELECT setval('rutas_id_seq', COALESCE((SELECT MAX(id) FROM rutas), 0) + 1, false)");
    await db.query("SELECT setval('empresas_id_seq', COALESCE((SELECT MAX(id) FROM empresas), 0) + 1, false)");
    console.log('Secuencias sincronizadas correctamente');

    console.log('Base de Datos inicializada correctamente');
  } catch (err) {
    console.error('Error al inicializar la base de datos:', err);
  }
};

initDb();

// --- Endpoints de CRUD de Usuarios ---

// Obtener todos los usuarios
app.get('/usuarios', async (req, res) => {
  try {
    const { rows } = await db.query('SELECT idusuario, nombre, telefono, correo, foto FROM usuarios');
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener usuarios' });
  }
});

// Registrar un usuario
app.post('/usuarios', async (req, res) => {
  const { nombre, telefono, correo, password, foto } = req.body;
  if (!nombre || !correo || !password) {
    return res.status(400).json({ error: 'Los campos nombre, correo y contraseña son obligatorios' });
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(correo)) {
    return res.status(400).json({ error: 'El formato del correo electrónico es inválido' });
  }
  try {
    const hashedPassword = await bcrypt.hash(password, 10);
    const result = await db.query(
      'INSERT INTO usuarios (nombre, telefono, correo, password, foto) VALUES ($1, $2, $3, $4, $5) RETURNING idusuario',
      [nombre, telefono, correo, hashedPassword, foto]
    );
    res.status(201).json({ idUsuario: result.rows[0].idusuario });
  } catch (err) {
    console.error('ERROR EN REGISTRO:', err);
    if (err.code === '23505') {
      return res.status(409).json({ error: 'El correo ya está registrado' });
    }
    res.status(500).json({ error: 'Error al crear usuario', details: err.message });
  }
});

// Login
app.post('/login', async (req, res) => {
  const { correo, password } = req.body;
  console.log(`Intento de login para: ${correo}`);
  try {
    const { rows } = await db.query('SELECT * FROM usuarios WHERE correo = $1', [correo]);
    if (rows.length === 0) {
      console.log('Usuario no encontrado');
      return res.status(401).json({ error: 'Usuario no encontrado' });
    }
    const user = rows[0];
    console.log('Usuario encontrado, verificando password...');
    const validPassword = await bcrypt.compare(password, user.password);
    if (!validPassword) {
      console.log('Password incorrecta');
      return res.status(401).json({ error: 'Contraseña incorrecta' });
    }
    const token = jwt.sign({ id: user.idusuario }, process.env.JWT_SECRET, { expiresIn: '1h' });
    console.log('Login exitoso');
    res.json({
      token,
      idUsuario: user.idusuario,
      nombre: user.nombre,
      correo: user.correo,
      telefono: user.telefono,
      foto: user.foto,
      fechaCreacion: user.fecha_creacion
    });
  } catch (err) {
    console.error('ERROR EN LOGIN:', err);
    res.status(500).json({ error: 'Error en el login', details: err.message });
  }
});

// --- Endpoints de Rutas ---

app.get('/rutas', async (req, res) => {
  try {
    const idUsuario = req.query.idUsuario;
    let query = `
      SELECT r.*, e.nombre as companyName, e.identificacion as companyIdentification,
      e.logo_url as companyLogo, e.descripcion as companyDescription,
      ST_AsGeoJSON(r.geom) as geojson
      FROM rutas r
      LEFT JOIN empresas e ON r.id_empresa = e.id
    `;
    let params = [];
    
    if (idUsuario) {
      query = `
        SELECT r.*, e.nombre as companyName, e.identificacion as companyIdentification,
        e.logo_url as companyLogo, e.descripcion as companyDescription,
        ST_AsGeoJSON(r.geom) as geojson,
        CASE WHEN f.idruta IS NOT NULL THEN TRUE ELSE FALSE END as isFavorite
        FROM rutas r
        LEFT JOIN empresas e ON r.id_empresa = e.id
        LEFT JOIN favoritos f ON r.id = f.idruta AND f.idusuario = $1
      `;
      params = [idUsuario];
    }
    
    const { rows } = await db.query(query, params);
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener rutas' });
  }
});

app.post('/rutas', async (req, res) => {
  console.log('Recibida petición POST en /rutas');
  const { title, gpx, id_empresa } = req.body;
  console.log(`Título: ${title}, ID Empresa: ${id_empresa}, Longitud GPX: ${gpx ? gpx.length : 0}`);
  
  const { imageUrl, description, height, difficulty, duration, guideName, latitude, longitude, recomendaciones } = req.body;
  
  try {
    let geomWkt = null;
    if (gpx) {
      // Simple parsing of GPX to extract coordinates for LineString
      const trkptRegex = /<trkpt lat="([-0-9.]+)" lon="([-0-9.]+)">/g;
      const points = [];
      let match;
      while ((match = trkptRegex.exec(gpx)) !== null) {
        points.push(`${match[2]} ${match[1]}`); // Longitude first for PostGIS
      }
      
      console.log(`Puntos extraídos: ${points.length}`);
      
      if (points.length > 1) {
        geomWkt = `LINESTRING(${points.join(', ')})`;
      }
    }

    const query = `
      INSERT INTO rutas (title, imageUrl, description, height, id_empresa, difficulty, duration, guideName, latitude, longitude, geom, recomendaciones)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, ${geomWkt ? 'ST_GeomFromText($11, 4326)' : 'NULL'}, ${geomWkt ? '$12' : '$11'})
      RETURNING id
    `;
    
    const params = [title, imageUrl, description, height, id_empresa, difficulty, duration, guideName, latitude, longitude];
    if (geomWkt) params.push(geomWkt);
    params.push(recomendaciones);

    const { rows } = await db.query(query, params);
    res.status(201).json({ id: rows[0].id, message: 'Ruta creada correctamente' });
  } catch (err) {
    console.error('Error al crear ruta:', err);
    res.status(500).json({ error: 'Error al crear la ruta', details: err.message });
  }
});

// --- Endpoints de Empresas ---

app.get('/empresas', async (req, res) => {
  try {
    const { rows } = await db.query('SELECT * FROM empresas ORDER BY nombre ASC');
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener empresas' });
  }
});

app.get('/empresas/:id', async (req, res) => {
  const { id } = req.params;
  console.log(`Petición GET /empresas/${id}`);
  try {
    const result = await db.query('SELECT * FROM empresas WHERE id = $1', [parseInt(id)]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Empresa no encontrada' });
    }
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener empresa' });
  }
});

app.post('/empresas', async (req, res) => {
  const { nombre, identificacion } = req.body;
  try {
    const result = await db.query(
      'INSERT INTO empresas (nombre, identificacion) VALUES ($1, $2) RETURNING id',
      [nombre, identificacion]
    );
    res.status(201).json({ id: result.rows[0].id, message: 'Empresa creada correctamente' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al crear empresa' });
  }
});

// --- Endpoints de Guías ---

// Obtener todos los guías (con nombre de empresa)
app.get('/guias', async (req, res) => {
  try {
    const { rows } = await db.query(`
      SELECT g.*, e.nombre as empresa_nombre
      FROM guias g
      LEFT JOIN empresas e ON g.id_empresa = e.id
      ORDER BY g.nombre ASC
    `);
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener guías' });
  }
});

// Obtener guías por empresa
app.get('/guias/empresa/:idEmpresa', async (req, res) => {
  try {
    const { rows } = await db.query(
      `SELECT g.*, e.nombre as empresa_nombre
       FROM guias g
       LEFT JOIN empresas e ON g.id_empresa = e.id
       WHERE g.id_empresa = $1
       ORDER BY g.nombre ASC`,
      [req.params.idEmpresa]
    );
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener guías de la empresa' });
  }
});

// Obtener un guía por ID
app.get('/guias/:id', async (req, res) => {
  try {
    const { rows } = await db.query(
      `SELECT g.*, e.nombre as empresa_nombre
       FROM guias g
       LEFT JOIN empresas e ON g.id_empresa = e.id
       WHERE g.id = $1`,
      [req.params.id]
    );
    if (rows.length === 0) return res.status(404).json({ error: 'Guía no encontrado' });
    res.json(rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener el guía' });
  }
});

// Crear un guía
app.post('/guias', async (req, res) => {
  const { nombre, cedula, telefono, correo, foto, id_empresa } = req.body;
  if (!nombre || !cedula) {
    return res.status(400).json({ error: 'Los campos nombre y cedula son obligatorios' });
  }
  try {
    const result = await db.query(
      `INSERT INTO guias (nombre, cedula, telefono, correo, foto, id_empresa)
       VALUES ($1, $2, $3, $4, $5, $6) RETURNING id`,
      [nombre, cedula, telefono, correo, foto, id_empresa]
    );
    res.status(201).json({ id: result.rows[0].id, message: 'Guía creado correctamente' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al crear guía', details: err.message });
  }
});

// Actualizar un guía
app.put('/guias/:id', async (req, res) => {
  const { nombre, cedula, telefono, correo, foto, id_empresa } = req.body;
  try {
    const result = await db.query(
      `UPDATE guias
       SET nombre = $1, cedula = $2, telefono = $3, correo = $4, foto = $5, id_empresa = $6
       WHERE id = $7 RETURNING id`,
      [nombre, cedula, telefono, correo, foto, id_empresa, req.params.id]
    );
    if (result.rows.length === 0) return res.status(404).json({ error: 'Guía no encontrado' });
    res.json({ message: 'Guía actualizado correctamente' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al actualizar guía', details: err.message });
  }
});

// Eliminar un guía
app.delete('/guias/:id', async (req, res) => {
  try {
    const result = await db.query('DELETE FROM guias WHERE id = $1 RETURNING id', [req.params.id]);
    if (result.rows.length === 0) return res.status(404).json({ error: 'Guía no encontrado' });
    res.json({ message: 'Guía eliminado correctamente' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al eliminar guía' });
  }
});

// --- Endpoints de Favoritos ---

app.get('/favoritos/:idUsuario', async (req, res) => {
  try {
    const query = `
      SELECT r.*, e.nombre as companyName, e.identificacion as companyIdentification,
      e.logo_url as companyLogo, e.descripcion as companyDescription,
      ST_AsGeoJSON(r.geom) as geojson,
      TRUE as isFavorite
      FROM rutas r
      LEFT JOIN empresas e ON r.id_empresa = e.id
      JOIN favoritos f ON r.id = f.idruta
      WHERE f.idusuario = $1
    `;
    const { rows } = await db.query(query, [req.params.idUsuario]);
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener favoritos' });
  }
});

app.post('/favoritos', async (req, res) => {
  const { idUsuario, idRuta } = req.body;
  try {
    await db.query('INSERT INTO favoritos (idusuario, idruta) VALUES ($1, $2) ON CONFLICT DO NOTHING', [idUsuario, idRuta]);
    res.status(201).json({ message: 'Añadido a favoritos' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al añadir a favoritos' });
  }
});

app.delete('/favoritos/:idUsuario/:idRuta', async (req, res) => {
  try {
    await db.query('DELETE FROM favoritos WHERE idusuario = $1 AND idruta = $2', [req.params.idUsuario, req.params.idRuta]);
    res.json({ message: 'Eliminado de favoritos' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al eliminar de favoritos' });
  }
});

// --- Otros Endpoints ---

// Eliminar usuario
app.delete('/usuarios/:id', async (req, res) => {
  try {
    await db.query('DELETE FROM usuarios WHERE idusuario = $1', [req.params.id]);
    res.json({ message: 'Usuario eliminado' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al eliminar usuario' });
  }
});

// Actualizar usuario
app.put('/usuarios/:id', async (req, res) => {
  const { nombre, telefono, correo, password, foto } = req.body;
  try {
    let updateQuery = 'UPDATE usuarios SET nombre = $1, telefono = $2, correo = $3, foto = $4';
    let params = [nombre, telefono, correo, foto];

    if (password) {
      const hashedPassword = await bcrypt.hash(password, 10);
      updateQuery += ', password = $5 WHERE idusuario = $6';
      params.push(hashedPassword, req.params.id);
    } else {
      updateQuery += ' WHERE idusuario = $5';
      params.push(req.params.id);
    }

    await db.query(updateQuery, params);
    res.json({ message: 'Usuario actualizado correctamente' });
  } catch (err) {
    console.error('ERROR EN UPDATE:', err);
    res.status(500).json({ error: 'Error al actualizar usuario', details: err.message });
  }
});

app.listen(port, () => {
  console.log(`Servidor de Trekking corriendo en http://localhost:${port}`);
});
