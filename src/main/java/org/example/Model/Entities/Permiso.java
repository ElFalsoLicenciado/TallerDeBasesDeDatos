package org.example.Model.Entities;

public class Permiso {
    private int id;
    private String codigo;
    private String descripcion;
    private String modulo;

    public Permiso(int id, String codigo, String descripcion, String modulo) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.modulo = modulo;
    }

    public int getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public String getModulo() { return modulo; }
}