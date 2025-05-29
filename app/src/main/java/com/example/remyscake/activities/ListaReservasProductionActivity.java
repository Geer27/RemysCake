package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.adapters.ReservasProductionAdapter;
import com.example.remyscake.models.Reserva;
import com.example.remyscake.utils.ConstantesApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListaReservasProductionActivity extends AppCompatActivity implements ReservasProductionAdapter.OnReservaProductionActionsListener {

    private static final String ETIQUETA_DEBUG = "ListaReservasProd";

    private ImageButton btnBackListaReservasProd;
    private TextView tvTituloListaReservasProd, tvSinReservasEnListaProd;
    private RecyclerView rvListaReservasProd;

    private ReservasProductionAdapter adapter;
    private List<Reserva> listaReservas = new ArrayList<>();

    private DatabaseReference referenciaReservacionesBD;
    private ValueEventListener listenerReservas;
    private Query queryReservas; // Para poder remover el listener de la query específica

    private String estadoFiltro = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_reservas_production);

        referenciaReservacionesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);

        if (getIntent().hasExtra("ESTADO_FILTRO_PRODUCCION")) {
            estadoFiltro = getIntent().getStringExtra("ESTADO_FILTRO_PRODUCCION");
        } else {
            Toast.makeText(this, "No se especificó un estado para filtrar.", Toast.LENGTH_LONG).show();
            finish(); // Salir si no hay filtro
            return;
        }

        inicializarVistas();
        configurarRecyclerView();
        establecerListeners();

        // El título se actualiza después de inicializar vistas
        if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
            String titulo = "Reservas: " + estadoFiltro.substring(0, 1).toUpperCase() + estadoFiltro.substring(1).replace("_", " ");
            tvTituloListaReservasProd.setText(titulo);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarReservasFiltradas();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (queryReservas != null && listenerReservas != null) {
            queryReservas.removeEventListener(listenerReservas);
        }
    }

    private void inicializarVistas() {
        btnBackListaReservasProd = findViewById(R.id.btnBackListaReservasProd);
        tvTituloListaReservasProd = findViewById(R.id.tvTituloListaReservasProd);
        rvListaReservasProd = findViewById(R.id.rvListaReservasProd);
        tvSinReservasEnListaProd = findViewById(R.id.tvSinReservasEnListaProd);
    }

    private void configurarRecyclerView() {
        rvListaReservasProd.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservasProductionAdapter(listaReservas, this, this);
        rvListaReservasProd.setAdapter(adapter);
    }

    private void establecerListeners() {
        btnBackListaReservasProd.setOnClickListener(v -> finish());
    }

    private void cargarReservasFiltradas() {
        tvSinReservasEnListaProd.setText("Cargando reservaciones...");
        tvSinReservasEnListaProd.setVisibility(View.VISIBLE);
        rvListaReservasProd.setVisibility(View.GONE);

        if (queryReservas != null && listenerReservas != null) { // Remover listener anterior de la query anterior
            queryReservas.removeEventListener(listenerReservas);
        }

        // Filtrar por estado y ordenar por fecha de entrega
        queryReservas = referenciaReservacionesBD.orderByChild("status").equalTo(estadoFiltro);

        listenerReservas = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaReservas.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Reserva reserva = snapshot.getValue(Reserva.class);
                        if (reserva != null) {
                            reserva.setId(snapshot.getKey());
                            listaReservas.add(reserva);
                        }
                    }
                    Collections.sort(listaReservas, Comparator.comparingLong(Reserva::getDeliveryAt));
                }

                if (listaReservas.isEmpty()) {
                    tvSinReservasEnListaProd.setText("No hay reservaciones en estado: " + estadoFiltro);
                    tvSinReservasEnListaProd.setVisibility(View.VISIBLE);
                    rvListaReservasProd.setVisibility(View.GONE);
                } else {
                    tvSinReservasEnListaProd.setVisibility(View.GONE);
                    rvListaReservasProd.setVisibility(View.VISIBLE);
                    adapter.actualizarLista(listaReservas);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar reservaciones filtradas: ", databaseError.toException());
                Toast.makeText(ListaReservasProductionActivity.this, "Error al cargar datos.", Toast.LENGTH_SHORT).show();
                tvSinReservasEnListaProd.setText("Error al cargar reservaciones.");
                tvSinReservasEnListaProd.setVisibility(View.VISIBLE);
            }
        };
        queryReservas.addValueEventListener(listenerReservas);
    }


    // Implementación de OnReservaProductionActionsListener del Adaptador
    @Override
    public void onCambiarEstadoClick(Reserva reserva, String nuevoEstado) {
        // Confirmación antes de cambiar estado
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Cambio de Estado")
                .setMessage("¿Estás seguro de que quieres cambiar el estado de la reserva #" +
                        reserva.getId().substring(Math.max(0, reserva.getId().length() - 6)) +
                        " a '" + nuevoEstado.replace("_", " ") + "'?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    actualizarEstadoReservaEnFirebase(reserva.getId(), nuevoEstado);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onVerDetalleProduccionClick(Reserva reserva) {
        Toast.makeText(this, "Viendo detalle de reserva: " + reserva.getClienteNombre(), Toast.LENGTH_SHORT).show();
    }

    private void actualizarEstadoReservaEnFirebase(String idReserva, String nuevoEstado) {
        if (idReserva == null || nuevoEstado == null) {
            Toast.makeText(this, "Error: Datos inválidos para actualizar estado.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference refEstadoReserva = referenciaReservacionesBD.child(idReserva).child("status");
        refEstadoReserva.setValue(nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Estado de la reserva actualizado a: " + nuevoEstado.replace("_"," "), Toast.LENGTH_SHORT).show();
                    // La lista se actualizará automáticamente por el ValueEventListener en cargarReservasFiltradas()
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar el estado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(ETIQUETA_DEBUG, "Error actualizando estado en Firebase: ", e);
                });
    }
}