const express = require('express');
const bodyParser = require('body-parser');
const mysql = require('mysql2/promise');
const cors = require('cors');

const app = express();
app.use(bodyParser.json());
app.use(cors());

const DB_HOST = process.env.DB_HOST || 'localhost';
const DB_USER = process.env.DB_USER || 'root';
const DB_PASS = process.env.DB_PASS || 'example';
const DB_NAME = process.env.DB_NAME || 'tmanager';

let pool;

async function initDb() {
  pool = await mysql.createPool({
    host: DB_HOST,
    user: DB_USER,
    password: DB_PASS,
    database: DB_NAME,
    waitForConnections: true,
    connectionLimit: 10,
  });

  // Create minimal tables if not exists
  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id INT AUTO_INCREMENT PRIMARY KEY,
      uid VARCHAR(255) UNIQUE,
      nombre VARCHAR(255),
      email VARCHAR(255) UNIQUE,
      password VARCHAR(255),
      fotoUrl TEXT,
      rol VARCHAR(50),
      creado BIGINT
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS reservas_pagadas (
      id INT AUTO_INCREMENT PRIMARY KEY,
      userUid VARCHAR(255),
      club VARCHAR(255),
      pista VARCHAR(255),
      dia VARCHAR(64),
      hora VARCHAR(32),
      duracion VARCHAR(16),
      precio DOUBLE,
      metodoPago VARCHAR(128),
      estado VARCHAR(64),
      timestamp BIGINT
    )
  `);

  // Generic table for other collections can be added similarly
}

app.post('/api/auth/register', async (req, res) => {
  const { nombre, email, password, google } = req.body;
  try {
    const uid = email; // simple uid
    const creado = Date.now();
    await pool.query('INSERT IGNORE INTO users (uid, nombre, email, password, rol, creado) VALUES (?, ?, ?, ?, ?, ?)', [uid, nombre, email, password || null, 'user', creado]);
    res.json({ success: true, uid });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;
  try {
    const [rows] = await pool.query('SELECT uid FROM users WHERE email = ? AND (password = ? OR password IS NULL)', [email, password]);
    if (rows.length > 0) {
      res.json({ success: true, uid: rows[0].uid });
    } else {
      res.status(401).json({ success: false, error: 'Credenciales invalidas' });
    }
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/:collection', async (req, res) => {
  const { collection } = req.params;
  const body = req.body;
  try {
    if (collection === 'reservas_pagadas') {
      const q = 'INSERT INTO reservas_pagadas (userUid, club, pista, dia, hora, duracion, precio, metodoPago, estado, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)';
      await pool.query(q, [body.userUid, body.club, body.pista, body.dia, body.hora, body.duracion, body.precio, body.metodoPago, body.estado, body.timestamp]);
      res.json({ success: true });
      return;
    }

    if (collection === 'users') {
      const q = 'INSERT INTO users (uid, nombre, email, fotoUrl, rol, creado) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), fotoUrl = VALUES(fotoUrl)';
      await pool.query(q, [body.uid, body.nombre, body.email, body.fotoUrl != null ? body.fotoUrl : null, body.rol != null ? body.rol : 'user', Date.now()]);
      res.json({ success: true });
      return;
    }

    // For unknown collections, just echo back
    res.json({ success: true, received: body });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

const PORT = process.env.PORT || 3000;

initDb().then(() => {
  app.listen(PORT, () => console.log('Backend listening on port', PORT));
}).catch(err => {
  console.error('Error initializing DB', err);
  process.exit(1);
});

