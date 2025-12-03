package Model.Entities;

public class InventarioItem {
    private String codigo;
    private String producto;
    private String tipo;
    private double precio;
    private int cantidad;
    private int minimo;
    private String estado; // "AGOTADO", "CRÍTICO", "BAJO", "ADECUADO"

    public InventarioItem(String codigo, String producto, String tipo, double precio, int cantidad, int minimo, String estado) {
        this.codigo = codigo;
        this.producto = producto;
        this.tipo = tipo;
        this.precio = precio;
        this.cantidad = cantidad;
        this.minimo = minimo;
        this.estado = estado;
    }

    // Getters
    public String getCodigo() { return codigo; }
    public String getProducto() { return producto; }
    public String getTipo() { return tipo; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public int getMinimo() { return minimo; }
    public String getEstado() { return estado; }
}