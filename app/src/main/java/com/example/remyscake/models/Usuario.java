package com.example.remyscake.models;



import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Usuario {

    @Exclude // El ID es la clave del nodo, no un campo del objeto JSON en Firebase
    public String id;

    public String nombreCompleto;
    public String correoElectronico;
    public String rol;

    // Constructor vacío requerido para Firebase
    public Usuario() {
    }

    public Usuario(String nombreCompleto, String correoElectronico, String rol) {
        this.nombreCompleto = nombreCompleto;
        this.correoElectronico = correoElectronico;
        this.rol = rol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}