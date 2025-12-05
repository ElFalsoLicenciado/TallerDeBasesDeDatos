package org.example.Model.DAO;

import org.example.Model.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAO {

    /**
     * Obtiene el nombre de la sucursal.
     * @return El nombre (ej. "Lua's Place Centro") o null si no existe.
     */
    public String obtenerNombreSucursal(int idSucursal) {
        String sql = "SELECT nombre FROM sucursales WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("nombre");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Lua's Place"; // Fallback
    }

    /**
     * Suma el total de ventas del día actual (CURRENT_DATE).
     * Si idSucursal es 0, suma todas las sucursales.
     */
    public double obtenerVentasDelDia(int idSucursal) {
        String sql;
        if (idSucursal > 0) {
            sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE id_sucursal = ? AND DATE(fecha) = CURRENT_DATE";
        } else {
            sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha) = CURRENT_DATE";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (idSucursal > 0) ps.setInt(1, idSucursal);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    /**
     * Cuenta cuántos productos están en estado CRÍTICO o BAJO.
     * Si idSucursal es 0, cuenta en toda la cadena.
     */
    public int obtenerConteoStockBajo(int idSucursal) {
        String sql;
        // Usamos la lógica: stock <= minimo * 2 (que cubre BAJO y CRÍTICO)
        if (idSucursal > 0) {
            sql = "SELECT COUNT(*) FROM inventario_sucursales WHERE id_sucursal = ? AND cantidad_disponible <= (cantidad_minima * 2)";
        } else {
            sql = "SELECT COUNT(*) FROM inventario_sucursales WHERE cantidad_disponible <= (cantidad_minima * 2)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (idSucursal > 0) ps.setInt(1, idSucursal);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}