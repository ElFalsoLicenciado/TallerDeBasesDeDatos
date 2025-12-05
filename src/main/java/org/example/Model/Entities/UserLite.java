package org.example.Model.Entities;

public class UserLite {
    private int id; // id de la tabla usuarios
    private String username;
    private String nombreCompleto;

    public UserLite(int id, String username, String nombreCompleto) {
        this.id = id;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
    }

    public int getId() { return id; }

    @Override
    public String toString() { return username + " - " + nombreCompleto; }
}