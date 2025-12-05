package org.example.Model.DAO;

import org.example.Model.DatabaseConnection;
import org.example.Model.Entities.Ingrediente;
import java.sql.*;
import java.util.Map;

public class CompraDAO {

    public boolean registrarCompra(int idUsuario, int idProveedor, double total, Map<Ingrediente, Double> carrito) {
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement psCompra = null;
        PreparedStatement psDetalle = null;
        PreparedStatement psStock = null;

        try {
            conn.setAutoCommit(false); // Iniciar Transacción

            // 1. Insertar Compra
            String sqlCompra = "INSERT INTO compras (id_proveedor, subtotal, impuestos, total, id_usuario_registro, estado) VALUES (?, ?, 0, ?, ?, 'Completada') RETURNING id";
            psCompra = conn.prepareStatement(sqlCompra);
            psCompra.setInt(1, idProveedor);
            psCompra.setDouble(2, total);
            psCompra.setDouble(3, total);
            psCompra.setInt(4, idUsuario);

            ResultSet rs = psCompra.executeQuery();
            int idCompra = 0;
            if (rs.next()) idCompra = rs.getInt(1);

            // 2. Preparar consultas de detalle y actualización de stock
            String sqlDetalle = "INSERT INTO detalles_compras (id_compra, id_ingrediente, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            String sqlUpdateStock = "UPDATE ingredientes SET cantidad_disponible = cantidad_disponible + ? WHERE id = ?";

            psDetalle = conn.prepareStatement(sqlDetalle);
            psStock = conn.prepareStatement(sqlUpdateStock);

            for (Map.Entry<Ingrediente, Double> entry : carrito.entrySet()) {
                Ingrediente ing = entry.getKey();
                Double cantidad = entry.getValue();
                double subtotal = ing.getCosto() * cantidad;

                // Detalle
                psDetalle.setInt(1, idCompra);
                psDetalle.setInt(2, ing.getId());
                psDetalle.setDouble(3, cantidad);
                psDetalle.setDouble(4, ing.getCosto());
                psDetalle.setDouble(5, subtotal);
                psDetalle.addBatch();

                // Actualizar Stock (+ cantidad)
                psStock.setDouble(1, cantidad);
                psStock.setInt(2, ing.getId());
                psStock.addBatch();
            }

            psDetalle.executeBatch();
            psStock.executeBatch();

            conn.commit(); // Confirmar cambios
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
                if (psCompra != null) psCompra.close();
                if (psDetalle != null) psDetalle.close();
                if (psStock != null) psStock.close();
                if (conn != null) conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}