package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.models.Reserva;
import com.example.remyscake.models.Usuario;
import com.example.remyscake.utils.ConstantesApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProductionDashboardActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "ProductionDashboard";

    // Vistas UI
    private TextView tvWelcomeProduction, tvKitchenStatus;
    private TextView tvPendingCount, tvInProductionCount, tvReadyCount;
    private TextView tvPendingBadge, tvActiveBadge, tvReadyBadge;
    private CardView cvReservasPendientes, cvReservasEnProduccion, cvListosParaEntrega;
    private ImageButton btnRefreshProduction, btnLogoutProduction;

    // Firebase
    private FirebaseAuth autenticacionFirebase;
    private DatabaseReference referenciaUsuarioActualBD;
    private DatabaseReference referenciaReservacionesBD;
    private ValueEventListener listenerEstadisticas; // Listener para las estadísticas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_dashboard); // Tu layout XML

        autenticacionFirebase = FirebaseAuth.getInstance();
        referenciaReservacionesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);

        inicializarVistas();
        establecerListeners();
        // cargarDatosUsuario() y cargarEstadisticasReservaciones() se llaman en onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuario();
        cargarEstadisticasReservaciones();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remover listener para evitar fugas de memoria
        if (referenciaReservacionesBD != null && listenerEstadisticas != null) {
            referenciaReservacionesBD.removeEventListener(listenerEstadisticas);
        }
        if (referenciaUsuarioActualBD != null) { // Si el listener de usuario fuera continuo
            // referenciaUsuarioActualBD.removeEventListener(listenerUsuario);
        }
    }

    private void inicializarVistas() {
        tvWelcomeProduction = findViewById(R.id.tvWelcomeProduction);
        tvKitchenStatus = findViewById(R.id.tvKitchenStatus);

        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvInProductionCount = findViewById(R.id.tvInProductionCount);
        tvReadyCount = findViewById(R.id.tvReadyCount);

        tvPendingBadge = findViewById(R.id.tvPendingBadge);
        tvActiveBadge = findViewById(R.id.tvActiveBadge);
        tvReadyBadge = findViewById(R.id.tvReadyBadge);

        cvReservasPendientes = findViewById(R.id.cvReservasPendientes);
        cvReservasEnProduccion = findViewById(R.id.cvReservasEnProduccion);
        cvListosParaEntrega = findViewById(R.id.cvListosParaEntrega);

        btnRefreshProduction = findViewById(R.id.btnRefreshProduction);
        btnLogoutProduction = findViewById(R.id.btnLogoutProduction);
    }

    private void cargarDatosUsuario() {
        FirebaseUser usuarioFirebase = autenticacionFirebase.getCurrentUser();
        if (usuarioFirebase != null) {
            String uid = usuarioFirebase.getUid();
            referenciaUsuarioActualBD = FirebaseDatabase.getInstance()
                    .getReference(ConstantesApp.NODO_USUARIOS)
                    .child(uid);

            // Usar addListenerForSingleValueEvent si solo necesitas cargarlo una vez al entrar
            referenciaUsuarioActualBD.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        if (usuario != null && usuario.getNombreCompleto() != null) {
                            // Mostrar solo el primer nombre para brevedad si es largo
                            String[] nombres = usuario.getNombreCompleto().split(" ");
                            tvWelcomeProduction.setText("Bienvenido, " + nombres[0]);
                        } else {
                            tvWelcomeProduction.setText("Bienvenido, Chef");
                        }
                    } else {
                        tvWelcomeProduction.setText("Bienvenido, Chef");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(ETIQUETA_DEBUG, "Error al cargar datos del usuario: ", databaseError.toException());
                    tvWelcomeProduction.setText("Bienvenido, Chef");
                }
            });
        } else {
            tvWelcomeProduction.setText("Bienvenido, Chef"); // Fallback
        }
    }

    private void cargarEstadisticasReservaciones() {
        if (listenerEstadisticas != null) { // Remover listener anterior si existe
            referenciaReservacionesBD.removeEventListener(listenerEstadisticas);
        }
        listenerEstadisticas = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long pendientes = 0;
                long enProduccion = 0;
                long listos = 0;
                long totalActivasCocina = 0;

                if (dataSnapshot.exists()){
                    for (DataSnapshot reservaSnapshot : dataSnapshot.getChildren()) {
                        Reserva reserva = reservaSnapshot.getValue(Reserva.class);
                        if (reserva != null && reserva.getStatus() != null) {
                            String estado = reserva.getStatus().toLowerCase(); // Normalizar a minúsculas
                            switch (estado) {
                                case "confirmada":
                                case "pendiente":
                                    pendientes++;
                                    totalActivasCocina++;
                                    break;
                                case "en_produccion":
                                case "en produccion":
                                    enProduccion++;
                                    totalActivasCocina++;
                                    break;
                                case "lista_para_entrega":
                                case "lista para entrega":
                                    listos++;
                                    // No contamos 'listos' como activos para la cocina, ya están terminados.
                                    break;
                            }
                        }
                    }
                }

                tvPendingCount.setText(String.valueOf(pendientes));
                tvInProductionCount.setText(String.valueOf(enProduccion));
                tvReadyCount.setText(String.valueOf(listos));

                tvPendingBadge.setText(String.valueOf(pendientes));
                tvActiveBadge.setText(String.valueOf(enProduccion));
                tvReadyBadge.setText(String.valueOf(listos));

                if (totalActivasCocina == 1) {
                    tvKitchenStatus.setText(totalActivasCocina + " pedido activo");
                } else {
                    tvKitchenStatus.setText(totalActivasCocina + " pedidos activos");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar estadísticas de reservaciones: ", databaseError.toException());
                tvKitchenStatus.setText("Error al cargar");
                tvPendingCount.setText("-"); tvInProductionCount.setText("-"); tvReadyCount.setText("-");
                tvPendingBadge.setText("0"); tvActiveBadge.setText("0"); tvReadyBadge.setText("0");
            }
        };
        referenciaReservacionesBD.addValueEventListener(listenerEstadisticas);
    }


    private void establecerListeners() {
        btnLogoutProduction.setOnClickListener(v -> cerrarSesion());

        btnRefreshProduction.setOnClickListener(v -> {
            Toast.makeText(this, "Actualizando datos...", Toast.LENGTH_SHORT).show();
            cargarEstadisticasReservaciones(); // Vuelve a cargar las estadísticas
        });

        // Listener para las CardViews que llevan a la lista de reservas
        View.OnClickListener listenerVerListaReservas = view -> {
            String estadoFiltro = "";
            int id = view.getId();
            if (id == R.id.cvReservasPendientes) {
                estadoFiltro = "pendiente";
            } else if (id == R.id.cvReservasEnProduccion) {
                estadoFiltro = "en_produccion";
            } else if (id == R.id.cvListosParaEntrega) {
                estadoFiltro = "lista_para_entrega";
            }

            if (!estadoFiltro.isEmpty()) {
                Intent intent = new Intent(ProductionDashboardActivity.this, ListaReservasProductionActivity.class); // Nueva Actividad
                intent.putExtra("ESTADO_FILTRO_PRODUCCION", estadoFiltro);
                startActivity(intent);
            }
        };

        cvReservasPendientes.setOnClickListener(listenerVerListaReservas);
        cvReservasEnProduccion.setOnClickListener(listenerVerListaReservas);
        cvListosParaEntrega.setOnClickListener(listenerVerListaReservas);
    }

    private void cerrarSesion() {
        autenticacionFirebase.signOut();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProductionDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}