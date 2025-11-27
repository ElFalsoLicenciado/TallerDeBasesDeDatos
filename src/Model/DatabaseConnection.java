package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.postgresql.Driver");
                String url = "jdbc:postgresql://localhost:5432/luas_place";
                String user = "postgres"; // Asegúrate que este sea tu usuario real
                String password = "password"; // <--- PON TU CONTRASEÑA AQUÍ

                connection = DriverManager.getConnection(url, user, password);
                System.out.println("✅ Conexión exitosa a la base de datos.");

            } catch (ClassNotFoundException e) {
                // Esto pasa si no agregaste el JAR a las librerías de IntelliJ
                System.err.println("ERROR: No se encontró el Driver de PostgreSQL. Verifica las librerías.");
                e.printStackTrace();
            } catch (SQLException e) {
                // Esto pasa si la URL, usuario o contraseña están mal
                System.err.println("ERROR: Falló la conexión SQL.");
                e.printStackTrace();
            }
        }
        return connection;
    }
}