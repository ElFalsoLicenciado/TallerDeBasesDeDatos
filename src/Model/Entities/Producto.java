package Model.Entities;

public class Producto {
    private int id;
    private String codigo;
    private String nombre;
    private double precio;
    private int existencia; // Stock disponible en esa sucursal

    public Producto(int id, String codigo, String nombre, double precio, int existencia) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.existencia = existencia;
    }

    // Getters
    public int getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getExistencia() { return existencia; }

    // toString para debug o combobox
    @Override
    public String toString() { return nombre; }
}