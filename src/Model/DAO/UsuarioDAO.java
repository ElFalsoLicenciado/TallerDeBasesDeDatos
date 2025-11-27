package Model.DAO;

import Model.DatabaseConnection;
import Model.Entities.Usuario;
import Util.HashPassword;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet; // <--- Importante
import java.util.Set;     // <--- Importante

public class UsuarioDAO {

    public Usuario login(String usuario, String passwordTextoPlano) {
        String sql = "SELECT u.*, e.nombres, e.apellidos, e.id_sucursal " +
                "FROM usuarios u " +
                "JOIN empleados e ON u.id_empleado = e.id " +
                "WHERE u.usuario = ? AND u.activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashGuardado = rs.getString("contrasena");
                String hashIngresado = HashPassword.hashString(passwordTextoPlano);

                if (hashIngresado != null && hashIngresado.equals(hashGuardado)) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsuario(rs.getString("usuario"));
                    u.setIdRol(rs.getInt("id_rol"));
                    u.setActivo(rs.getBoolean("activo"));

                    String nombre = rs.getString("nombres");
                    String apellido = rs.getString("apellidos");
                    // Concatenamos nombre y apellido para que se vea mejor
                    u.setNombreCompleto(nombre + " " + apellido);
                    u.setIdSucursal(rs.getInt("id_sucursal"));

                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en Login DAO: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene la lista de códigos de permisos (ej: 'ventas.crear') asociados al rol.
     */
    public Set<String> obtenerPermisos(int idRol) {
        Set<String> permisos = new HashSet<>();

        // Consulta que une roles_permisos con la tabla permisos
        String sql = "SELECT p.codigo " +
                "FROM permisos p " +
                "JOIN roles_permisos rp ON p.id = rp.id_permiso " +
                "WHERE rp.id_rol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idRol);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                permisos.add(rs.getString("codigo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permisos;
    }
}