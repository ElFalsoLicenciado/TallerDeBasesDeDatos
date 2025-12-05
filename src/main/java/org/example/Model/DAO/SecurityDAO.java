package org.example.Model.DAO;

import org.example.Model.Entities.Permiso;
import org.example.Model.Entities.Rol;
import org.example.Model.Entities.UserLite;
import org.example.Model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SecurityDAO {

    // --- CARGA DE DATOS ---

    public List<Rol> getAllRoles() {
        List<Rol> list = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion FROM roles WHERE activo = TRUE ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Rol(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<UserLite> getAllUsers() {
        List<UserLite> list = new ArrayList<>();
        // Usamos la vista vista_empleados_completa que ya definiste en el schema
        String sql = "SELECT id_usuario, usuario, nombre_completo FROM vista_empleados_completa WHERE usuario_activo = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new UserLite(rs.getInt("id_usuario"), rs.getString("usuario"), rs.getString("nombre_completo")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Permiso> getAllPermissions() {
        List<Permiso> list = new ArrayList<>();
        String sql = "SELECT id, codigo, descripcion, modulo FROM permisos ORDER BY modulo, codigo";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Permiso(rs.getInt("id"), rs.getString("codigo"), rs.getString("descripcion"), rs.getString("modulo")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- LÓGICA DE ROLES ---

    public List<Integer> getPermissionIdsByRole(int roleId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_permiso FROM roles_permisos WHERE id_rol = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) ids.add(rs.getInt("id_permiso"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    public void updateRolePermissions(int roleId, List<Integer> permissionIds) {
        String deleteSql = "DELETE FROM roles_permisos WHERE id_rol = ?";
        String insertSql = "INSERT INTO roles_permisos (id_rol, id_permiso) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                delStmt.setInt(1, roleId);
                delStmt.executeUpdate();
            }
            try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                for (Integer pId : permissionIds) {
                    insStmt.setInt(1, roleId);
                    insStmt.setInt(2, pId);
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- LÓGICA DE USUARIOS (Excepciones) ---

    // Devuelve permisos explícitos asignados al usuario (ignora los del rol por ahora)
    public List<Integer> getExplicitUserPermissionIds(int userId) {
        List<Integer> ids = new ArrayList<>();
        // Solo traemos los 'allow' para visualizarlos en los checkbox
        String sql = "SELECT id_permiso FROM usuarios_permisos WHERE id_usuario = ? AND tipo = 'allow'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) ids.add(rs.getInt("id_permiso"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    public void updateUserPermissions(int userId, List<Integer> permissionIds) {
        // Esta función asume que marcamos permisos EXTRA para el usuario (tipo 'allow')
        String deleteSql = "DELETE FROM usuarios_permisos WHERE id_usuario = ?";
        String insertSql = "INSERT INTO usuarios_permisos (id_usuario, id_permiso, tipo) VALUES (?, ?, 'allow')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                delStmt.setInt(1, userId);
                delStmt.executeUpdate();
            }
            try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                for (Integer pId : permissionIds) {
                    insStmt.setInt(1, userId);
                    insStmt.setInt(2, pId);
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}