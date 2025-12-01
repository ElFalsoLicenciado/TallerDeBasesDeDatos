package Model.DAO;

import Model.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecetaDAO {

    /**
     * Obtiene el texto formateado de una receta dado el código del producto.
     */
    public String obtenerRecetaPorProducto(String codigoProducto) {
        StringBuilder sb = new StringBuilder();

        // 1. Obtener datos generales de la receta
        String sqlReceta = "SELECT r.nombre, r.instrucciones, r.tiempo_preparacion " +
                "FROM recetas r " +
                "JOIN productos p ON p.id_receta = r.id " +
                "WHERE p.codigo = ?";

        // 2. Obtener ingredientes
        String sqlIngredientes = "SELECT i.nombre, ri.cantidad_requerida, i.unidad_medida " +
                "FROM recetas_ingredientes ri " +
                "JOIN ingredientes i ON ri.id_ingrediente = i.id " +
                "JOIN recetas r ON ri.id_receta = r.id " +
                "JOIN productos p ON p.id_receta = r.id " +
                "WHERE p.codigo = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Query 1: Info General
            try (PreparedStatement ps = conn.prepareStatement(sqlReceta)) {
                ps.setString(1, codigoProducto);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    sb.append("RECETA: ").append(rs.getString("nombre")).append("\n");
                    sb.append("Tiempo: ").append(rs.getInt("tiempo_preparacion")).append(" mins\n\n");
                    sb.append("INSTRUCCIONES:\n").append(rs.getString("instrucciones")).append("\n\n");
                } else {
                    return "Este producto no tiene una receta vinculada.";
                }
            }

            // Query 2: Ingredientes
            sb.append("INGREDIENTES:\n");
            try (PreparedStatement ps = conn.prepareStatement(sqlIngredientes)) {
                ps.setString(1, codigoProducto);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    sb.append("• ").append(rs.getString("nombre"))
                            .append(": ").append(rs.getDouble("cantidad_requerida"))
                            .append(" ").append(rs.getString("unidad_medida")).append("\n");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error al cargar la receta.";
        }

        return sb.toString();
    }
}