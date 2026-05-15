package com.example.padelandmore.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AWSConnection {

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

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    // ─── Modelo de datos del usuario ─────────────────────────────────────────
    public static class Usuario {
        public String uid;
        public String nombre;
        public String email;
        public String rol;
        public String fotoUrl;
        public String nivel;
        public String mano;
        public String ciudadPadel;
        public Integer edad;
        public Double nivelPadel;
        public int seguidosCount;
        public int seguidoresCount;
        public long creado;
        public long actualizado;
    }

    // ─── Login directo a BBDD: devuelve Usuario completo o null ──────────────
    public static Usuario loginUsuario(String email, String password) {
        String sql = "SELECT uid, nombre, email, rol, fotoUrl, nivel, mano, ciudadPadel, " +
                     "edad, nivelPadel, seguidosCount, seguidoresCount, creado, actualizado " +
                     "FROM users WHERE email = ? AND password = ? LIMIT 1";

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
                u.edad            = rs.getObject("edad") != null ? rs.getInt("edad") : null;
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

    // ─── Insertar torneo directamente en BBDD ────────────────────────────────
    public static boolean insertarTorneo(String nombre, String ciudad, String fecha, String nivel, long creado, long actualizado) {
        String sql = "INSERT INTO torneos_padel (nombre, ciudad, fecha, nivel, inscritos, creado, actualizado) VALUES (?, ?, ?, ?, '[]', ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, ciudad);
            pstmt.setString(3, fecha);
            pstmt.setString(4, nivel);
            pstmt.setLong(5, creado);
            pstmt.setLong(6, actualizado);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
