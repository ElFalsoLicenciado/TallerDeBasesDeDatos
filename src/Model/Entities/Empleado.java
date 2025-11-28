package Model.Entities;

import java.sql.Date;

public class Empleado {
    private int id;
    private String codigo;
    private String nombres;
    private String apellidos;
    private Date fechaNacimiento;
    private String lugarNacimiento;
    private String direccion;
    private String telefono;
    private String correo;
    private String tipo; // "Administrativo" o "Produccion"
    private int idSucursal;
    private boolean activo;

    // Constructor vacío
    public Empleado() {}

    // Constructor completo
    public Empleado(int id, String codigo, String nombres, String apellidos, Date fechaNacimiento,
                    String lugarNacimiento, String direccion, String telefono, String correo,
                    String tipo, int idSucursal, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.lugarNacimiento = lugarNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
        this.tipo = tipo;
        this.idSucursal = idSucursal;
        this.activo = activo;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getLugarNacimiento() { return lugarNacimiento; }
    public void setLugarNacimiento(String lugarNacimiento) { this.lugarNacimiento = lugarNacimiento; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() { return nombres + " " + apellidos; }
}