package Util;

import Model.Entities.Usuario;
import java.util.HashSet;
import java.util.Set;

public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;

    private Set<String> permisosActuales;

    private SessionManager() {
        permisosActuales = new HashSet<>();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Usuario usuario, Set<String> permisos) {
        this.usuarioActual = usuario;
        this.permisosActuales = permisos;
        System.out.println("LOG: Sesión iniciada para " + usuario.getUsuario());
    }

    public void logout() {
        this.usuarioActual = null;
        if (this.permisosActuales != null) {
            this.permisosActuales.clear();
        }
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean tienePermiso(String codigoPermiso) {
        if (usuarioActual == null || permisosActuales == null) return false;

        if (permisosActuales.contains("sys.full_access")) return true;

        return permisosActuales.contains(codigoPermiso);
    }
}