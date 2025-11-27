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
}