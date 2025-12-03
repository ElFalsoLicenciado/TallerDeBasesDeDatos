package Model.DAO;

import Model.DatabaseConnection;
import Model.Entities.Ingrediente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredienteDAO {
    public List<Ingrediente> listarTodos() {
        List<Ingrediente> lista = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, costo_unitario, unidad_medida, cantidad_disponible FROM ingredientes WHERE activo = TRUE ORDER BY nombre";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Ingrediente(
                        rs.getInt("id"), rs.getString("codigo"), rs.getString("nombre"),
                        rs.getDouble("costo_unitario"), rs.getString("unidad_medida"), rs.getDouble("cantidad_disponible")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * Obtiene solo los ingredientes que vende un proveedor específico.
     * El costo devuelto es el precio pactado con ese proveedor.
     */
    public List<Ingrediente> listarPorProveedor(int idProveedor) {
        List<Ingrediente> lista = new ArrayList<>();

        // Hacemos JOIN para filtrar y para obtener el precio específico de compra
        String sql = "SELECT i.id, i.codigo, i.nombre, i.unidad_medida, i.cantidad_disponible, pi.precio_unitario " +
                "FROM ingredientes i " +
                "JOIN proveedores_ingredientes pi ON i.id = pi.id_ingrediente " +
                "WHERE pi.id_proveedor = ? AND i.activo = TRUE " +
                "ORDER BY i.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProveedor);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.add(new Ingrediente(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio_unitario"), // <--- Usamos el precio del proveedor
                        rs.getString("unidad_medida"),
                        rs.getDouble("cantidad_disponible")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}