package Model.DAO;

import Model.DatabaseConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import Model.Entities.RecetaDetalle; // Asumiendo que crearás esta entidad
import Model.Entities.Ingrediente;

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

    public boolean guardarReceta(String nombre, String instrucciones, int tiempo, double cantidadProd, String unidad, int idUsuario, Map<Ingrediente, Double> ingredientes) {
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement psReceta = null;
        PreparedStatement psIngredientes = null;

        // Generamos código automático con el trigger, no lo enviamos
        String sqlReceta = "INSERT INTO recetas (nombre, instrucciones, tiempo_preparacion, cantidad_producida, unidad_produccion, id_usuario_creador) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        String sqlDetalle = "INSERT INTO recetas_ingredientes (id_receta, id_ingrediente, cantidad_requerida, orden_uso) VALUES (?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar Receta
            psReceta = conn.prepareStatement(sqlReceta);
            psReceta.setString(1, nombre);
            psReceta.setString(2, instrucciones);
            psReceta.setInt(3, tiempo);
            psReceta.setDouble(4, cantidadProd);
            psReceta.setString(5, unidad);
            psReceta.setInt(6, idUsuario);

            ResultSet rs = psReceta.executeQuery();
            int idReceta = 0;
            if (rs.next()) idReceta = rs.getInt(1);

            // 2. Insertar Ingredientes
            psIngredientes = conn.prepareStatement(sqlDetalle);
            int orden = 1;

            for (Map.Entry<Ingrediente, Double> entry : ingredientes.entrySet()) {
                psIngredientes.setInt(1, idReceta);
                psIngredientes.setInt(2, entry.getKey().getId());
                psIngredientes.setDouble(3, entry.getValue());
                psIngredientes.setInt(4, orden++);
                psIngredientes.addBatch();
            }

            psIngredientes.executeBatch();
            conn.commit(); // Confirmar
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
                if (psReceta != null) psReceta.close();
                if (psIngredientes != null) psIngredientes.close();
                if (conn != null) conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Helper para llenar el ComboBox de creación de productos
    public Map<Integer, String> obtenerListaSimple() {
        Map<Integer, String> mapa = new HashMap<>();
        String sql = "SELECT id, nombre FROM recetas WHERE activo = TRUE ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                mapa.put(rs.getInt("id"), rs.getString("nombre"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return mapa;
    }
}