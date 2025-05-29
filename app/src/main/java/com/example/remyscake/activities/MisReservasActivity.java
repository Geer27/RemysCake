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
import android.widget.LinearLayout; // Para el LinearLayout de estado vacío
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.adapters.ReservasAdapter;
import com.example.remyscake.models.Reserva;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MisReservasActivity extends AppCompatActivity implements ReservasAdapter.OnReservaActionsListener {

    private static final String ETIQUETA_DEBUG = "MisReservasActivity";

    // Vistas UI
    private ImageButton btnBackMisReservas, btnFiltrarReservas, btnBuscarReservas;
    private TextView tvSubtituloMisReservas, tvTotalMisReservas, tvReservasPendientes, tvVentasEsteMes;
    private TextView tvCountPendientesRapido, tvCountProcesoRapido, tvCountListosRapido;
    private CardView cvReservasPendientesRapido, cvReservasEnProcesoRapido, cvReservasListasRapido;
    private ChipGroup chipGroupEstadosMisReservas;
    private Spinner spinnerOrdenarMisReservas;
    private RecyclerView rvMisReservas;
    private LinearLayout llEstadoVacio;
    private MaterialButton btnCrearPrimeraReserva;
    private FloatingActionButton fabNuevaReserva;

    // Firebase
    private FirebaseAuth autenticacionFirebase;
    private FirebaseUser usuarioFirebaseActual;
    private DatabaseReference referenciaReservacionesBD;
    private ValueEventListener listenerMisReservas;

    // Listas y Adaptador
    private ReservasAdapter reservasAdapter;
    private List<Reserva> listaTodasMisReservas = new ArrayList<>();
    private List<Reserva> listaReservasFiltradas = new ArrayList<>();

    private String filtroEstadoActual = "Todas";
    private String ordenActual = "Más recientes primero";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_reservas);

        autenticacionFirebase = FirebaseAuth.getInstance();
        usuarioFirebaseActual = autenticacionFirebase.getCurrentUser();

        if (usuarioFirebaseActual == null) {
            Toast.makeText(this, "Error de sesión. Por favor, inicie sesión de nuevo.", Toast.LENGTH_LONG).show();
            // irALogin();
            finish(); // Cierra esta actividad si no hay usuario
            return;
        }

        referenciaReservacionesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);

        inicializarVistas();
        configurarSpinnerOrden();
        configurarRecyclerView();
        establecerListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMisReservas();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (listenerMisReservas != null && usuarioFirebaseActual != null) {
            // Remover el listener de la query específica
            Query query = referenciaReservacionesBD.orderByChild("createdBy").equalTo(usuarioFirebaseActual.getUid());
            query.removeEventListener(listenerMisReservas);
        }
    }

    private void inicializarVistas() {
        btnBackMisReservas = findViewById(R.id.btnBackMisReservas);
        tvSubtituloMisReservas = findViewById(R.id.tvSubtituloMisReservas);
        btnFiltrarReservas = findViewById(R.id.btnFiltrarReservas);
        btnBuscarReservas = findViewById(R.id.btnBuscarReservas);

        tvTotalMisReservas = findViewById(R.id.tvTotalMisReservas);
        tvReservasPendientes = findViewById(R.id.tvReservasPendientes); // Estadísticas generales
        tvVentasEsteMes = findViewById(R.id.tvVentasEsteMes);

        cvReservasPendientesRapido = findViewById(R.id.cvReservasPendientesRapido);
        tvCountPendientesRapido = findViewById(R.id.tvCountPendientesRapido);
        cvReservasEnProcesoRapido = findViewById(R.id.cvReservasEnProcesoRapido);
        tvCountProcesoRapido = findViewById(R.id.tvCountProcesoRapido);
        cvReservasListasRapido = findViewById(R.id.cvReservasListasRapido);
        tvCountListosRapido = findViewById(R.id.tvCountListosRapido);

        chipGroupEstadosMisReservas = findViewById(R.id.chipGroupEstadosMisReservas);
        spinnerOrdenarMisReservas = findViewById(R.id.spinnerOrdenarMisReservas);
        rvMisReservas = findViewById(R.id.rvMisReservas);

        llEstadoVacio = findViewById(R.id.llEstadoVacio);
        btnCrearPrimeraReserva = findViewById(R.id.btnCrearPrimeraReserva);
        fabNuevaReserva = findViewById(R.id.fabNuevaReserva);

        // Valores iniciales para estadísticas
        tvTotalMisReservas.setText("0");
        tvReservasPendientes.setText("0");
        tvVentasEsteMes.setText("$0.00");
        tvCountPendientesRapido.setText("0");
        tvCountProcesoRapido.setText("0");
        tvCountListosRapido.setText("0");
    }

    private void configurarSpinnerOrden() {
        String[] opcionesOrden = {"Más recientes primero", "Más antiguas primero", "Entrega (próxima)", "Entrega (lejana)"};
        ArrayAdapter<String> adapterOrden = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcionesOrden);
        adapterOrden.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrdenarMisReservas.setAdapter(adapterOrden);

        spinnerOrdenarMisReservas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        rvMisReservas.setLayoutManager(new LinearLayoutManager(this));
        listaReservasFiltradas = new ArrayList<>(); // Iniciar lista
        reservasAdapter = new ReservasAdapter(listaReservasFiltradas, this, this);
        rvMisReservas.setAdapter(reservasAdapter);
    }

    private void establecerListeners() {
        btnBackMisReservas.setOnClickListener(v -> onBackPressed());
        btnFiltrarReservas.setOnClickListener(v -> Toast.makeText(this, "Filtros avanzados (Próximamente)", Toast.LENGTH_SHORT).show());
        btnBuscarReservas.setOnClickListener(v -> Toast.makeText(this, "Buscar en mis reservas (Próximamente)", Toast.LENGTH_SHORT).show());

        View.OnClickListener listenerCrearReserva = v -> {
            Intent intent = new Intent(MisReservasActivity.this, CrearReservaActivity.class);
            startActivity(intent);
        };
        fabNuevaReserva.setOnClickListener(listenerCrearReserva);
        btnCrearPrimeraReserva.setOnClickListener(listenerCrearReserva);

        // Listeners para accesos rápidos
        cvReservasPendientesRapido.setOnClickListener(v -> seleccionarChipPorTexto("Pendientes"));
        cvReservasEnProcesoRapido.setOnClickListener(v -> seleccionarChipPorTexto("En Proceso"));
        cvReservasListasRapido.setOnClickListener(v -> seleccionarChipPorTexto("Listos"));


        chipGroupEstadosMisReservas.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null && chip.isChecked()) { // Asegurarse que el chip es el que se marcó
                filtroEstadoActual = chip.getText().toString();
            } else if (group.getCheckedChipId() == View.NO_ID){ // Si nada está seleccionado
                filtroEstadoActual = "Todas"; // Volver a "Todas"
                Chip chipTodas = findViewById(R.id.chipTodasMisReservas);
                if(chipTodas != null) chipTodas.setChecked(true);
            }
            aplicarFiltrosYOrden();
        });
        // Estado inicial del ChipGroup
        ((Chip)findViewById(R.id.chipTodasMisReservas)).setChecked(true);
    }

    private void seleccionarChipPorTexto(String textoChip) {
        for (int i = 0; i < chipGroupEstadosMisReservas.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupEstadosMisReservas.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(textoChip)) {
                // chipGroupEstadosMisReservas.check(chip.getId()); // Esto dispara el listener
                chip.setChecked(true); // Esto también debería disparar el listener si está configurado correctamente
                return;
            }
        }
    }


    private void cargarMisReservas() {
        llEstadoVacio.setVisibility(View.GONE); // Ocultar mientras carga
        // Podrías añadir un ProgressBar aquí

        if (listenerMisReservas != null && usuarioFirebaseActual != null) {
            referenciaReservacionesBD.orderByChild("createdBy").equalTo(usuarioFirebaseActual.getUid()).removeEventListener(listenerMisReservas);
        }

        Query queryMisReservas = referenciaReservacionesBD.orderByChild("createdBy").equalTo(usuarioFirebaseActual.getUid());

        listenerMisReservas = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaTodasMisReservas.clear();
                long countTotal = 0;
                long countPendientes = 0;
                double ventasMesActual = 0;

                long countPendientesRapido = 0;
                long countProcesoRapido = 0;
                long countListosRapido = 0;

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                long inicioMesMillis = cal.getTimeInMillis();


                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Reserva reserva = snapshot.getValue(Reserva.class);
                        if (reserva != null) {
                            reserva.setId(snapshot.getKey());
                            listaTodasMisReservas.add(reserva);
                            countTotal++;

                            String status = reserva.getStatus() != null ? reserva.getStatus().toLowerCase() : "";
                            if (status.equals("pendiente") || status.equals("confirmada")) {
                                countPendientes++;
                                countPendientesRapido++;
                            } else if (status.equals("en_produccion") || status.equals("en produccion")) {
                                countProcesoRapido++;
                            } else if (status.equals("lista_para_entrega") || status.equals("lista para entrega")) {
                                countListosRapido++;
                            }

                            if (reserva.getPayment() != null && "completado".equalsIgnoreCase(reserva.getPayment().getStatusPago()) &&
                                    reserva.getCreatedAt() >= inicioMesMillis) {
                                ventasMesActual += reserva.getPayment().getAmount();
                            }
                        }
                    }
                }

                tvTotalMisReservas.setText(String.valueOf(countTotal));
                tvReservasPendientes.setText(String.valueOf(countPendientes));
                NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "SV"));
                tvVentasEsteMes.setText(formatoMoneda.format(ventasMesActual));

                tvCountPendientesRapido.setText(String.valueOf(countPendientesRapido));
                tvCountProcesoRapido.setText(String.valueOf(countProcesoRapido));
                tvCountListosRapido.setText(String.valueOf(countListosRapido));

                aplicarFiltrosYOrden(); // Aplicar filtros y ordenamiento
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar mis reservas: ", databaseError.toException());
                Toast.makeText(MisReservasActivity.this, "Error al cargar tus reservas.", Toast.LENGTH_SHORT).show();
                llEstadoVacio.setVisibility(View.VISIBLE);
                rvMisReservas.setVisibility(View.GONE);
                ((TextView) llEstadoVacio.findViewById(R.id.tvEstadoVacioTitulo)).setText("Error al cargar");
                ((TextView) llEstadoVacio.findViewById(R.id.tvEstadoVacioMensaje)).setText("No se pudieron cargar tus reservas.");
            }
        };
        queryMisReservas.addValueEventListener(listenerMisReservas);
    }

    private void aplicarFiltrosYOrden() {
        listaReservasFiltradas.clear();
        List<Reserva> listaTemporal = new ArrayList<>();

        if (filtroEstadoActual.equalsIgnoreCase("Todas")) {
            listaTemporal.addAll(listaTodasMisReservas);
        } else {
            for (Reserva res : listaTodasMisReservas) {
                if (res.getStatus() != null && res.getStatus().equalsIgnoreCase(filtroEstadoActual)) {
                    listaTemporal.add(res);
                }
            }
        }

        switch (ordenActual) {
            case "Más recientes primero":
                Collections.sort(listaTemporal, (r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                break;
            case "Más antiguas primero":
                Collections.sort(listaTemporal, Comparator.comparingLong(Reserva::getCreatedAt));
                break;
            case "Entrega (próxima)":
                Collections.sort(listaTemporal, Comparator.comparingLong(Reserva::getDeliveryAt));
                break;
            case "Entrega (lejana)":
                Collections.sort(listaTemporal, (r1, r2) -> Long.compare(r2.getDeliveryAt(), r1.getDeliveryAt()));
                break;
        }

        listaReservasFiltradas.addAll(listaTemporal);
        if (reservasAdapter != null) {
            reservasAdapter.actualizarLista(listaReservasFiltradas);
        }

        if (listaReservasFiltradas.isEmpty()) {
            llEstadoVacio.setVisibility(View.VISIBLE);
            // Personalizar mensaje del estado vacío
            TextView tvTituloVacio = llEstadoVacio.findViewById(R.id.tvEstadoVacioTitulo);
            TextView tvMensajeVacio = llEstadoVacio.findViewById(R.id.tvEstadoVacioMensaje);
            if (filtroEstadoActual.equalsIgnoreCase("Todas")) {
                tvTituloVacio.setText("No tienes reservas aún");
                tvMensajeVacio.setText("Crea tu primera reserva para comenzar a vender.");
            } else {
                tvTituloVacio.setText("Sin resultados");
                tvMensajeVacio.setText("No hay reservas que coincidan con el estado '" + filtroEstadoActual + "'.");
            }
            rvMisReservas.setVisibility(View.GONE);
        } else {
            llEstadoVacio.setVisibility(View.GONE);
            rvMisReservas.setVisibility(View.VISIBLE);
        }
    }


    // Implementación de OnReservaActionsListener del ReservasAdapter
    @Override
    public void onVerDetalleClick(Reserva reserva) {
        Toast.makeText(this, "Ver detalle de mi reserva: " + reserva.getId().substring(Math.max(0, reserva.getId().length() - 6)), Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, DetalleReservaVendedorActivity.class); // Podría ser una vista diferente
        // intent.putExtra("ID_RESERVA", reserva.getId());
        // startActivity(intent);
    }

    @Override
    public void onEditarClick(Reserva reserva) {
        // El vendedor solo debería poder editar bajo ciertas condiciones
        if ("pendiente".equalsIgnoreCase(reserva.getStatus()) || "confirmada".equalsIgnoreCase(reserva.getStatus())) {
            Intent intent = new Intent(this, CrearReservaActivity.class); // Reutilizar para editar
            intent.putExtra("ID_RESERVA_EDITAR", reserva.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Esta reserva ya no se puede editar.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCambiarEstadoClick(Reserva reserva) {
        Toast.makeText(this, "Acciones para mi reserva: " + reserva.getId().substring(Math.max(0, reserva.getId().length() - 6)), Toast.LENGTH_SHORT).show();
    }

}