package com.example.remyscake.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.adapters.ReservasAdapter;
import com.example.remyscake.models.Reserva;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VerReservasActivity extends AppCompatActivity implements ReservasAdapter.OnReservaActionsListener {

    private static final String ETIQUETA_DEBUG = "VerReservasActivity";

    // Vistas UI
    private ImageButton btnBackReservas, btnCalendarView, btnFilterReservas;
    private TextView tvTotalReservas, tvReservasPendientes, tvReservasEnProceso, tvReservasCompletadas;
    private TextView tvCountUrgentes, tvCountHoy;
    private CardView cvUrgentes, cvParaHoy;
    private ChipGroup chipGroupEstados;
    private Spinner spinnerOrdenarReservas;
    private RecyclerView rvReservas;
    private FloatingActionButton fabCalendarView;
    private TextView tvMensajeCentral;

    // Firebase
    private DatabaseReference referenciaReservacionesBD;
    private ValueEventListener listenerReservaciones;

    // Listas y Adaptador
    private ReservasAdapter reservasAdapter;
    private List<Reserva> listaTodasLasReservas = new ArrayList<>();
    private List<Reserva> listaReservasFiltradas = new ArrayList<>();

    private String filtroEstadoActual = "Todas";
    private String ordenActual = "Más recientes primero";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_reservas);

        referenciaReservacionesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);

        inicializarVistas();
        configurarSpinnerOrden();
        configurarRecyclerView();
        establecerListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosReservas();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (referenciaReservacionesBD != null && listenerReservaciones != null) {
            referenciaReservacionesBD.removeEventListener(listenerReservaciones);
        }
    }

    private void inicializarVistas() {
        btnBackReservas = findViewById(R.id.btnBackReservas);
        btnCalendarView = findViewById(R.id.btnCalendarView);
        btnFilterReservas = findViewById(R.id.btnFilterReservas);

        tvTotalReservas = findViewById(R.id.tvTotalReservas);
        tvReservasPendientes = findViewById(R.id.tvReservasPendientes);
        tvReservasEnProceso = findViewById(R.id.tvReservasEnProceso);
        tvReservasCompletadas = findViewById(R.id.tvReservasCompletadas);

        cvUrgentes = findViewById(R.id.cvUrgentes);
        tvCountUrgentes = findViewById(R.id.tvCountUrgentes);
        cvParaHoy = findViewById(R.id.cvParaHoy);
        tvCountHoy = findViewById(R.id.tvCountHoy);

        chipGroupEstados = findViewById(R.id.chipGroupEstados);
        spinnerOrdenarReservas = findViewById(R.id.spinnerOrdenarReservas);
        rvReservas = findViewById(R.id.rvReservas);
        fabCalendarView = findViewById(R.id.fabCalendarView);

    }

    private void configurarSpinnerOrden() {
        String[] opcionesOrden = {"Más recientes primero", "Más antiguas primero", "Entrega (próxima)", "Entrega (lejana)"};
        ArrayAdapter<String> adapterOrden = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcionesOrden);
        adapterOrden.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrdenarReservas.setAdapter(adapterOrden);

        spinnerOrdenarReservas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ordenActual = parent.getItemAtPosition(position).toString();
                aplicarFiltrosYOrden();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void configurarRecyclerView() {
        rvReservas.setLayoutManager(new LinearLayoutManager(this));
        listaReservasFiltradas = new ArrayList<>();
        reservasAdapter = new ReservasAdapter(listaReservasFiltradas, this, this);
        rvReservas.setAdapter(reservasAdapter);
    }

    private void establecerListeners() {
        btnBackReservas.setOnClickListener(v -> onBackPressed());

        View.OnClickListener listenerVistaCalendario = v -> Toast.makeText(this, "Vista Calendario (Próximamente)", Toast.LENGTH_SHORT).show();
        btnCalendarView.setOnClickListener(listenerVistaCalendario);
        fabCalendarView.setOnClickListener(listenerVistaCalendario);

        btnFilterReservas.setOnClickListener(v -> Toast.makeText(this, "Filtros Avanzados (Próximamente)", Toast.LENGTH_SHORT).show());

        cvUrgentes.setOnClickListener(v -> {
            Toast.makeText(this, "Mostrar Reservas Urgentes", Toast.LENGTH_SHORT).show();
        });
        cvParaHoy.setOnClickListener(v -> {
            Toast.makeText(this, "Mostrar Reservas para Hoy", Toast.LENGTH_SHORT).show();
        });

        // Listener para los Chips de Estado
        chipGroupEstados.setOnCheckedChangeListener((group, checkedId) -> {

            Chip chip = group.findViewById(checkedId);
            if (chip != null && chip.isChecked()) {
                filtroEstadoActual = chip.getText().toString();
                if (chip.getId() != R.id.chipTodas) {
                    Chip chipTodas = findViewById(R.id.chipTodas);
                    if (chipTodas != null && chipTodas.isChecked()) chipTodas.setChecked(false);
                }
            } else if (checkedId == View.NO_ID || group.getCheckedChipIds().isEmpty()){
                filtroEstadoActual = "Todas";
                Chip chipTodas = findViewById(R.id.chipTodas);
                if (chipTodas != null) chipTodas.setChecked(true); // Esto puede causar un re-trigger del listener
            }
            aplicarFiltrosYOrden();
        });
        ((Chip)findViewById(R.id.chipTodas)).setChecked(true);
    }

    private void cargarDatosReservas() {
        if (tvMensajeCentral != null) tvMensajeCentral.setText("Cargando reservaciones...");
        if (tvMensajeCentral != null) tvMensajeCentral.setVisibility(View.VISIBLE);
        rvReservas.setVisibility(View.GONE);

        if (listenerReservaciones != null) {
            referenciaReservacionesBD.removeEventListener(listenerReservaciones);
        }

        // Ordenar por fecha de creación descendente (más recientes primero) como base
        Query queryReservas = referenciaReservacionesBD.orderByChild("createdAt");

        listenerReservaciones = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaTodasLasReservas.clear();
                long total = 0, pendientes = 0, enProceso = 0, completadas = 0, urgentes = 0, paraHoy = 0;
                Calendar hoyInicio = Calendar.getInstance();
                hoyInicio.set(Calendar.HOUR_OF_DAY, 0); hoyInicio.set(Calendar.MINUTE, 0); hoyInicio.set(Calendar.SECOND, 0);
                Calendar hoyFin = Calendar.getInstance();
                hoyFin.set(Calendar.HOUR_OF_DAY, 23); hoyFin.set(Calendar.MINUTE, 59); hoyFin.set(Calendar.SECOND, 59);
                long ahoraMillis = System.currentTimeMillis();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot reservaSnapshot : dataSnapshot.getChildren()) {
                        Reserva reserva = reservaSnapshot.getValue(Reserva.class);
                        if (reserva != null) {
                            reserva.setId(reservaSnapshot.getKey()); // Asignar el ID de Firebase
                            listaTodasLasReservas.add(reserva); // Añadir a la lista principal
                            total++;
                            String estado = reserva.getStatus();
                            if (estado != null) {
                                switch (estado.toLowerCase()) { // Usar toLowerCase para consistencia
                                    case "pendiente":
                                    case "confirmada":
                                        pendientes++;
                                        break;
                                    case "en_produccion":
                                    case "en produccion":
                                        enProceso++;
                                        break;
                                    case "lista_para_entrega":
                                    case "lista para entrega":
                                    case "entregada":
                                        completadas++; // Contar listos y entregados como completados para esta estadística
                                        break;
                                }
                            }
                            // Lógica para Urgentes y Para Hoy
                            long deliveryAt = reserva.getDeliveryAt();
                            if (deliveryAt > 0) {
                                if (deliveryAt >= hoyInicio.getTimeInMillis() && deliveryAt <= hoyFin.getTimeInMillis()) {
                                    paraHoy++;
                                }
                                long diffMillis = deliveryAt - ahoraMillis;
                                long diffHoras = TimeUnit.MILLISECONDS.toHours(diffMillis);
                                if (diffHoras >= 0 && diffHoras <= 24 && !(estado != null && (estado.equalsIgnoreCase("lista_para_entrega") || estado.equalsIgnoreCase("entregada") || estado.equalsIgnoreCase("cancelada")))) {
                                    urgentes++;
                                }
                            }
                        }
                    }
                    // Invertir la lista si la query por defecto es ascendente y queremos descendente
                    Collections.reverse(listaTodasLasReservas);
                }

                tvTotalReservas.setText(String.valueOf(total));
                tvReservasPendientes.setText(String.valueOf(pendientes));
                tvReservasEnProceso.setText(String.valueOf(enProceso));
                tvReservasCompletadas.setText(String.valueOf(completadas));
                tvCountUrgentes.setText(urgentes + (urgentes == 1 ? " pedido" : " pedidos"));
                tvCountHoy.setText(paraHoy + (paraHoy == 1 ? " entrega" : " entregas"));

                aplicarFiltrosYOrden(); // Aplicar filtros y ordenamiento inicial
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar reservaciones: ", databaseError.toException());
                Toast.makeText(VerReservasActivity.this, "Error al cargar datos.", Toast.LENGTH_LONG).show();
                if (tvMensajeCentral != null) {
                    tvMensajeCentral.setText("Error al cargar reservaciones.");
                    tvMensajeCentral.setVisibility(View.VISIBLE);
                }
                rvReservas.setVisibility(View.GONE);
            }
        };
        queryReservas.addValueEventListener(listenerReservaciones); // Usar addValueEventListener para actualizaciones
    }

    private void aplicarFiltrosYOrden() {
        listaReservasFiltradas.clear();
        List<Reserva> listaTemporal = new ArrayList<>();

        // 1. Filtrar por estado
        if (filtroEstadoActual.equalsIgnoreCase("Todas")) {
            listaTemporal.addAll(listaTodasLasReservas);
        } else {
            for (Reserva res : listaTodasLasReservas) {
                if (res.getStatus() != null && res.getStatus().equalsIgnoreCase(filtroEstadoActual)) {
                    listaTemporal.add(res);
                }
            }
        }

        // 2. Ordenar
        switch (ordenActual) {
            case "Más recientes primero": // Basado en createdAt
                Collections.sort(listaTemporal, (r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                break;
            case "Más antiguas primero": // Basado en createdAt
                Collections.sort(listaTemporal, Comparator.comparingLong(Reserva::getCreatedAt));
                break;
            case "Entrega (próxima)": // Basado en deliveryAt
                Collections.sort(listaTemporal, Comparator.comparingLong(Reserva::getDeliveryAt));
                break;
            case "Entrega (lejana)": // Basado en deliveryAt
                Collections.sort(listaTemporal, (r1, r2) -> Long.compare(r2.getDeliveryAt(), r1.getDeliveryAt()));
                break;
        }

        listaReservasFiltradas.addAll(listaTemporal);
        if (reservasAdapter != null) {
            reservasAdapter.actualizarLista(listaReservasFiltradas); // Usar el método del adaptador
        }

        // Actualizar UI si la lista filtrada está vacía
        if (listaReservasFiltradas.isEmpty()) {
            if (tvMensajeCentral != null) {
                tvMensajeCentral.setText("No hay reservaciones que coincidan con los filtros.");
                tvMensajeCentral.setVisibility(View.VISIBLE);
            }
            rvReservas.setVisibility(View.GONE);
        } else {
            if (tvMensajeCentral != null) tvMensajeCentral.setVisibility(View.GONE);
            rvReservas.setVisibility(View.VISIBLE);
        }
    }

    // Implementación de OnReservaActionsListener
    @Override
    public void onVerDetalleClick(Reserva reserva) {
        Toast.makeText(this, "Ver detalle de: " + reserva.getId(), Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, DetalleReservaActivity.class);
        // intent.putExtra("ID_RESERVA", reserva.getId());
        // startActivity(intent);
    }

    @Override
    public void onEditarClick(Reserva reserva) {
        Toast.makeText(this, "Editar reserva: " + reserva.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCambiarEstadoClick(Reserva reserva) {
        Toast.makeText(this, "Cambiar estado de: " + reserva.getId(), Toast.LENGTH_SHORT).show();
    }

    // private void actualizarEstadoReservaEnFirebase(String idReserva, String nuevoEstado) {
    //    DatabaseReference refReserva = referenciaReservacionesBD.child(idReserva).child("status");
    //    refReserva.setValue(nuevoEstado)
    //            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Estado actualizado a: " + nuevoEstado, Toast.LENGTH_SHORT).show())
    //            .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar estado.", Toast.LENGTH_SHORT).show());
    // }
}