package org.example.Model.DAO;

import org.example.Model.DatabaseConnection;
import org.example.Model.Entities.Proveedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {
    public List<Proveedor> listarTodos() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, rfc FROM proveedores WHERE activo = TRUE ORDER BY nombre";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("rfc")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}