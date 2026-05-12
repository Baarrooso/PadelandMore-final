package com.example.tmanager;

public class Club {
    private String id;
    private String nombre;
    private String ubicacion;
    private String imagenUrl;
    
    public Club() {}
    
    public Club(String id, String nombre, String ubicacion, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.imagenUrl = imagenUrl;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getUbicacion() {
        return ubicacion;
    }
    
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
    
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}

