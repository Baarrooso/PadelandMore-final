package com.example.padelandmore.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class AWSConnection {

    // ─── Credenciales ────────────────────────────────────────────────────────
    private static final String HOST     = "db-padelandmore.comuj8jxeeod.us-east-1.rds.amazonaws.com";
    private static final int    PORT     = 3306;
    private static final String USER     = "admin";
    private static final String PASSWORD = "padel123456";
    private static final String DATABASE = "padelandmore";

    private static final String JDBC_URL = String.format(
        "jdbc:mysql://%s:%d/%s" +
        "?useSSL=false" +
        "&allowPublicKeyRetrieval=true" +
        "&serverTimezone=UTC" +
        "&connectTimeout=10000",
        HOST, PORT, DATABASE
    );

    private static String ultimoError = "Ninguno";

    public static String getUltimoError() {
        return ultimoError;
    }

    // ─── Obtener conexión ────────────────────────────────────────────────────
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    // =========================================================================
    // ─── INICIALIZACIÓN DE TABLAS (se llama al arrancar la app) ─────────────
    // =========================================================================

    /**
     * Crea todas las tablas necesarias si no existen.
     * Llamar en un hilo secundario al iniciar la app.
     */
    public static void inicializarTablas() {
        android.util.Log.d("AWSConnection", "Intentando inicializar tablas en RDS...");
        try (Connection conn = getConnection()) {
            if (conn == null) {
                android.util.Log.e("AWSConnection", "No se pudo establecer conexión (conn es null)");
                return;
            }
            try (Statement stmt = conn.createStatement()) {
                // ── Tabla de usuarios (Aseguramos columnas de seguidores) ─────
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "  uid              VARCHAR(255)  PRIMARY KEY," +
                    "  nombre           VARCHAR(255)  NOT NULL," +
                    "  email            VARCHAR(255)  UNIQUE NOT NULL," +
                    "  password         VARCHAR(255)  NULL," +
                    "  rol              VARCHAR(50)   NOT NULL DEFAULT 'jugador'," +
                    "  creado           BIGINT        NOT NULL," +
                    "  actualizado      BIGINT        NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
                );

                // Intentar añadir cada columna por separado (Ignoramos el error si ya existe)
                try { stmt.execute("ALTER TABLE users ADD creado BIGINT NOT NULL DEFAULT 0"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD actualizado BIGINT NOT NULL DEFAULT 0"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD rol VARCHAR(50) NOT NULL DEFAULT 'jugador'"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD seguidosCount INT NOT NULL DEFAULT 0"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD seguidoresCount INT NOT NULL DEFAULT 0"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD nivelPadel DECIMAL(3,1) NULL"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD ciudadPadel VARCHAR(120) NULL"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD fotoUrl TEXT NULL"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD nivel VARCHAR(50) NULL"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD mano VARCHAR(50) NULL"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD edad INT NULL"); } catch (Exception e) {}

                // ── Tabla de reservas de pistas (UNIFICADA con soporte para partidos)
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS reservas_pistas (" +
                    "  id               INT           AUTO_INCREMENT PRIMARY KEY," +
                    "  userUid          VARCHAR(255)  NOT NULL," +
                    "  club             VARCHAR(255)  NOT NULL," +
                    "  pista            VARCHAR(255)  NOT NULL," +
                    "  fecha            VARCHAR(64)   NOT NULL," +
                    "  hora             VARCHAR(32)   NOT NULL," +
                    "  duracion         VARCHAR(32)   DEFAULT '90'," +
                    "  precio           DECIMAL(10,2) DEFAULT 0.0," +
                    "  estado           VARCHAR(64)   DEFAULT 'confirmada'," +
                    "  rivalNombre      VARCHAR(255)  NULL," +
                    "  resultado        VARCHAR(128)  NULL," +
                    "  creado           BIGINT        NOT NULL," +
                    "  actualizado      BIGINT        NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
                );
                
                // Asegurar columnas si la tabla ya existía
                try { stmt.execute("ALTER TABLE reservas_pistas ADD rivalNombre VARCHAR(255) NULL"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE reservas_pistas ADD resultado VARCHAR(128) NULL"); } catch (Exception e) {}

                // ── Tabla de reservas de clases ─────────────────────────────
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS reservas_clases (" +
                    "  id               INT           AUTO_INCREMENT PRIMARY KEY," +
                    "  userUid          VARCHAR(255)  NOT NULL," +
                    "  club             VARCHAR(255)  NOT NULL," +
                    "  pista            VARCHAR(255)  NOT NULL," +
                    "  fecha            VARCHAR(64)   NOT NULL," +
                    "  hora             VARCHAR(32)   NOT NULL," +
                    "  duracion         VARCHAR(32)   DEFAULT '60'," +
                    "  precio           DECIMAL(10,2) DEFAULT 0.0," +
                    "  estado           VARCHAR(64)   DEFAULT 'confirmada'," +
                    "  creado           BIGINT        NOT NULL," +
                    "  actualizado      BIGINT        NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
                );

                // ── Tabla de torneos (NOMBRE CORREGIDO) ─────────────────────────
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS torneos (" +
                    "  id          BIGINT       AUTO_INCREMENT PRIMARY KEY," +
                    "  nombre      VARCHAR(255) NOT NULL," +
                    "  ciudad      VARCHAR(255) NOT NULL," +
                    "  fecha       VARCHAR(64)  NOT NULL," +
                    "  nivel       VARCHAR(50)  NOT NULL," +
                    "  inscritos   JSON         NULL," +
                    "  creado      BIGINT       NOT NULL," +
                    "  actualizado BIGINT       NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
                );

                // ── Tabla de inscripciones a torneos (NOMBRE CORREGIDO) ─────────
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS inscripciones_torneo (" +
                    "  id             BIGINT       AUTO_INCREMENT PRIMARY KEY," +
                    "  userUid        VARCHAR(255) NOT NULL," +
                    "  torneoResumen  VARCHAR(500) NOT NULL," +
                    "  torneoId       VARCHAR(255) NULL," +
                    "  creado         BIGINT       NOT NULL," +
                    "  INDEX idx_inscripciones_torneo_user (userUid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
                );

                // ── Tabla de sorteos ───────────────────────────────────────────
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS sorteos (" +
                    "  id      BIGINT       AUTO_INCREMENT PRIMARY KEY," +
                    "  nombre  VARCHAR(255) NOT NULL," +
                    "  premio  VARCHAR(255) NOT NULL," +
                    "  fecha   VARCHAR(64)  NOT NULL," +
                    "  creado  BIGINT       NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
                );

                // ── Tabla de inscripciones a sorteos ───────────────────────────
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS inscripciones_sorteos (" +
                    "  id             BIGINT       AUTO_INCREMENT PRIMARY KEY," +
                    "  userUid        VARCHAR(255) NOT NULL," +
                    "  sorteoResumen  VARCHAR(500) NOT NULL," +
                    "  sorteoId       VARCHAR(255) NULL," +
                    "  creado         BIGINT       NOT NULL," +
                    "  INDEX idx_inscripciones_sorteos_user (userUid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
                );
                android.util.Log.d("AWSConnection", "Tablas verificadas/creadas con éxito.");
            }
        } catch (Throwable t) {
            android.util.Log.e("AWSConnection", "ERROR CRÍTICO en inicializarTablas: " + t.getMessage());
            t.printStackTrace();
        }
    }

    // =========================================================================
    // ─── MODELOS DE DATOS ────────────────────────────────────────────────────
    // =========================================================================

    public static class Usuario {
        public String  uid;
        public String  nombre;
        public String  email;
        public String  rol;
        public String  fotoUrl;
        public String  nivel;
        public String  mano;
        public String  ciudadPadel;
        public Integer edad;
        public Double  nivelPadel;
        public int     seguidosCount;
        public int     seguidoresCount;
        public long    creado;
        public long    actualizado;
    }

    public static class Torneo {
        public int id;
        public String nombre;
        public String ciudad;
        public String fecha;
        public String nivel;
        public String inscritos;
    }

    public static class Sorteo {
        public int id;
        public String nombre;
        public String premio;
        public String fecha;
    }

    // =========================================================================
    // ─── AUTENTICACIÓN Y ROLES ───────────────────────────────────────────────
    // =========================================================================

    /**
     * Inicio de sesión: busca email + password en la tabla users.
     */
    public static Usuario loginUsuario(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ? LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.uid             = rs.getString("uid");
                u.nombre          = rs.getString("nombre");
                u.email           = rs.getString("email");
                u.rol             = rs.getString("rol");
                u.fotoUrl         = rs.getString("fotoUrl");
                u.nivel           = rs.getString("nivel");
                u.mano            = rs.getString("mano");
                u.ciudadPadel     = rs.getString("ciudadPadel");
                u.edad            = rs.getObject("edad")       != null ? rs.getInt("edad")       : null;
                u.nivelPadel      = rs.getObject("nivelPadel") != null ? rs.getDouble("nivelPadel") : null;
                u.seguidosCount   = rs.getInt("seguidosCount");
                u.seguidoresCount = rs.getInt("seguidoresCount");
                u.creado          = rs.getLong("creado");
                u.actualizado     = rs.getLong("actualizado");
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene el rol de un usuario por su UID.
     */
    public static String obtenerRolUsuario(String uid) {
        String sql = "SELECT rol FROM users WHERE uid = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("rol");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "jugador";
    }

    /**
     * Obtiene el perfil completo de un usuario por su UID.
     */
    public static Usuario obtenerUsuarioCompleto(String uid) {
        String sql = "SELECT * FROM users WHERE uid = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.uid             = rs.getString("uid");
                u.nombre          = rs.getString("nombre");
                u.email           = rs.getString("email");
                u.rol             = rs.getString("rol");
                u.fotoUrl         = rs.getString("fotoUrl");
                u.nivel           = rs.getString("nivel");
                u.mano            = rs.getString("mano");
                u.ciudadPadel     = rs.getString("ciudadPadel");
                u.edad            = rs.getObject("edad")       != null ? rs.getInt("edad")       : null;
                u.nivelPadel      = rs.getObject("nivelPadel") != null ? rs.getDouble("nivelPadel") : null;
                u.seguidosCount   = rs.getInt("seguidosCount");
                u.seguidoresCount = rs.getInt("seguidoresCount");
                u.creado          = rs.getLong("creado");
                u.actualizado     = rs.getLong("actualizado");
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registro de nuevo usuario.
     */
    public static String registrarUsuario(String nombre, String email, String password) {
        String uid = UUID.randomUUID().toString();
        long   now = System.currentTimeMillis();
        
        // 1. Comprobar si el email ya existe
        String checkSql = "SELECT uid FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = getConnection()) {
            if (conn == null) {
                ultimoError = "No se pudo establecer la conexión";
                return null;
            }
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                if (checkStmt.executeQuery().next()) {
                    ultimoError = "El email ya está registrado";
                    return null; 
                }
            }

            // 2. Insertar el nuevo usuario (MÁXIMA SIMPLIFICACIÓN)
            // Solo enviamos lo que el usuario escribe: nombre, email y password + el ID único
            String sql = "INSERT INTO users (uid, nombre, email, password) VALUES (?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, uid);
                pstmt.setString(2, nombre);
                pstmt.setString(3, email);
                pstmt.setString(4, password);
                
                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    return uid;
                }
            }
        } catch (SQLException e) {
            ultimoError = "SQL: " + e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            ultimoError = "General: " + e.getMessage();
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================================
    // ─── TORNEOS ─────────────────────────────────────────────────────────────
    // =========================================================================

    public static java.util.List<Torneo> obtenerTorneos() {
        java.util.List<Torneo> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM torneos ORDER BY creado DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Torneo t = new Torneo();
                t.id = rs.getInt("id");
                t.nombre = rs.getString("nombre");
                t.ciudad = rs.getString("ciudad");
                t.fecha = rs.getString("fecha");
                t.nivel = rs.getString("nivel");
                t.inscritos = rs.getString("inscritos");
                lista.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public static boolean insertarTorneo(String nombre, String ciudad, String fecha, String nivel, long creado, long actualizado) {
        String sql = "INSERT INTO torneos (nombre, ciudad, fecha, nivel, inscritos, creado, actualizado) VALUES (?, ?, ?, ?, '[]', ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, ciudad);
            pstmt.setString(3, fecha);
            pstmt.setString(4, nivel);
            pstmt.setLong(5, creado);
            pstmt.setLong(6, actualizado);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean inscribirEnTorneo(String userUid, String torneoResumen, long creado) {
        String sql = "INSERT INTO inscripciones_torneo (userUid, torneoResumen, creado) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userUid);
            pstmt.setString(2, torneoResumen);
            pstmt.setLong(3, creado);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // =========================================================================
    // ─── SORTEOS ─────────────────────────────────────────────────────────────
    // =========================================================================

    public static java.util.List<Sorteo> obtenerSorteos() {
        java.util.List<Sorteo> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM sorteos ORDER BY creado DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Sorteo s = new Sorteo();
                s.id = rs.getInt("id");
                s.nombre = rs.getString("nombre");
                s.premio = rs.getString("premio");
                s.fecha = rs.getString("fecha");
                lista.add(s);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public static boolean insertarSorteo(String nombre, String premio, String fecha, long creado) {
        String sql = "INSERT INTO sorteos (nombre, premio, fecha, creado) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, premio);
            pstmt.setString(3, fecha);
            pstmt.setLong(4, creado);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean inscribirEnSorteo(String userUid, String sorteoResumen, long creado) {
        String sql = "INSERT INTO inscripciones_sorteos (userUid, sorteoResumen, creado) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userUid);
            pstmt.setString(2, sorteoResumen);
            pstmt.setLong(3, creado);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Obtiene las reservas existentes para un club, pista y fecha específicos.
     */
    public static java.util.List<String> obtenerHorasReservadas(String tabla, String club, String pista, String fecha) {
        java.util.List<String> horas = new java.util.ArrayList<>();
        String sql = String.format("SELECT hora FROM %s WHERE club = ? AND pista = ? AND fecha = ? AND estado != 'cancelada'", tabla);
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, club);
            pstmt.setString(2, pista);
            pstmt.setString(3, fecha);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    horas.add(rs.getString("hora"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return horas;
    }

    public static boolean insertarReserva(String tabla, String userUid, String club, String pista, String fecha, String hora) {
        long now = System.currentTimeMillis();
        // Usamos reservas_pistas o reservas_clases según el parámetro tabla
        String sql = String.format("INSERT INTO %s (userUid, club, pista, fecha, hora, duracion, estado, creado, actualizado) VALUES (?, ?, ?, ?, ?, '90', 'confirmada', ?, ?)", tabla);
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userUid);
            pstmt.setString(2, club);
            pstmt.setString(3, pista);
            pstmt.setString(4, fecha);
            pstmt.setString(5, hora);
            pstmt.setLong(6, now);
            pstmt.setLong(7, now);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean insertarReservaPagada(String userUid, String club, String pista, String dia, String hora, String duracion, double precio, String metodoPago) {
        String sql = "INSERT INTO reservas_pagadas (userUid, club, pista, dia, hora, duracion, precio, metodoPago, estado, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'confirmada', ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userUid);
            pstmt.setString(2, club);
            pstmt.setString(3, pista);
            pstmt.setString(4, dia);
            pstmt.setString(5, hora);
            pstmt.setString(6, duracion);
            pstmt.setDouble(7, precio);
            pstmt.setString(8, metodoPago);
            pstmt.setLong(9, System.currentTimeMillis());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static java.util.List<java.util.Map<String, String>> obtenerReservasUsuario(String uid) {
        java.util.List<java.util.Map<String, String>> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM reservas_pistas WHERE userUid = ? ORDER BY creado DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uid);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, String> reserva = new java.util.HashMap<>();
                    reserva.put("id", rs.getString("id"));
                    reserva.put("club", rs.getString("club"));
                    reserva.put("pista", rs.getString("pista"));
                    reserva.put("dia", rs.getString("fecha"));
                    reserva.put("hora", rs.getString("hora"));
                    reserva.put("estado", rs.getString("estado"));
                    reserva.put("rival", rs.getString("rivalNombre"));
                    reserva.put("resultado", rs.getString("resultado"));
                    lista.add(reserva);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public static boolean actualizarResultadoReserva(String id, String resultado) {
        String sql = "UPDATE reservas_pistas SET resultado = ?, actualizado = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resultado);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean actualizarRivalReserva(String id, String rivalNombre) {
        String sql = "UPDATE reservas_pistas SET rivalNombre = ?, actualizado = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rivalNombre);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
