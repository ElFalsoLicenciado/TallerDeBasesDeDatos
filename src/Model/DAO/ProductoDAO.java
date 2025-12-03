package Model.DAO;

import Model.DatabaseConnection;
import Model.Entities.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> buscarPorNombreOCodigo(String busqueda, int idSucursal) {
        List<Producto> lista = new ArrayList<>();
        // Esta consulta une productos con el inventario de TU sucursal
        String sql = "SELECT p.id, p.codigo, p.nombre, p.precio, i.cantidad_disponible " +
                "FROM productos p " +
                "JOIN inventario_sucursales i ON p.id = i.id_producto " +
                "WHERE i.id_sucursal = ? " +
                "AND (LOWER(p.nombre) LIKE ? OR p.codigo LIKE ?) " +
                "AND p.activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idSucursal);
            pstmt.setString(2, "%" + busqueda.toLowerCase() + "%");
            pstmt.setString(3, "%" + busqueda + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("cantidad_disponible")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean guardar(Producto p) {
        // El código se genera automáticamente por el trigger de la BD si no se envía
        String sql = "INSERT INTO productos (nombre, precio, id_receta, tipo, sabor, descripcion) " +
                "VALUES (?, ?, ?, ?::tipo_producto, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getIdReceta());
            ps.setString(4, p.getTipo()); // Debe coincidir con el ENUM ('Pan', 'Bebida'...)
            ps.setString(5, p.getSabor());
            ps.setString(6, p.getDescripcion());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}