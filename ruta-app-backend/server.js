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
  const createTableQuery = `
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
  `;
  try {
    await db.query(createTableQuery);

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

    console.log('Tabla "usuarios" verificada/creada');
  } catch (err) {
    console.error('Error al inicializar la tabla:', err);
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
