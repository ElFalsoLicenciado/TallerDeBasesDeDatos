package Model.DAO;

import Model.DatabaseConnection;
import Model.Entities.Producto;
import java.sql.*;
import java.util.Map;

public class VentaDAO {

    public boolean registrarVenta(int idUser, int idSucursal, double total, Map<Producto, Integer> carrito, Integer idCliente) {
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement psVenta = null;
        PreparedStatement psDetalle = null;

        try {
            conn.setAutoCommit(false);

            // Se añadió la columna id_cliente
            String sqlVenta = "INSERT INTO ventas (id_sucursal, id_empleado_vendedor, id_usuario_registro, subtotal, impuestos, total, metodo_pago, id_cliente) " +
                    "VALUES (?, (SELECT id_empleado FROM usuarios WHERE id = ?), ?, ?, 0, ?, 'Efectivo', ?) RETURNING id";

            psVenta = conn.prepareStatement(sqlVenta);
            psVenta.setInt(1, idSucursal);
            psVenta.setInt(2, idUser);
            psVenta.setInt(3, idUser);
            psVenta.setDouble(4, total);
            psVenta.setDouble(5, total);

            // Manejo de cliente nulo
            if (idCliente == null) {
                psVenta.setNull(6, java.sql.Types.INTEGER);
            } else {
                psVenta.setInt(6, idCliente);
            }

            ResultSet rs = psVenta.executeQuery();
            int idVenta = 0;
            if (rs.next()) {
                idVenta = rs.getInt(1);
            }

            String sqlDetalle = "INSERT INTO detalles_ventas (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            psDetalle = conn.prepareStatement(sqlDetalle);

            for (Map.Entry<Producto, Integer> item : carrito.entrySet()) {
                Producto p = item.getKey();
                int cantidad = item.getValue();
                double subtotal = p.getPrecio() * cantidad;

                psDetalle.setInt(1, idVenta);
                psDetalle.setInt(2, p.getId());
                psDetalle.setInt(3, cantidad);
                psDetalle.setDouble(4, p.getPrecio());
                psDetalle.setDouble(5, subtotal);
                psDetalle.addBatch();
            }

            psDetalle.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
                if (psVenta != null) psVenta.close();
                if (psDetalle != null) psDetalle.close();
                if (conn != null) conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}