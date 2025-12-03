package Model.Entities;

public class Cliente {
    private int id;
    private String nombre;
    private String apellido;
    private String rfc;

    public Cliente(int id, String nombre, String apellido, String rfc) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rfc = rfc;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getRfc() { return rfc; }

    @Override
    public String toString() {
        return nombre + " " + apellido + " (" + (rfc != null ? rfc : "S/RFC") + ")";
    }
}