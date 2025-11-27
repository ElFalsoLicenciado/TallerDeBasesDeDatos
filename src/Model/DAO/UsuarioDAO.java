package Model.DAO;

import Model.DatabaseConnection;
import Model.Entities.Usuario;
import Util.HashPassword; // <--- Importamos tu nueva clase

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario login(String usuario, String passwordTextoPlano) {
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND activo = TRUE";

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
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en Login DAO: " + e.getMessage());
        }
        return null;
    }
}