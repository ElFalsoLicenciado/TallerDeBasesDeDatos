package org.example.Model.Entities;

public class Usuario {
    private int id;
    private int idEmpleado; // <--- CAMPO AÑADIDO
    private String usuario;
    private String contrasena;
    private String nombreCompleto;
    private int idRol;
    private boolean activo;
    private int idSucursal;

    public Usuario() {}

    public Usuario(int id, String usuario, int idRol) {
        this.id = id;
        this.usuario = usuario;
        this.idRol = idRol;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // --- MÉTODOS AÑADIDOS PARA SOLUCIONAR EL ERROR ---
    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }
    // ------------------------------------------------

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
}