package Model.DAO;

import Model.DatabaseConnection;
import java.sql.*;

public class ServerLogDAO {

    public String obtenerLogDelServidor() {
        StringBuilder logContent = new StringBuilder();

        // Esta consulta mágica pide el contenido del archivo de log actual
        // Solo funciona si el usuario conectado es Superuser (tu usuario 'postgres' lo es)
        String sql = "SELECT convert_from(pg_read_binary_file(pg_current_logfile()), 'WIN1252')";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                // El resultado es un solo String gigante con todo el texto del archivo
                String contenido = rs.getString(1);
                if (contenido != null) {
                    logContent.append(contenido);
                } else {
                    logContent.append("El archivo de log está vacío o no se pudo leer.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error al leer el log del servidor:\n" + e.getMessage() +
                    "\n\nAsegúrate de que 'logging_collector = on' en postgresql.conf";
        }
        return logContent.toString();
    }
}