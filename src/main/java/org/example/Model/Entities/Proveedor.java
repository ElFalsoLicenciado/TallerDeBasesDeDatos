package org.example.Model.Entities;

public class Proveedor {
    private int id;
    private String nombre;
    private String rfc;

    public Proveedor(int id, String nombre, String rfc) {
        this.id = id;
        this.nombre = nombre;
        this.rfc = rfc;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() { return nombre; }
}