import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * AWSConnection.java
 * Conexión JDBC a AWS RDS MySQL (sin SSL).
 *
 * Compilar:  javac -cp mysql-connector-java.jar AWSConnection.java
 * Ejecutar:  java  -cp ".;mysql-connector-java.jar" AWSConnection
 */
public class AWSConnection {

    // ─── Credenciales ────────────────────────────────────────────────────────
    private static final String HOST     = "db-padelandmore.comuj8jxeeod.us-east-1.rds.amazonaws.com";
    private static final int    PORT     = 3306;
    private static final String USER     = "admin";
    private static final String PASSWORD = "padel123456";
    private static final String DATABASE = "padelandmore";

    // ─── URL de conexión SIN SSL ─────────────────────────────────────────────
    private static final String JDBC_URL = String.format(
        "jdbc:mysql://%s:%d/%s" +
        "?useSSL=false" +
        "&allowPublicKeyRetrieval=true" +
        "&serverTimezone=UTC" +
        "&connectTimeout=10000",
        HOST, PORT, DATABASE
    );

    // ─── Obtener conexión ────────────────────────────────────────────────────
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    // ─── Controlador: Verificar conexión ────────────────────────────────────
    public static void testConnection() {
        System.out.println("🔌 Verificando conexión a AWS RDS...");
        System.out.println("   Host    : " + HOST);
        System.out.println("   Puerto  : " + PORT);
        System.out.println("   Base    : " + DATABASE);
        System.out.println("   Usuario : " + USER);
        System.out.println("   SSL     : Desactivado");
        System.out.println();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 + 1 AS resultado, NOW() AS hora_servidor")) {

            if (rs.next()) {
                System.out.println("✅ Conexión exitosa.");
                System.out.println("   Resultado prueba : " + rs.getInt("resultado"));
                System.out.println("   Hora del servidor: " + rs.getString("hora_servidor"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al conectar: " + e.getMessage());
            System.err.println("   SQLState : " + e.getSQLState());
            System.err.println("   Código   : " + e.getErrorCode());
        }
    }

    // ─── Controlador: Listar tablas ──────────────────────────────────────────
    public static void listTables() {
        System.out.println("\n📋 Listando tablas de la base de datos '" + DATABASE + "'...");

        String sql = "SELECT table_name, table_rows " +
                     "FROM information_schema.tables " +
                     "WHERE table_schema = ? " +
                     "ORDER BY table_name";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, DATABASE);
            ResultSet rs = pstmt.executeQuery();

            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                String tableName = rs.getString("table_name");
                long   tableRows = rs.getLong("table_rows");
                tables.add(tableName);
                System.out.printf("   ├─ %-30s (~%d filas)%n", tableName, tableRows);
            }

            if (tables.isEmpty()) {
                System.out.println("   ⚠️  No se encontraron tablas en la base de datos.");
            } else {
                System.out.println("\n✅ Total tablas encontradas: " + tables.size());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al listar tablas: " + e.getMessage());
        }
    }

    // ─── Controlador: Listar bases de datos ─────────────────────────────────
    public static void listDatabases() {
        System.out.println("\n📂 Listando bases de datos disponibles...");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {

            List<String> dbs = new ArrayList<>();
            while (rs.next()) {
                dbs.add(rs.getString(1));
                System.out.println("   ├─ " + rs.getString(1));
            }
            System.out.println("\n✅ Total bases de datos: " + dbs.size());

        } catch (SQLException e) {
            System.err.println("❌ Error al listar bases de datos: " + e.getMessage());
        }
    }

    // ─── Controlador: Crear tabla usuarios ──────────────────────────────────
    public static void createUsuariosTable() {
        System.out.println("\n🛠️ Creando tabla 'usuarios' si no existe...");
        
        String sql = "CREATE TABLE IF NOT EXISTS usuarios (" +
                     "  uid VARCHAR(255) PRIMARY KEY," +
                     "  nombre VARCHAR(255) NOT NULL," +
                     "  email VARCHAR(255) UNIQUE NOT NULL," +
                     "  rol ENUM('jugador', 'entrenador', 'admin', 'none') DEFAULT 'none'," +
                     "  equipoId VARCHAR(255)," +
                     "  fotoUrl VARCHAR(255)," +
                     "  edad INT," +
                     "  nivel VARCHAR(50)," +
                     "  mano VARCHAR(50)," +
                     "  creado TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                     "  actualizado TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                     ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            System.out.println("✅ Tabla 'usuarios' lista.");
            
        } catch (SQLException e) {
            System.err.println("❌ Error al crear tabla: " + e.getMessage());
        }
    }

    // ─── Controlador: Registrar un usuario (INSERT) ─────────────────────────
    public static boolean registerUser(String uid, String nombre, String email, String rol) {
        System.out.println("\n📝 Registrando usuario: " + nombre + " (" + email + ")...");
        
        String sql = "INSERT INTO usuarios (uid, nombre, email, rol) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), rol = VALUES(rol)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, uid);
            pstmt.setString(2, nombre);
            pstmt.setString(3, email);
            pstmt.setString(4, rol);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Usuario registrado/actualizado correctamente.");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al registrar usuario: " + e.getMessage());
        }
        return false;
    }

    // ─── Controlador: Crear tablas de reservas ──────────────────────────────
    public static void createReservasTables() {
        System.out.println("\n🛠️ Creando tablas de reservas si no existen...");
        
        String sqlPadel = "CREATE TABLE IF NOT EXISTS reservas_padel (" +
                     "  id INT AUTO_INCREMENT PRIMARY KEY," +
                     "  userUid VARCHAR(255) NOT NULL," +
                     "  club VARCHAR(255) NOT NULL," +
                     "  pista VARCHAR(255) NOT NULL," +
                     "  fecha VARCHAR(64) NOT NULL," +
                     "  hora VARCHAR(32) NOT NULL," +
                     "  creado TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ")";

        String sqlClases = "CREATE TABLE IF NOT EXISTS reservas_clases (" +
                     "  id INT AUTO_INCREMENT PRIMARY KEY," +
                     "  userUid VARCHAR(255) NOT NULL," +
                     "  club VARCHAR(255) NOT NULL," +
                     "  pista VARCHAR(255) NOT NULL," +
                     "  fecha VARCHAR(64) NOT NULL," +
                     "  hora VARCHAR(32) NOT NULL," +
                     "  creado TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sqlPadel);
            System.out.println("✅ Tabla 'reservas_padel' lista.");
            stmt.execute(sqlClases);
            System.out.println("✅ Tabla 'reservas_clases' lista.");
            
        } catch (SQLException e) {
            System.err.println("❌ Error al crear tablas de reservas: " + e.getMessage());
        }
    }

    // ─── Controlador: Registrar una reserva (INSERT) ────────────────────────
    public static boolean insertReserva(String tipo, String userUid, String club, String pista, String fecha, String hora) {
        String tabla = "clase".equals(tipo) ? "reservas_clases" : "reservas_padel";
        System.out.println("\n📝 Registrando reserva en " + tabla + " para usuario: " + userUid + "...");
        
        String sql = "INSERT INTO " + tabla + " (userUid, club, pista, fecha, hora) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userUid);
            pstmt.setString(2, club);
            pstmt.setString(3, pista);
            pstmt.setString(4, fecha);
            pstmt.setString(5, hora);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Reserva guardada correctamente.");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar reserva: " + e.getMessage());
        }
        return false;
    }

    // ─── Main: Ejecutar todas las pruebas ───────────────────────────────────
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   TEST DE CONEXIÓN Y REGISTRO AWS RDS - JAVA");
        System.out.println("═══════════════════════════════════════════════\n");

        testConnection();
        listDatabases();
        
        // 1. Asegurar que la tabla existe
        createUsuariosTable();
        createReservasTables();
        
        // 2. Probar un registro
        registerUser("test_uid_123", "Usuario de Prueba", "test@padelmore.com", "jugador");
        
        // 3. Probar una reserva
        insertReserva("padel", "test_uid_123", "Club Padel", "Pista 1", "2024-05-20", "18:00");

        // 4. Listar resultados
        listTables();

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("   FIN DEL TEST");
        System.out.println("═══════════════════════════════════════════════");
    }
}
