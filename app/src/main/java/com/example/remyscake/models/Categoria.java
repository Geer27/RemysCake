package com.example.remyscake.models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Categoria {
    @Exclude
    public String id; // ID del nodo de Firebase

    public String nombre;
    public String descripcion; // Opcional

    public Categoria() {
        // Constructor vacío requerido por Firebase
    }

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Para usar en ArrayAdapter (muestra el nombre en el Spinner/AutoCompleteTextView)
    @Override
    public String toString() {
        return nombre;
    }
}