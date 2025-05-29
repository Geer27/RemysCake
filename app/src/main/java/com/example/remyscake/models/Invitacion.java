package com.example.remyscake.models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Invitacion {
    @Exclude
    public String idInvitacion;

    public String emailInvitado;
    public String rolAsignado;
    public String codigoInvitacion;
    public long fechaCreacion;
    public String estado; // "pendiente", "usada", "expirada"
    public String creadaPorUid; // UID del admin que la creó

    public Invitacion() {
        // Constructor vacío
    }

    public Invitacion(String emailInvitado, String rolAsignado, String codigoInvitacion, long fechaCreacion, String estado, String creadaPorUid) {
        this.emailInvitado = emailInvitado;
        this.rolAsignado = rolAsignado;
        this.codigoInvitacion = codigoInvitacion;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.creadaPorUid = creadaPorUid;
    }

    @Exclude
    public String getIdInvitacion() { return idInvitacion; }
    @Exclude
    public void setIdInvitacion(String idInvitacion) { this.idInvitacion = idInvitacion; }

    public String getEmailInvitado() { return emailInvitado; }
    public void setEmailInvitado(String emailInvitado) { this.emailInvitado = emailInvitado; }

    public String getRolAsignado() { return rolAsignado; }
    public void setRolAsignado(String rolAsignado) { this.rolAsignado = rolAsignado; }

    public String getCodigoInvitacion() { return codigoInvitacion; }
    public void setCodigoInvitacion(String codigoInvitacion) { this.codigoInvitacion = codigoInvitacion; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCreadaPorUid() { return creadaPorUid; }
    public void setCreadaPorUid(String creadaPorUid) { this.creadaPorUid = creadaPorUid; }
}