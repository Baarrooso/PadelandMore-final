const express = require('express');
const bodyParser = require('body-parser');
const mysql = require('mysql2/promise');
const cors = require('cors');
const { randomUUID } = require('crypto');
const fs = require('fs');
require('dotenv').config();

const app = express();
app.use(bodyParser.json({ limit: '2mb' }));
app.use(cors());

const DB_HOST = process.env.DB_HOST || 'localhost';
const DB_USER = process.env.DB_USER || 'root';
const DB_PASS = process.env.DB_PASSWORD || process.env.DB_PASS || 'example';
const DB_NAME = process.env.DB_NAME || 'padelandmore';
const DB_PORT = process.env.DB_PORT || 3306;
const DB_SSL_CA = process.env.DB_SSL_CA || null;

const TABLES = {
  users: 'users',
  reservas_padel: 'reservas_padel',
  reservas_clases: 'reservas_clases',
  reservas_pagadas: 'reservas_pagadas',
  torneos_padel: 'torneos_padel',
  inscripciones_torneo_padel: 'inscripciones_torneo_padel',
  seguimientos: 'seguimientos',
  partidos: 'partidos',
  documents: 'documents',
};

const COLLECTION_TO_TABLE = {
  users: TABLES.users,
  reservas_padel: TABLES.reservas_padel,
  reservas_clases: TABLES.reservas_clases,
  reservas_pagadas: TABLES.reservas_pagadas,
  torneos_padel: TABLES.torneos_padel,
  inscripciones_torneo_padel: TABLES.inscripciones_torneo_padel,
  seguimientos: TABLES.seguimientos,
  partidos: TABLES.partidos,
};

let pool;

function now() {
  return Date.now();
}

function toJsonValue(value) {
  if (value === undefined) {
    return null;
  }

  if (Array.isArray(value) || (value && typeof value === 'object')) {
    return JSON.stringify(value);
  }

  return value;
}

function serializeRow(row) {
  if (!row) {
    return row;
  }

  const output = { ...row };

  for (const key of ['inscritos', 'payload']) {
    if (typeof output[key] === 'string') {
      try {
        output[key] = JSON.parse(output[key]);
      } catch {
        // Keep the raw value when it is not valid JSON.
      }
    }
  }

  return output;
}

async function queryOne(sql, params = []) {
  const [rows] = await pool.query(sql, params);
  return rows[0] || null;
}

function buildPlaceholders(fields) {
  return fields.map(() => '?').join(', ');
}

function pickDefined(body, fields) {
  return fields.filter((field) => body[field] !== undefined);
}

function buildValues(body, fields) {
  return fields.map((field) => toJsonValue(body[field]));
}

async function initDb() {
  const poolConfig = {
    host: DB_HOST,
    user: DB_USER,
    password: DB_PASS,
    database: DB_NAME,
    port: DB_PORT,
    waitForConnections: true,
    connectionLimit: 10,
    charset: 'utf8mb4',
  };

  if (DB_SSL_CA) {
    try {
      poolConfig.ssl = {
        ca: fs.readFileSync(DB_SSL_CA)
      };
    } catch (err) {
      console.warn("No se pudo leer el archivo SSL CA:", DB_SSL_CA);
    }
  }

  pool = await mysql.createPool(poolConfig);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      uid VARCHAR(255) PRIMARY KEY,
      nombre VARCHAR(255) NOT NULL,
      email VARCHAR(255) NOT NULL UNIQUE,
      password VARCHAR(255) NULL,
      fotoUrl TEXT NULL,
      rol VARCHAR(50) NOT NULL DEFAULT 'none',
      equipoId VARCHAR(255) NULL,
      edad INT NULL,
      nivel VARCHAR(50) NULL,
      mano VARCHAR(50) NULL,
      nivelPadel DECIMAL(3,1) NULL,
      ciudadPadel VARCHAR(120) NULL,
      seguidosCount INT NOT NULL DEFAULT 0,
      seguidoresCount INT NOT NULL DEFAULT 0,
      creado BIGINT NOT NULL,
      actualizado BIGINT NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS reservas_padel (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      userUid VARCHAR(255) NOT NULL,
      club VARCHAR(255) NOT NULL,
      pista VARCHAR(255) NOT NULL,
      fecha VARCHAR(64) NOT NULL,
      hora VARCHAR(32) NOT NULL,
      duracion VARCHAR(32) NULL,
      precio DECIMAL(10,2) NULL,
      estado VARCHAR(64) NOT NULL DEFAULT 'pendiente',
      notas VARCHAR(500) NULL,
      creado BIGINT NOT NULL,
      actualizado BIGINT NOT NULL,
      INDEX idx_reservas_padel_user (userUid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS reservas_clases (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      userUid VARCHAR(255) NOT NULL,
      club VARCHAR(255) NOT NULL,
      pista VARCHAR(255) NOT NULL,
      fecha VARCHAR(64) NOT NULL,
      hora VARCHAR(32) NOT NULL,
      duracion VARCHAR(32) NULL,
      precio DECIMAL(10,2) NULL,
      estado VARCHAR(64) NOT NULL DEFAULT 'pendiente',
      notas VARCHAR(500) NULL,
      creado BIGINT NOT NULL,
      actualizado BIGINT NOT NULL,
      INDEX idx_reservas_clases_user (userUid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS reservas_pagadas (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      userUid VARCHAR(255) NOT NULL,
      club VARCHAR(255) NOT NULL,
      pista VARCHAR(255) NOT NULL,
      dia VARCHAR(64) NOT NULL,
      hora VARCHAR(32) NOT NULL,
      duracion VARCHAR(16) NOT NULL,
      precio DECIMAL(10,2) NOT NULL,
      metodoPago VARCHAR(128) NOT NULL,
      estado VARCHAR(64) NOT NULL,
      timestamp BIGINT NOT NULL,
      INDEX idx_reservas_pagadas_user (userUid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS torneos_padel (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      nombre VARCHAR(255) NOT NULL,
      ciudad VARCHAR(255) NOT NULL,
      fecha VARCHAR(64) NOT NULL,
      nivel VARCHAR(50) NOT NULL,
      inscritos JSON NULL,
      creado BIGINT NOT NULL,
      actualizado BIGINT NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS inscripciones_torneo_padel (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      userUid VARCHAR(255) NOT NULL,
      torneoResumen VARCHAR(500) NOT NULL,
      torneoId VARCHAR(255) NULL,
      creado BIGINT NOT NULL,
      INDEX idx_inscripciones_torneo_user (userUid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS seguimientos (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      followerUid VARCHAR(255) NOT NULL,
      followingUid VARCHAR(255) NOT NULL,
      createdAt BIGINT NOT NULL,
      UNIQUE KEY uq_seguimientos (followerUid, followingUid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS partidos (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      jugador1Uid VARCHAR(255) NOT NULL,
      jugador2Uid VARCHAR(255) NULL,
      jugador2Nombre VARCHAR(255) NULL,
      resultado VARCHAR(64) NOT NULL,
      fecha VARCHAR(64) NOT NULL,
      creado BIGINT NOT NULL,
      INDEX idx_partidos_jugador1 (jugador1Uid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS documents (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      collection_name VARCHAR(100) NOT NULL,
      document_id VARCHAR(255) NOT NULL,
      payload JSON NOT NULL,
      created_at BIGINT NOT NULL,
      updated_at BIGINT NOT NULL,
      UNIQUE KEY uq_documents_collection_id (collection_name, document_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);
}

async function upsertUser(body) {
  const uid = body.uid || body.email;

  if (!uid) {
    const error = new Error('Se requiere uid o email para registrar el usuario');
    error.statusCode = 400;
    throw error;
  }

  const createdAt = body.creado || body.createdAt || now();
  const updatedAt = now();
  const sql = `
    INSERT INTO users (
      uid, nombre, email, password, fotoUrl, rol, equipoId, edad, nivel, mano,
      nivelPadel, ciudadPadel, seguidosCount, seguidoresCount, creado, actualizado
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE
      nombre = VALUES(nombre),
      email = VALUES(email),
      password = VALUES(password),
      fotoUrl = VALUES(fotoUrl),
      rol = VALUES(rol),
      equipoId = VALUES(equipoId),
      edad = VALUES(edad),
      nivel = VALUES(nivel),
      mano = VALUES(mano),
      nivelPadel = VALUES(nivelPadel),
      ciudadPadel = VALUES(ciudadPadel),
      seguidosCount = VALUES(seguidosCount),
      seguidoresCount = VALUES(seguidoresCount),
      actualizado = VALUES(actualizado)
  `;

  const values = [
    uid,
    body.nombre || '',
    body.email || uid,
    body.password ?? null,
    body.fotoUrl ?? null,
    body.rol || 'none',
    body.equipoId ?? null,
    body.edad ?? null,
    body.nivel ?? null,
    body.mano ?? null,
    body.nivelPadel ?? null,
    body.ciudadPadel ?? null,
    body.seguidosCount ?? 0,
    body.seguidoresCount ?? 0,
    createdAt,
    updatedAt,
  ];

  await pool.query(sql, values);
  return queryOne('SELECT * FROM users WHERE uid = ?', [uid]);
}

async function insertGenericDocument(collection, body) {
  const documentId = body.documentId || body.id || body.uid || randomUUID();
  const payload = JSON.stringify(body);
  const createdAt = body.createdAt || body.creado || now();
  const updatedAt = body.updatedAt || body.actualizado || now();

  await pool.query(
    `
      INSERT INTO documents (collection_name, document_id, payload, created_at, updated_at)
      VALUES (?, ?, ?, ?, ?)
      ON DUPLICATE KEY UPDATE payload = VALUES(payload), updated_at = VALUES(updated_at)
    `,
    [collection, documentId, payload, createdAt, updatedAt]
  );

  return queryOne('SELECT * FROM documents WHERE collection_name = ? AND document_id = ?', [collection, documentId]);
}

async function insertTypedDocument(collection, body) {
  if (collection === 'users') {
    return upsertUser(body);
  }

  if (collection === 'reservas_pagadas') {
    const sql = `
      INSERT INTO reservas_pagadas (
        userUid, club, pista, dia, hora, duracion, precio, metodoPago, estado, timestamp
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    const values = [
      body.userUid,
      body.club,
      body.pista,
      body.dia,
      body.hora,
      body.duracion,
      body.precio,
      body.metodoPago,
      body.estado || 'confirmada',
      body.timestamp || now(),
    ];

    const [result] = await pool.query(sql, values);
    return queryOne('SELECT * FROM reservas_pagadas WHERE id = ?', [result.insertId]);
  }

  const table = COLLECTION_TO_TABLE[collection];

  if (!table) {
    return insertGenericDocument(collection, body);
  }

  if (collection === 'reservas_padel' || collection === 'reservas_clases') {
    const sql = `
      INSERT INTO ${table} (
        userUid, club, pista, fecha, hora, duracion, precio, estado, notas, creado, actualizado
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    const values = [
      body.userUid,
      body.club,
      body.pista,
      body.fecha,
      body.hora,
      body.duracion ?? null,
      body.precio ?? null,
      body.estado || 'pendiente',
      body.notas ?? null,
      body.creado || now(),
      body.actualizado || now(),
    ];

    const [result] = await pool.query(sql, values);
    return queryOne(`SELECT * FROM ${table} WHERE id = ?`, [result.insertId]);
  }

  if (collection === 'torneos_padel') {
    const sql = `
      INSERT INTO torneos_padel (nombre, ciudad, fecha, nivel, inscritos, creado, actualizado)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `;

    const values = [
      body.nombre,
      body.ciudad,
      body.fecha,
      body.nivel,
      toJsonValue(body.inscritos ?? []),
      body.creado || now(),
      body.actualizado || now(),
    ];

    const [result] = await pool.query(sql, values);
    return queryOne('SELECT * FROM torneos_padel WHERE id = ?', [result.insertId]);
  }

  if (collection === 'inscripciones_torneo_padel') {
    const sql = `
      INSERT INTO inscripciones_torneo_padel (userUid, torneoResumen, torneoId, creado)
      VALUES (?, ?, ?, ?)
    `;

    const values = [
      body.userUid,
      body.torneoResumen,
      body.torneoId ?? null,
      body.creado || now(),
    ];

    const [result] = await pool.query(sql, values);
    return queryOne('SELECT * FROM inscripciones_torneo_padel WHERE id = ?', [result.insertId]);
  }

  if (collection === 'seguimientos') {
    const sql = `
      INSERT INTO seguimientos (followerUid, followingUid, createdAt)
      VALUES (?, ?, ?)
      ON DUPLICATE KEY UPDATE createdAt = VALUES(createdAt)
    `;

    await pool.query(sql, [body.followerUid, body.followingUid, body.createdAt || now()]);
    return queryOne('SELECT * FROM seguimientos WHERE followerUid = ? AND followingUid = ?', [body.followerUid, body.followingUid]);
  }

  if (collection === 'partidos') {
    const sql = `
      INSERT INTO partidos (jugador1Uid, jugador2Uid, jugador2Nombre, resultado, fecha, creado)
      VALUES (?, ?, ?, ?, ?, ?)
    `;

    const values = [
      body.jugador1Uid,
      body.jugador2Uid ?? null,
      body.jugador2Nombre ?? null,
      body.resultado,
      body.fecha,
      body.creado || now(),
    ];

    const [result] = await pool.query(sql, values);
    return queryOne('SELECT * FROM partidos WHERE id = ?', [result.insertId]);
  }

  return insertGenericDocument(collection, body);
}

async function listCollection(collection) {
  if (collection === 'users') {
    const [rows] = await pool.query('SELECT * FROM users ORDER BY actualizado DESC, creado DESC');
    return rows;
  }

  const table = COLLECTION_TO_TABLE[collection];

  if (table) {
    const orderBy = table === 'reservas_pagadas'
      ? 'timestamp DESC'
      : table === 'seguimientos'
        ? 'createdAt DESC'
        : 'id DESC';

    const [rows] = await pool.query(`SELECT * FROM ${table} ORDER BY ${orderBy}`);
    return rows.map(serializeRow);
  }

  const [rows] = await pool.query('SELECT * FROM documents WHERE collection_name = ? ORDER BY updated_at DESC, created_at DESC', [collection]);
  return rows.map((row) => ({
    id: row.document_id,
    collection: row.collection_name,
    payload: serializeRow({ payload: row.payload }).payload,
    created_at: row.created_at,
    updated_at: row.updated_at,
  }));
}

async function getCollectionItem(collection, identifier) {
  if (collection === 'users') {
    return queryOne('SELECT * FROM users WHERE uid = ?', [identifier]);
  }

  const table = COLLECTION_TO_TABLE[collection];

  if (table) {
    return queryOne(`SELECT * FROM ${table} WHERE id = ?`, [identifier]);
  }

  return queryOne('SELECT * FROM documents WHERE collection_name = ? AND document_id = ?', [collection, identifier]);
}

async function updateCollectionItem(collection, identifier, body) {
  if (collection === 'users') {
    const current = await queryOne('SELECT * FROM users WHERE uid = ?', [identifier]);

    if (!current) {
      return null;
    }

    return upsertUser({
      ...current,
      ...body,
      uid: identifier,
      actualizado: now(),
    });
  }

  const table = COLLECTION_TO_TABLE[collection];

  if (table) {
    const current = await queryOne(`SELECT * FROM ${table} WHERE id = ?`, [identifier]);

    if (!current) {
      return null;
    }

    const fieldMap = {
      reservas_padel: ['userUid', 'club', 'pista', 'fecha', 'hora', 'duracion', 'precio', 'estado', 'notas'],
      reservas_clases: ['userUid', 'club', 'pista', 'fecha', 'hora', 'duracion', 'precio', 'estado', 'notas'],
      reservas_pagadas: ['userUid', 'club', 'pista', 'dia', 'hora', 'duracion', 'precio', 'metodoPago', 'estado', 'timestamp'],
      torneos_padel: ['nombre', 'ciudad', 'fecha', 'nivel', 'inscritos'],
      inscripciones_torneo_padel: ['userUid', 'torneoResumen', 'torneoId'],
      seguimientos: ['followerUid', 'followingUid', 'createdAt'],
      partidos: ['jugador1Uid', 'jugador2Uid', 'jugador2Nombre', 'resultado', 'fecha'],
    };

    const fields = (fieldMap[collection] || []).filter((field) => body[field] !== undefined);

    if (fields.length === 0) {
      return serializeRow(current);
    }

    const assignments = fields.map((field) => `${field} = ?`);
    const values = fields.map((field) => (field === 'inscritos' ? toJsonValue(body[field]) : body[field]));

    if (collection === 'reservas_padel' || collection === 'reservas_clases' || collection === 'torneos_padel') {
      assignments.push('actualizado = ?');
      values.push(now());
    }

    const sql = `UPDATE ${table} SET ${assignments.join(', ')} WHERE id = ?`;
    await pool.query(sql, [...values, identifier]);
    return queryOne(`SELECT * FROM ${table} WHERE id = ?`, [identifier]);
  }

  const current = await queryOne('SELECT * FROM documents WHERE collection_name = ? AND document_id = ?', [collection, identifier]);

  if (!current) {
    return null;
  }

  const currentPayload = typeof current.payload === 'string' ? JSON.parse(current.payload) : current.payload;
  const payload = {
    ...currentPayload,
    ...body,
  };

  await pool.query(
    'UPDATE documents SET payload = ?, updated_at = ? WHERE collection_name = ? AND document_id = ?',
    [JSON.stringify(payload), now(), collection, identifier]
  );

  return queryOne('SELECT * FROM documents WHERE collection_name = ? AND document_id = ?', [collection, identifier]);
}

async function deleteCollectionItem(collection, identifier) {
  if (collection === 'users') {
    const [result] = await pool.query('DELETE FROM users WHERE uid = ?', [identifier]);
    return result.affectedRows > 0;
  }

  const table = COLLECTION_TO_TABLE[collection];

  if (table) {
    const [result] = await pool.query(`DELETE FROM ${table} WHERE id = ?`, [identifier]);
    return result.affectedRows > 0;
  }

  const [result] = await pool.query('DELETE FROM documents WHERE collection_name = ? AND document_id = ?', [collection, identifier]);
  return result.affectedRows > 0;
}

app.get('/api/health', (_req, res) => {
  res.json({ success: true, status: 'ok' });
});

app.post('/api/auth/register', async (req, res) => {
  try {
    const user = await upsertUser({
      uid: req.body.uid || req.body.email,
      nombre: req.body.nombre,
      email: req.body.email,
      password: req.body.password ?? null,
      fotoUrl: req.body.fotoUrl ?? null,
      rol: req.body.rol || 'none',
      equipoId: req.body.equipoId ?? null,
      edad: req.body.edad ?? null,
      nivel: req.body.nivel ?? null,
      mano: req.body.mano ?? null,
      nivelPadel: req.body.nivelPadel ?? null,
      ciudadPadel: req.body.ciudadPadel ?? null,
      seguidosCount: req.body.seguidosCount ?? 0,
      seguidoresCount: req.body.seguidoresCount ?? 0,
      creado: req.body.creado,
    });

    res.status(201).json({
      success: true,
      uid: user.uid,
      user,
    });
  } catch (err) {
    console.error(err);
    res.status(err.statusCode || 500).json({ success: false, error: err.message });
  }
});

app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;

  try {
    const user = await queryOne('SELECT * FROM users WHERE email = ? LIMIT 1', [email]);

    if (!user) {
      res.status(401).json({ success: false, error: 'Credenciales invalidas' });
      return;
    }

    if (user.password === null || user.password === undefined || user.password !== password) {
      res.status(401).json({ success: false, error: 'Credenciales invalidas' });
      return;
    }

    res.json({
      success: true,
      uid: user.uid,
      user: serializeRow(user),
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.get('/api/users/:uid', async (req, res) => {
  try {
    const user = await queryOne('SELECT * FROM users WHERE uid = ?', [req.params.uid]);

    if (!user) {
      res.status(404).json({ success: false, error: 'Usuario no encontrado' });
      return;
    }

    res.json({ success: true, user: serializeRow(user) });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.get('/api/users/:uid/role', async (req, res) => {
  try {
    const user = await queryOne('SELECT uid, rol FROM users WHERE uid = ?', [req.params.uid]);

    if (!user) {
      res.status(404).json({ success: false, error: 'Usuario no encontrado' });
      return;
    }

    res.json({ success: true, uid: user.uid, rol: user.rol || 'none' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.put('/api/users/:uid', async (req, res) => {
  try {
    const user = await updateCollectionItem('users', req.params.uid, req.body);

    if (!user) {
      res.status(404).json({ success: false, error: 'Usuario no encontrado' });
      return;
    }

    res.json({ success: true, user: serializeRow(user) });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.delete('/api/users/:uid', async (req, res) => {
  try {
    const deleted = await deleteCollectionItem('users', req.params.uid);

    if (!deleted) {
      res.status(404).json({ success: false, error: 'Usuario no encontrado' });
      return;
    }

    res.json({ success: true });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.get('/api/:collection', async (req, res) => {
  const { collection } = req.params;

  try {
    const rows = await listCollection(collection);
    res.json({ success: true, collection, data: rows.map(serializeRow) });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.get('/api/:collection/:id', async (req, res) => {
  const { collection, id } = req.params;

  try {
    const item = await getCollectionItem(collection, id);

    if (!item) {
      res.status(404).json({ success: false, error: 'Registro no encontrado' });
      return;
    }

    res.json({ success: true, collection, data: serializeRow(item) });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/:collection', async (req, res) => {
  const { collection } = req.params;

  try {
    const result = await insertTypedDocument(collection, req.body);

    if (!result) {
      res.status(404).json({ success: false, error: 'Coleccion no soportada' });
      return;
    }

    res.status(201).json({ success: true, collection, data: serializeRow(result) });
  } catch (err) {
    console.error(err);
    res.status(err.statusCode || 500).json({ success: false, error: err.message });
  }
});

app.put('/api/:collection/:id', async (req, res) => {
  const { collection, id } = req.params;

  try {
    const result = await updateCollectionItem(collection, id, req.body);

    if (!result) {
      res.status(404).json({ success: false, error: 'Registro no encontrado' });
      return;
    }

    res.json({ success: true, collection, data: serializeRow(result) });
  } catch (err) {
    console.error(err);
    res.status(err.statusCode || 500).json({ success: false, error: err.message });
  }
});

app.delete('/api/:collection/:id', async (req, res) => {
  const { collection, id } = req.params;

  try {
    const deleted = await deleteCollectionItem(collection, id);

    if (!deleted) {
      res.status(404).json({ success: false, error: 'Registro no encontrado' });
      return;
    }

    res.json({ success: true, collection });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

const PORT = process.env.PORT || 3000;

initDb()
  .then(() => {
    app.listen(PORT, () => console.log('Backend listening on port', PORT));
  })
  .catch((err) => {
    console.error('Error initializing DB', err);
    process.exit(1);
  });

