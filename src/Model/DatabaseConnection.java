package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Datos de conexión
    private static final String URL = "jdbc:postgresql://localhost:5432/luas_place";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin"; // O tu contraseña real

    /**
     * Obtiene una NUEVA conexión a la base de datos.
     * Es responsabilidad del que llama cerrar esta conexión (usando try-with-resources).
     */
    public static Connection getConnection() {
        try {
            // Cargar driver explícitamente para evitar problemas
            Class.forName("org.postgresql.Driver");

            // SIEMPRE devuelve una nueva conexión
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR CRÍTICO: Driver de PostgreSQL no encontrado.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("❌ ERROR CONEXIÓN: No se pudo conectar a la BD.");
            e.printStackTrace();
            return null;
        }
    }
}