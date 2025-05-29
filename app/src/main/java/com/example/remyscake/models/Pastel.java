package com.example.remyscake.models;


import com.google.firebase.database.Exclude; // Para el ID local
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Map; // Para opciones de personalización

@IgnoreExtraProperties
public class Pastel implements Serializable {

    @Exclude // Para que Firebase no intente guardar/leer 'id' directamente del objeto, lo manejaremos como key del nodo
    public String id; // ID del nodo de Firebase

    public String nombre;
    public String descripcion;
    public double precioBase;
    public String categoria;
    public String imagenUrl;
    public boolean disponible;
    public Map<String, Map<String, Double>> opcionesPersonalizacion;

    // Constructor vacío requerido por Firebase
    public Pastel() {
    }

    // Constructor principal
    public Pastel(String nombre, String descripcion, double precioBase, String categoria, String imagenUrl, boolean disponible, Map<String, Map<String, Double>> opcionesPersonalizacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
        this.categoria = categoria;
        this.imagenUrl = imagenUrl;
        this.disponible = disponible;
        this.opcionesPersonalizacion = opcionesPersonalizacion;
    }

    // Getters
    @Exclude
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public Map<String, Map<String, Double>> getOpcionesPersonalizacion() {
        return opcionesPersonalizacion;
    }

    // Setters
    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecioBase(double precioBase) {
        this.precioBase = precioBase;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public void setOpcionesPersonalizacion(Map<String, Map<String, Double>> opcionesPersonalizacion) {
        this.opcionesPersonalizacion = opcionesPersonalizacion;
    }
}