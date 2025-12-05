package org.example.Model.Entities;

public class Ingrediente {
    private int id;
    private String codigo;
    private String nombre;
    private double costo;
    private String unidad;
    private double stock; // Cantidad actual

    public Ingrediente(int id, String codigo, String nombre, double costo, String unidad, double stock) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.costo = costo;
        this.unidad = unidad;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public double getCosto() { return costo; }
    public String getUnidad() { return unidad; }
    public double getStock() { return stock; }

    @Override
    public String toString() { return nombre + " (" + unidad + ")"; }
}