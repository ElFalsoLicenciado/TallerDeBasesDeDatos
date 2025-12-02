package Model.DAO;

import Model.DatabaseConnection;
import Model.Entities.InventarioItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {

    public List<InventarioItem> obtenerInventarioPorSucursal(int idSucursal) {
        List<InventarioItem> lista = new ArrayList<>();
        // Consultamos la VISTA que ya tiene la lógica de estados
        String sql = "SELECT codigo_producto, producto, tipo, precio_venta, " +
                "cantidad_disponible, cantidad_minima, estado_stock " +
                "FROM vista_inventario_sucursales " +
                "WHERE id_sucursal = ? " +
                "ORDER BY cantidad_disponible ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idSucursal);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.add(new InventarioItem(
                        rs.getString("codigo_producto"),
                        rs.getString("producto"),
                        rs.getString("tipo"),
                        rs.getDouble("precio_venta"),
                        rs.getInt("cantidad_disponible"),
                        rs.getInt("cantidad_minima"),
                        rs.getString("estado_stock")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene la lista de ingredientes globales para mostrar en la tabla unificada.
     */
    public List<InventarioItem> obtenerIngredientesGlobales() {
        List<InventarioItem> lista = new ArrayList<>();
        String sql = "SELECT codigo, nombre, 'Ingrediente' as tipo, costo_unitario, cantidad_disponible, cantidad_minima " +
                "FROM ingredientes WHERE activo = TRUE ORDER BY cantidad_disponible ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                double stock = rs.getDouble("cantidad_disponible");
                double min = rs.getDouble("cantidad_minima");

                // Calcular estado manualmente para ingredientes
                String estado;
                if (stock <= 0) estado = "AGOTADO";
                else if (stock <= min) estado = "CRÍTICO";
                else if (stock <= min * 2) estado = "BAJO";
                else estado = "ADECUADO";

                lista.add(new InventarioItem(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        "Materia Prima", // Tipo
                        rs.getDouble("costo_unitario"), // Usamos costo como precio visual
                        (int) stock, // Casteo a int para visualización simple
                        (int) min,
                        estado
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}