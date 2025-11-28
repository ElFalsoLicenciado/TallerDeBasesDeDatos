package Model.DAO;

import Model.DatabaseConnection;
import Model.Entities.Empleado;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmpleadoDAO {

    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleados WHERE activo = TRUE ORDER BY apellidos";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSetToEmpleado(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean guardar(Empleado e) {
        String sql;
        boolean esNuevo = (e.getId() == 0);

        if (esNuevo) {
            sql = "INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, " +
                    "direccion, telefono, correo, tipo, id_sucursal) VALUES (?,?,?,?,?,?,?,?::tipo_empleado,?)";
        } else {
            sql = "UPDATE empleados SET nombres=?, apellidos=?, fecha_nacimiento=?, lugar_nacimiento=?, " +
                    "direccion=?, telefono=?, correo=?, tipo=?::tipo_empleado, id_sucursal=? WHERE id=?";
        }

        // CAMBIO IMPORTANTE: Statement.RETURN_GENERATED_KEYS
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, e.getNombres());
            pstmt.setString(2, e.getApellidos());
            pstmt.setDate(3, e.getFechaNacimiento());
            pstmt.setString(4, e.getLugarNacimiento());
            pstmt.setString(5, e.getDireccion());
            pstmt.setString(6, e.getTelefono());
            pstmt.setString(7, e.getCorreo());
            pstmt.setString(8, e.getTipo());

            if (e.getIdSucursal() == 0) pstmt.setNull(9, Types.INTEGER);
            else pstmt.setInt(9, e.getIdSucursal());

            if (!esNuevo) pstmt.setInt(10, e.getId());

            int affectedRows = pstmt.executeUpdate();

            // SI ES NUEVO, RECUPERAMOS EL ID GENERADO
            if (affectedRows > 0 && esNuevo) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        e.setId(generatedKeys.getInt(1)); // <--- ¡AQUÍ ACTUALIZAMOS EL ID!
                    }
                }
            }
            return affectedRows > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int idEmpleado) {
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement psEmpleado = null;
        PreparedStatement psUsuario = null;

        String sqlEmpleado = "UPDATE empleados SET activo = FALSE WHERE id = ?";
        // También desactivamos el usuario vinculado (si existe)
        String sqlUsuario = "UPDATE usuarios SET activo = FALSE WHERE id_empleado = ?";

        try {
            // 1. Iniciamos Transacción
            conn.setAutoCommit(false);

            // 2. Desactivar Empleado
            psEmpleado = conn.prepareStatement(sqlEmpleado);
            psEmpleado.setInt(1, idEmpleado);
            int filasEmpleado = psEmpleado.executeUpdate();

            // 3. Desactivar Usuario vinculado
            psUsuario = conn.prepareStatement(sqlUsuario);
            psUsuario.setInt(1, idEmpleado);
            psUsuario.executeUpdate(); // No importa si devuelve 0 (si no tenía usuario)

            if (filasEmpleado > 0) {
                conn.commit(); // Confirmar cambios
                return true;
            } else {
                conn.rollback(); // Si falló el empleado, deshacer
                return false;
            }

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
                if (psEmpleado != null) psEmpleado.close();
                if (psUsuario != null) psUsuario.close();
                if (conn != null) conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Helper para llenar el ComboBox de sucursales
    public Map<Integer, String> obtenerSucursales() {
        Map<Integer, String> map = new HashMap<>();
        String sql = "SELECT id, nombre FROM sucursales WHERE activo = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("nombre"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    private Empleado mapResultSetToEmpleado(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getInt("id"), rs.getString("codigo"), rs.getString("nombres"),
                rs.getString("apellidos"), rs.getDate("fecha_nacimiento"), rs.getString("lugar_nacimiento"),
                rs.getString("direccion"), rs.getString("telefono"), rs.getString("correo"),
                rs.getString("tipo"), rs.getInt("id_sucursal"), rs.getBoolean("activo")
        );
    }
}