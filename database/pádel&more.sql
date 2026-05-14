CREATE DATABASE IF NOT EXISTS padel&more
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE padel&more;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS torneos_padel (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  ciudad VARCHAR(255) NOT NULL,
  fecha VARCHAR(64) NOT NULL,
  nivel VARCHAR(50) NOT NULL,
  inscritos JSON NULL,
  creado BIGINT NOT NULL,
  actualizado BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inscripciones_torneo_padel (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  userUid VARCHAR(255) NOT NULL,
  torneoResumen VARCHAR(500) NOT NULL,
  torneoId VARCHAR(255) NULL,
  creado BIGINT NOT NULL,
  INDEX idx_inscripciones_torneo_user (userUid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS seguimientos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  followerUid VARCHAR(255) NOT NULL,
  followingUid VARCHAR(255) NOT NULL,
  createdAt BIGINT NOT NULL,
  UNIQUE KEY uq_seguimientos (followerUid, followingUid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS partidos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  jugador1Uid VARCHAR(255) NOT NULL,
  jugador2Uid VARCHAR(255) NULL,
  jugador2Nombre VARCHAR(255) NULL,
  resultado VARCHAR(64) NOT NULL,
  fecha VARCHAR(64) NOT NULL,
  creado BIGINT NOT NULL,
  INDEX idx_partidos_jugador1 (jugador1Uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS documents (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  collection_name VARCHAR(100) NOT NULL,
  document_id VARCHAR(255) NOT NULL,
  payload JSON NOT NULL,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  UNIQUE KEY uq_documents_collection_id (collection_name, document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;