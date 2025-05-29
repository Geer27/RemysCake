package com.example.remyscake.utils;


public class ConstantesApp {

    // Roles de Usuario
    public static final String ROL_VENDEDOR = "seller";
    public static final String ROL_PRODUCCION = "production";
    public static final String ROL_ADMIN = "admin";

    // Nombres de Nodos en Firebase
    public static final String NODO_USUARIOS = "usuarios";
    public static final String NODO_CATALOGO_PASTELES = "catalogo_pasteles";
    public static final String NODO_CLIENTES = "clientes";
    public static final String NODO_RESERVACIONES = "reservaciones";
    public static final String NODO_CATEGORIAS_CATALOGO = "categorias_catalogo";

    // Estados de las Reservaciones
    public static final String ESTADO_RESERVA_PENDIENTE = "pendiente";
    public static final String ESTADO_RESERVA_CONFIRMADA = "confirmada";
    public static final String ESTADO_RESERVA_EN_PRODUCCION = "en_produccion";
    public static final String ESTADO_RESERVA_LISTA_ENTREGA = "lista_para_entrega";
    public static final String ESTADO_RESERVA_ENTREGADA = "entregada";
    public static final String ESTADO_RESERVA_CANCELADA = "cancelada";

    public static final String ESTADO_PAGO_PENDIENTE = "pendiente";
    public static final String ESTADO_PAGO_COMPLETADO = "completado";
    public static final String ESTADO_PAGO_FALLIDO = "fallido";
    public static final String ESTADO_PAGO_REEMBOLSADO = "reembolsado";
    public static final String ESTADO_PAGO_A_DEFINIR = "a_definir";

}