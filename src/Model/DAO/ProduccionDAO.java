package Model.DAO;

import Model.DatabaseConnection;
import Model.Entities.Producto;
import Model.Entities.RecetaDetalle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduccionDAO {

    /**
     * Obtiene productos que tienen una receta definida.
     */
    public List<Producto> listarProductosConReceta() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.codigo, p.nombre, p.precio " +
                "FROM productos p " +
                "JOIN recetas r ON p.id_receta = r.id " +
                "WHERE p.activo = TRUE AND r.activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Usamos 0 en existencia porque aquí no es relevante para la selección
                lista.add(new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        0
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * Obtiene los ingredientes necesarios y el stock actual.
     * ESTE ES EL MÉTODO QUE LLENA LA TABLA DE PRODUCCIÓN.
     */
    public List<RecetaDetalle> obtenerDetallesRecetaGlobal(int idProducto) {
        List<RecetaDetalle> detalles = new ArrayList<>();

        // Consulta corregida para tu estructura de BD
        String sql = "SELECT i.nombre, ri.cantidad_requerida, i.unidad_medida, i.cantidad_disponible " +
                "FROM productos p " +
                "JOIN recetas_ingredientes ri ON p.id_receta = ri.id_receta " +
                "JOIN ingredientes i ON ri.id_ingrediente = i.id " +
                "WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                detalles.add(new RecetaDetalle(
                        rs.getString("nombre"),
                        rs.getDouble("cantidad_requerida"),
                        rs.getDouble("cantidad_disponible"),
                        rs.getString("unidad_medida")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detalles;
    }

    /**
     * Registra la producción: inserta el lote.
     * (El trigger de la BD se encarga de restar ingredientes y sumar producto).
     */
    public boolean registrarProduccion(int idProducto, double cantidad, int idUsuario, int idSucursal) {
        // Obtenemos el id_receta primero
        String sqlGetReceta = "SELECT id_receta FROM productos WHERE id = ?";
        String sqlInsert = "INSERT INTO lotes_produccion (id_producto, id_receta, cantidad_producida, " +
                "id_empleado_productor, id_usuario_registro, id_sucursal, estado) " +
                "VALUES (?, ?, ?, (SELECT id_empleado FROM usuarios WHERE id = ?), ?, ?, 'Completado')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int idReceta = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlGetReceta)) {
                ps.setInt(1, idProducto);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) idReceta = rs.getInt(1);
            }

            if (idReceta == 0) return false;

            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setInt(1, idProducto);
                ps.setInt(2, idReceta);
                ps.setDouble(3, cantidad);
                ps.setInt(4, idUsuario);
                ps.setInt(5, idUsuario);
                ps.setInt(6, idSucursal);

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}