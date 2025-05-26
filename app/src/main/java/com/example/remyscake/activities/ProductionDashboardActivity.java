package com.example.remyscake.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.models.Usuario;
import com.example.remyscake.utils.ConstantesApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProductionDashboardActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "ProductionDashboard";

    private TextView tvWelcomeProduction, tvKitchenStatus;
    private TextView tvPendingCount, tvInProductionCount, tvReadyCount;
    private TextView tvPendingBadge, tvActiveBadge, tvReadyBadge;
    private CardView cvReservasPendientes, cvReservasEnProduccion, cvListosParaEntrega;
    private ImageButton btnRefreshProduction, btnLogoutProduction;

    private FirebaseAuth autenticacionFirebase;
    private DatabaseReference referenciaUsuarioActualBD;
    private DatabaseReference referenciaReservacionesBD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate de haber corregido el XML antes de inflarlo
        setContentView(R.layout.activity_production_dashboard);

        autenticacionFirebase = FirebaseAuth.getInstance();
        referenciaReservacionesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);

        inicializarVistas();
        cargarDatosUsuario();
        cargarEstadisticasReservaciones(); // Cargar todas las estadísticas de reservaciones
        establecerListeners();
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

            referenciaUsuarioActualBD.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            tvWelcomeProduction.setText("Bienvenido, " + usuario.getNombreCompleto());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(ETIQUETA_DEBUG, "Error al cargar datos del usuario: ", databaseError.toException());
                    tvWelcomeProduction.setText("Bienvenido, Chef");
                }
            });
        } else {
            tvWelcomeProduction.setText("Bienvenido, Chef");
        }
    }

    private void cargarEstadisticasReservaciones() {
        // Listener para contar reservaciones por estado
        referenciaReservacionesBD.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long pendientes = 0;
                long enProduccion = 0;
                long listos = 0;
                long totalActivas = 0; // Pendientes + En Producción

                for (DataSnapshot reservaSnapshot : dataSnapshot.getChildren()) {
                    // Asumiendo que cada reservación tiene un campo "status"
                    String estado = reservaSnapshot.child("status").getValue(String.class);
                    if (estado != null) {
                        switch (estado) {
                            case "confirmada": // O "pendiente" si ese es el estado inicial para producción
                            case "pendiente":
                                pendientes++;
                                totalActivas++;
                                break;
                            case "en_produccion":
                                enProduccion++;
                                totalActivas++;
                                break;
                            case "lista_para_entrega":
                                listos++;
                                break;
                            // No contamos "entregada" o "cancelada" en estas estadísticas activas
                        }
                    }
                }

                tvPendingCount.setText(String.valueOf(pendientes));
                tvInProductionCount.setText(String.valueOf(enProduccion));
                tvReadyCount.setText(String.valueOf(listos));

                tvPendingBadge.setText(String.valueOf(pendientes));
                tvActiveBadge.setText(String.valueOf(enProduccion));
                tvReadyBadge.setText(String.valueOf(listos));

                if (totalActivas == 1) {
                    tvKitchenStatus.setText(totalActivas + " pedido activo");
                } else {
                    tvKitchenStatus.setText(totalActivas + " pedidos activos");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar estadísticas de reservaciones: ", databaseError.toException());
                tvKitchenStatus.setText("Error al cargar estado");
                tvPendingCount.setText("N/A");
                tvInProductionCount.setText("N/A");
                tvReadyCount.setText("N/A");
                tvPendingBadge.setText("0");
                tvActiveBadge.setText("0");
                tvReadyBadge.setText("0");
            }
        });
    }


    private void establecerListeners() {
        btnLogoutProduction.setOnClickListener(v -> cerrarSesion());

        btnRefreshProduction.setOnClickListener(v -> {
            Toast.makeText(this, "Actualizando datos...", Toast.LENGTH_SHORT).show();
            cargarEstadisticasReservaciones(); // Vuelve a cargar las estadísticas
        });

        cvReservasPendientes.setOnClickListener(v -> {
            Toast.makeText(this, "Ver Reservas Pendientes", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(ProductionDashboardActivity.this, ListaReservasActivity.class);
            // intent.putExtra("ESTADO_FILTRO", "pendiente"); // O "confirmada"
            // startActivity(intent);
        });

        cvReservasEnProduccion.setOnClickListener(v -> {
            Toast.makeText(this, "Ver Reservas en Producción", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(ProductionDashboardActivity.this, ListaReservasActivity.class);
            // intent.putExtra("ESTADO_FILTRO", "en_produccion");
            // startActivity(intent);
        });

        cvListosParaEntrega.setOnClickListener(v -> {
            Toast.makeText(this, "Ver Reservas Listas para Entrega", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(ProductionDashboardActivity.this, ListaReservasActivity.class);
            // intent.putExtra("ESTADO_FILTRO", "lista_para_entrega");
            // startActivity(intent);
        });
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