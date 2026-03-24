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
      rol VARCHAR(50) DEFAULT 'usuario',
      fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS rutas (
      id SERIAL PRIMARY KEY,
      title VARCHAR(255) NOT NULL,
      imageUrl TEXT,
      description TEXT,
      height INTEGER,
      companyName VARCHAR(255),
      difficulty VARCHAR(50),
      duration VARCHAR(50),
      guideName VARCHAR(255),
      latitude DOUBLE PRECISION DEFAULT 0.0,
      longitude DOUBLE PRECISION DEFAULT 0.0,
      geom GEOMETRY(LineString, 4326)
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

    if (!columnNames.includes('rol')) {
      await db.query("ALTER TABLE usuarios ADD COLUMN rol VARCHAR(50) DEFAULT 'usuario'");
      console.log('Columna "rol" añadida.');
    }
    if (!columnNames.includes('fecha_creacion')) {
      await db.query("ALTER TABLE usuarios ADD COLUMN fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
      console.log('Columna "fecha_creacion" añadida.');
    }

    // Columna geom en rutas (si no existe)
    const routesColumns = await db.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'rutas'");
    const routesColumnNames = routesColumns.rows.map(c => c.column_name);
    if (!routesColumnNames.includes('geom')) {
      await db.query("ALTER TABLE rutas ADD COLUMN geom GEOMETRY(LineString, 4326)");
      console.log('Columna "geom" añadida a rutas.');
    }

    // Seed routes if empty
    const routesCheck = await db.query("SELECT count(*) FROM rutas");
    if (parseInt(routesCheck.rows[0].count) === 0) {
      const seedRoutesQuery = `
        INSERT INTO rutas (title, imageUrl, description, height, companyName, difficulty, duration, guideName) VALUES
        ('Camino del Inca', 'https://picsum.photos/seed/1/400/600', 'Una ruta milenaria que atraviesa los Andes hasta llegar a Machu Picchu.', 300, 'Inca Trails Ltd.', 'Alta', '4 días', 'Juan Pérez'),
        ('Nevado del Cocuy', 'https://picsum.photos/seed/2/400/400', 'Nieve en el trópico colombiano. Siente la magia de los glaciares.', 200, 'Andes Adventures', 'Muy Alta', '2 días', 'María García'),
        ('Ciudad Perdida', 'https://picsum.photos/seed/3/400/700', 'Tesoro arqueológico en la Sierra Nevada de Santa Marta.', 350, 'Sierra Treks', 'Media', '5 días', 'Carlos Ruiz'),
        ('Páramo de Santurbán', 'https://picsum.photos/seed/4/400/500', 'Tierra de frailejones y nacimientos de agua cristalina.', 250, 'Páramo Tours', 'Media', '1 día', 'Elena Blanco'),
        ('Desierto de la Tatacoa', 'https://picsum.photos/seed/5/400/600', 'Un laberinto de tierra roja bajo cielos estrellados.', 280, 'Desert Guides', 'Baja', '1 día', 'Felipe Mora'),
        ('Valle del Cocora', 'https://picsum.photos/seed/6/400/450', 'Hogar de la palma de cera, árbol nacional de Colombia.', 220, 'Quindío Nature', 'Media', '6 horas', 'Sofía Vargas');
      `;
      await db.query(seedRoutesQuery);
      console.log('Rutas iniciales creadas.');
    }

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
  const { nombre, telefono, correo, password, foto, rol } = req.body;
  try {
    const hashedPassword = await bcrypt.hash(password, 10);
    const result = await db.query(
      'INSERT INTO usuarios (nombre, telefono, correo, password, foto, rol) VALUES ($1, $2, $3, $4, $5, $6) RETURNING idusuario',
      [nombre, telefono, correo, hashedPassword, foto, rol || 'usuario']
    );
    res.status(201).json({ idUsuario: result.rows[0].idusuario });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al crear usuario' });
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
    const token = jwt.sign({ id: user.idusuario, rol: user.rol }, process.env.JWT_SECRET, { expiresIn: '1h' });
    console.log('Login exitoso');
    res.json({
      token,
      idUsuario: user.idusuario,
      nombre: user.nombre,
      rol: user.rol,
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
    let query = 'SELECT * FROM rutas';
    let params = [];
    
    if (idUsuario) {
      query = `
        SELECT r.*, ST_AsGeoJSON(r.geom) as geojson,
        CASE WHEN f.idruta IS NOT NULL THEN TRUE ELSE FALSE END as isFavorite
        FROM rutas r
        LEFT JOIN favoritos f ON r.id = f.idruta AND f.idusuario = $1
      `;
      params = [idUsuario];
    } else {
      query = 'SELECT *, ST_AsGeoJSON(geom) as geojson FROM rutas';
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
  const { title, gpx } = req.body;
  console.log(`Título: ${title}, Longitud GPX: ${gpx ? gpx.length : 0}`);
  
  const { imageUrl, description, height, companyName, difficulty, duration, guideName, latitude, longitude } = req.body;
  
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
      INSERT INTO rutas (title, imageUrl, description, height, companyName, difficulty, duration, guideName, latitude, longitude, geom)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, ${geomWkt ? 'ST_GeomFromText($11, 4326)' : 'NULL'})
      RETURNING id
    `;
    
    const params = [title, imageUrl, description, height, companyName, difficulty, duration, guideName, latitude, longitude];
    if (geomWkt) params.push(geomWkt);

    const { rows } = await db.query(query, params);
    res.status(201).json({ id: rows[0].id, message: 'Ruta creada correctamente' });
  } catch (err) {
    console.error('Error al crear ruta:', err);
    res.status(500).json({ error: 'Error al crear la ruta', details: err.message });
  }
});

// --- Endpoints de Favoritos ---

app.get('/favoritos/:idUsuario', async (req, res) => {
  try {
    const { rows } = await db.query(
      'SELECT r.*, TRUE as isFavorite FROM rutas r JOIN favoritos f ON r.id = f.idruta WHERE f.idusuario = $1',
      [req.params.idUsuario]
    );
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
