package org.example.Model.Entities;

public class Producto {
    private int id;
    private String codigo;
    private String nombre;
    private double precio;
    private int existencia; // Stock (solo lectura)

    // Nuevos campos para el registro
    private int idReceta;
    private String tipo; // Enum: Bebida, Pan, Postre
    private String sabor;
    private String descripcion;

    // Constructor para el POS (Simple)
    public Producto(int id, String codigo, String nombre, double precio, int existencia) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.existencia = existencia;
    }

    // Constructor vacío para el formulario
    public Producto() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getExistencia() { return existencia; }
    // No hay setExistencia porque es calculado por la BD

    public int getIdReceta() { return idReceta; }
    public void setIdReceta(int idReceta) { this.idReceta = idReceta; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getSabor() { return sabor; }
    public void setSabor(String sabor) { this.sabor = sabor; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() { return nombre; }
}