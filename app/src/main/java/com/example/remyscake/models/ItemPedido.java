package com.example.remyscake.models;

import java.io.Serializable;

public class ItemPedido implements Serializable {
    public String productoId;
    public String productoNombre;
    public int cantidad;
    public double precioUnitario;
    public String imagenUrl;

    public ItemPedido() {}
    public ItemPedido(String productoId, String productoNombre, int cantidad, double precioUnitario, String imagenUrl) {
        this.productoId = productoId; this.productoNombre = productoNombre;
        this.cantidad = cantidad; this.precioUnitario = precioUnitario; this.imagenUrl = imagenUrl;
    }
    // Getters y Setters...
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}