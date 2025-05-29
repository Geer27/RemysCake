package com.example.remyscake.models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Cliente implements Serializable {
    @Exclude
    public String id;

    public String nombreCompleto;
    public String telefono;
    public String email;
    public String direccionHabitual;
    public long fechaRegistro;
    public String creadoPor;
    public boolean activo;

    public Cliente() {
        // Constructor vacío requerido por Firebase
    }
    public Cliente(String nombreCompleto, String telefono, String email) {
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.email = email;
    }

    public Cliente(String nombreCompleto, String telefono, String email, String direccionHabitual, long fechaRegistro, String creadoPor, boolean activo) {
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.email = email;
        this.direccionHabitual = direccionHabitual;
        this.fechaRegistro = fechaRegistro;
        this.creadoPor = creadoPor;
        this.activo = activo;
    }

    // Getters
    @Exclude
    public String getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getDireccionHabitual() { return direccionHabitual; }
    public long getFechaRegistro() { return fechaRegistro; }
    public String getCreadoPor() { return creadoPor; }
    public boolean isActivo() { return activo; }


    // Setters
    @Exclude
    public void setId(String id) { this.id = id; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }
    public void setDireccionHabitual(String direccionHabitual) { this.direccionHabitual = direccionHabitual; }
    public void setFechaRegistro(long fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public void setCreado_por(String creadoPor) { this.creadoPor = creadoPor; }
    public void setActivo(boolean activo) { this.activo = activo; }
}