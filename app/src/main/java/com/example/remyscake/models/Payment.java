package com.example.remyscake.models;


import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Payment {
    public double amount;
    public String method;
    public String statusPago;
    public long timestampPago;
    public Payment() {
        // Constructor vacío para Firebase
    }

    public Payment(double amount, String method, String statusPago, long timestampPago) {
        this.amount = amount;
        this.method = method;
        this.statusPago = statusPago;
        this.timestampPago = timestampPago;
    }

    // Getters y Setters
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatusPago() { return statusPago; }
    public void setStatusPago(String statusPago) { this.statusPago = statusPago; }
    public long getTimestampPago() { return timestampPago; }
    public void setTimestampPago(long timestampPago) { this.timestampPago = timestampPago; }
}