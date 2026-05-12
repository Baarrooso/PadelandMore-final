package com.example.tmanager;

public class Pista {
    private String id;
    private String clubId;
    private String nombre;
    private double precio;
    private String tipo; // Interior, Exterior
    private int capacidad;
    private boolean disponible;
    
    public Pista() {}
    
    public Pista(String id, String clubId, String nombre, double precio, String tipo, int capacidad, boolean disponible) {
        this.id = id;
        this.clubId = clubId;
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.disponible = disponible;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getClubId() {
        return clubId;
    }
    
    public void setClubId(String clubId) {
        this.clubId = clubId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public double getPrecio() {
        return precio;
    }
    
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public int getCapacidad() {
        return capacidad;
    }
    
    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
    
    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}

