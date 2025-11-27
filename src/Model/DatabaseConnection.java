package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Datos de tu script SQL
                String url = "jdbc:postgresql://localhost:5432/luas_place";
                String user = "postgres";
                String password = "password";

                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Conexión exitosa a Lua's Place");

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error conectando a la base de datos");
            }
        }
        return connection;
    }
}