package org.example.Model.DAO;

import org.example.Model.DatabaseConnection;
import org.example.Model.Entities.Usuario;
import org.example.Util.HashPassword;

import java.sql.*;
import java.util.HashSet; // <--- Importante
import java.util.Set;     // <--- Importante
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Busca si existe un usuario asignado a un empleado específico.
     */
    public Usuario buscarPorIdEmpleado(int idEmpleado) {
        String sql = "SELECT * FROM usuarios WHERE id_empleado = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idEmpleado);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsuario(rs.getString("usuario"));
                u.setIdRol(rs.getInt("id_rol"));
                u.setIdEmpleado(rs.getInt("id_empleado")); // Asegúrate de tener este setter en Entidad Usuario
                u.setActivo(rs.getBoolean("activo"));
                return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Crea o actualiza un usuario de sistema.
     */
    public boolean guardarUsuarioSistema(Usuario u, boolean esNuevo, String passwordSinHash) {
        String sql;
        if (esNuevo) {
            sql = "INSERT INTO usuarios (id_empleado, usuario, contrasena, id_rol, activo) VALUES (?, ?, ?, ?, ?)";
        } else {
            // Si el password viene vacío/null, no lo actualizamos
            if (passwordSinHash == null || passwordSinHash.isEmpty()) {
                sql = "UPDATE usuarios SET usuario=?, id_rol=?, activo=? WHERE id=?";
            } else {
                sql = "UPDATE usuarios SET usuario=?, id_rol=?, activo=?, contrasena=? WHERE id=?";
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (esNuevo) {
                pstmt.setInt(1, u.getIdEmpleado());
                pstmt.setString(2, u.getUsuario());
                // Hashear contraseña nueva
                pstmt.setString(3, org.example.Util.HashPassword.hashString(passwordSinHash));
                pstmt.setInt(4, u.getIdRol());
                pstmt.setBoolean(5, u.isActivo());
            } else {
                // Update
                pstmt.setString(1, u.getUsuario());
                pstmt.setInt(2, u.getIdRol());
                pstmt.setBoolean(3, u.isActivo());

                if (passwordSinHash != null && !passwordSinHash.isEmpty()) {
                    pstmt.setString(4,org.example.Util.HashPassword.hashString(passwordSinHash));
                    pstmt.setInt(5, u.getId());
                } else {
                    pstmt.setInt(4, u.getId());
                }
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene los roles disponibles para llenar el ComboBox.
     */
    public Map<Integer, String> obtenerRoles() {
        Map<Integer, String> roles = new HashMap<>();
        String sql = "SELECT id, nombre FROM roles WHERE activo = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                roles.put(rs.getInt("id"), rs.getString("nombre"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return roles;
    }
}