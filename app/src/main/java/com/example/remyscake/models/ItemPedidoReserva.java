package com.example.remyscake.models;


import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ItemPedidoReserva {
    public String productoId;
    public String nombreProducto;
    public int cantidad;
    public double precioUnitarioCalculado;

    public ItemPedidoReserva() {
        // Constructor vacío para Firebase
    }

    public ItemPedidoReserva(String productoId, String nombreProducto, int cantidad, double precioUnitarioCalculado) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitarioCalculado = precioUnitarioCalculado;
    }

    // Getters y Setters
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioUnitarioCalculado() { return precioUnitarioCalculado; }
    public void setPrecioUnitarioCalculado(double precioUnitarioCalculado) { this.precioUnitarioCalculado = precioUnitarioCalculado; }
}