package Model.Entities;

public class Usuario {
    private int id;
    private String usuario;
    private String contrasena; // Guardará el hash, aunque no lo usaremos mucho en la UI
    private int idRol;
    private boolean activo;

    public Usuario() {}

    public Usuario(int id, String usuario, int idRol) {
        this.id = id;
        this.usuario = usuario;
        this.idRol = idRol;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}