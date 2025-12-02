package Model.Entities;

public class RecetaDetalle {
    private String ingrediente;
    private double cantidadRequeridaBase;
    private double cantidadRequeridaTotal;
    private double stockDisponible;
    private String unidad;

    public RecetaDetalle(String ingrediente, double requeridaBase, double stockDisponible, String unidad) {
        this.ingrediente = ingrediente;
        this.cantidadRequeridaBase = requeridaBase;
        this.stockDisponible = stockDisponible;
        this.unidad = unidad;
        this.cantidadRequeridaTotal = requeridaBase; // Por defecto x1
    }

    public void calcularTotal(double cantidadAProducir) {
        this.cantidadRequeridaTotal = this.cantidadRequeridaBase * cantidadAProducir;
    }

    public boolean esSuficiente() {
        return stockDisponible >= cantidadRequeridaTotal;
    }

    // --- GETTERS OBLIGATORIOS PARA LA TABLA ---
    public String getIngrediente() { return ingrediente; }
    public double getCantidadRequeridaTotal() { return cantidadRequeridaTotal; }
    public double getStockDisponible() { return stockDisponible; }
    public String getUnidad() { return unidad; }
}