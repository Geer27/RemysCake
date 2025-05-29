package com.example.remyscake.activities;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout; // Para el saludo personalizado, si lo referenciamos
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

public class SellerDashboardActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "SellerDashboard";

    // Vistas UI
    private TextView tvWelcomeSeller, tvMyReservationsCount, tvMyClientsCount;
    private CardView cvCrearReserva, cvMisReservas, cvGestionarClientes;
    private ImageButton btnNotificationsSeller, btnLogoutSeller;
    // LinearLayout llSaludoPersonalizado;

    // Firebase
    private FirebaseAuth autenticacionFirebase;
    private FirebaseUser usuarioFirebaseActual;
    private DatabaseReference referenciaUsuarioActualBD;
    private DatabaseReference referenciaReservacionesBD;
    private DatabaseReference referenciaClientesBD;

    // Listeners de Firebase para removerlos en onPause
    private ValueEventListener listenerUsuarioActual;
    private ValueEventListener listenerMisReservas;
    private ValueEventListener listenerMisClientes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        autenticacionFirebase = FirebaseAuth.getInstance();
        usuarioFirebaseActual = autenticacionFirebase.getCurrentUser();

        if (usuarioFirebaseActual == null) {
            // Si por alguna razón no hay usuario, volver al login
            Toast.makeText(this, "Error: Sesión no válida.", Toast.LENGTH_LONG).show();
            irALogin();
            return;
        }

        referenciaReservacionesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);
        referenciaClientesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CLIENTES);
        referenciaUsuarioActualBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_USUARIOS).child(usuarioFirebaseActual.getUid());

        inicializarVistas();
        establecerListeners();
        // cargarDatosUsuario() y cargarEstadisticasVendedor() se llamarán en onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuario();
        cargarEstadisticasVendedor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remover listeners para evitar fugas de memoria y actualizaciones innecesarias
        if (referenciaUsuarioActualBD != null && listenerUsuarioActual != null) {
            referenciaUsuarioActualBD.removeEventListener(listenerUsuarioActual);
        }
        if (referenciaReservacionesBD != null && listenerMisReservas != null) {
        }
        if (referenciaClientesBD != null && listenerMisClientes != null) {
        }
    }


    private void inicializarVistas() {
        tvWelcomeSeller = findViewById(R.id.tvWelcomeSeller);
        tvMyReservationsCount = findViewById(R.id.tvMyReservationsCount);
        tvMyClientsCount = findViewById(R.id.tvMyClientsCount);

        cvCrearReserva = findViewById(R.id.cvCrearReserva);
        cvMisReservas = findViewById(R.id.cvMisReservas);
        cvGestionarClientes = findViewById(R.id.cvGestionarClientes);

        btnNotificationsSeller = findViewById(R.id.btnNotificationsSeller);
        btnLogoutSeller = findViewById(R.id.btnLogoutSeller);

        // llSaludoPersonalizado = findViewById(R.id.llSaludoPersonalizado); // Si tuvieras un ID para el LinearLayout del saludo
    }

    private void establecerListeners() {
        btnLogoutSeller.setOnClickListener(v -> cerrarSesion());

        btnNotificationsSeller.setOnClickListener(v -> {
            Toast.makeText(this, "Notificaciones (Próximamente)", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, NotificacionesActivity.class);
            startActivity(intent);
        });

        cvCrearReserva.setOnClickListener(v -> {
            Intent intent = new Intent(SellerDashboardActivity.this, CrearReservaActivity.class);
            startActivity(intent);
        });

        cvMisReservas.setOnClickListener(v -> {
            Toast.makeText(this, "Mis Reservas (Próximamente)", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SellerDashboardActivity.this, MisReservasActivity.class);
            startActivity(intent);
        });

        cvGestionarClientes.setOnClickListener(v -> {
            Toast.makeText(this, "Gestionar Clientes (Próximamente)", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SellerDashboardActivity.this, GestionarClientesSellerActivity.class);
            startActivity(intent);
        });
    }

    private void cargarDatosUsuario() {
        if (listenerUsuarioActual != null) {
            referenciaUsuarioActualBD.removeEventListener(listenerUsuarioActual);
        }
        listenerUsuarioActual = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    if (usuario != null && usuario.getNombreCompleto() != null) {
                        tvWelcomeSeller.setText("Bienvenido, " + usuario.getNombreCompleto().split(" ")[0]); // Solo el primer nombre
                    } else {
                        tvWelcomeSeller.setText("Bienvenido, Vendedor");
                    }
                } else {
                    tvWelcomeSeller.setText("Bienvenido, Vendedor");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar datos del usuario: ", databaseError.toException());
                tvWelcomeSeller.setText("Bienvenido, Vendedor");
            }
        };
        referenciaUsuarioActualBD.addValueEventListener(listenerUsuarioActual);
    }

    private void cargarEstadisticasVendedor() {
        if (usuarioFirebaseActual == null) return;

        // Remover listeners anteriores para evitar múltiples escuchas si onResume se llama varias veces
        if (listenerMisReservas != null) {
        }
        if (listenerMisClientes != null) {
            // Similar
        }


        // Contar mis reservaciones (donde createdBy es mi UID)
        Query misReservasQuery = referenciaReservacionesBD.orderByChild("createdBy").equalTo(usuarioFirebaseActual.getUid());
        listenerMisReservas = new ValueEventListener() { // Asignar a la variable de instancia
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long countActivas = 0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Solo contar reservaciones que no estén "entregada" o "cancelada"
                        String status = snapshot.child("status").getValue(String.class);
                        if (status != null && !(status.equalsIgnoreCase("entregada") || status.equalsIgnoreCase("cancelada"))) {
                            countActivas++;
                        }
                    }
                }
                tvMyReservationsCount.setText(String.valueOf(countActivas));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al contar mis reservaciones: ", databaseError.toException());
                tvMyReservationsCount.setText("0");
            }
        };
        misReservasQuery.addValueEventListener(listenerMisReservas);

        Query misClientesQuery = referenciaClientesBD.orderByChild("creado_por").equalTo(usuarioFirebaseActual.getUid());
        listenerMisClientes = new ValueEventListener() { // Asignar a la variable de instancia
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tvMyClientsCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al contar mis clientes: ", databaseError.toException());
                tvMyClientsCount.setText("0");
            }
        };
        misClientesQuery.addValueEventListener(listenerMisClientes);
    }

    private void cerrarSesion() {
        autenticacionFirebase.signOut();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        irALogin();
    }

    private void irALogin() {
        Intent intent = new Intent(SellerDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}