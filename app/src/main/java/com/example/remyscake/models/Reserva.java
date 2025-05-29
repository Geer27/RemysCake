package com.example.remyscake.models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.Map;

@IgnoreExtraProperties
public class Reserva {
    @Exclude
    public String id;

    public String clientId;
    public String clienteNombre; // Desnormalizado
    public String createdBy; // UID del seller
    public String sellerNombre; // Desnormalizado
    public long createdAt; // Timestamp
    public long deliveryAt; // Timestamp
    public String status; // "pendiente", "confirmada", "en_produccion", "lista_para_entrega", "entregada", "cancelada"
    public String notasAdicionalesPedido;


    public Map<String, ItemPedido> items;

    public Payment payment; // Objeto Payment anidado

    public Reserva() {
        // Constructor vacío para Firebase
    }

    public Reserva(String clientId, String clienteNombre, String createdBy, String sellerNombre,
                   long createdAt, long deliveryAt, String status, Map<String, ItemPedido> items,
                   Payment payment, String notasAdicionalesPedido) {
        this.clientId = clientId;
        this.clienteNombre = clienteNombre;
        this.createdBy = createdBy;
        this.sellerNombre = sellerNombre;
        this.createdAt = createdAt;
        this.deliveryAt = deliveryAt;
        this.status = status;
        this.items = items;
        this.payment = payment;
        this.notasAdicionalesPedido = notasAdicionalesPedido;
    }

    // Getters
    @Exclude
    public String getId() { return id; }
    public String getClientId() { return clientId; }
    public String getClienteNombre() { return clienteNombre; }
    public String getCreatedBy() { return createdBy; }
    public String getSellerNombre() { return sellerNombre; }
    public long getCreatedAt() { return createdAt; }
    public long getDeliveryAt() { return deliveryAt; }
    public String getStatus() { return status; }
    public Map<String, ItemPedido> getItems() { return items; }
    public Payment getPayment() { return payment; }
    public String getNotasAdicionalesPedido() { return notasAdicionalesPedido; }


    // Setters
    @Exclude
    public void setId(String id) { this.id = id; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setSellerNombre(String sellerNombre) { this.sellerNombre = sellerNombre; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setDeliveryAt(long deliveryAt) { this.deliveryAt = deliveryAt; }
    public void setStatus(String status) { this.status = status; }
    public void setItems(Map<String, ItemPedido> items) { this.items = items; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public void setNotasAdicionalesPedido(String notasAdicionalesPedido) { this.notasAdicionalesPedido = notasAdicionalesPedido; }
}