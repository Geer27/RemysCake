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

public class SellerDashboardActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "SellerDashboard";

    private TextView tvWelcomeSeller, tvMyReservationsCount, tvMyClientsCount;
    private CardView cvCrearReserva, cvMisReservas, cvGestionarClientes;
    private ImageButton btnNotificationsSeller, btnLogoutSeller;

    private FirebaseAuth autenticacionFirebase;
    private DatabaseReference referenciaUsuarioActualBD;
    private DatabaseReference referenciaReservacionesBD;
    private DatabaseReference referenciaClientesBD;
    private FirebaseUser usuarioFirebaseActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate de haber corregido el XML antes de inflarlo
        setContentView(R.layout.activity_seller_dashboard);

        autenticacionFirebase = FirebaseAuth.getInstance();
        usuarioFirebaseActual = autenticacionFirebase.getCurrentUser();

        referenciaReservacionesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);
        referenciaClientesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CLIENTES);

        inicializarVistas();
        cargarDatosUsuario();
        cargarEstadisticasVendedor();
        establecerListeners();
    }

    private void inicializarVistas() {
        tvWelcomeSeller = findViewById(R.id.tvWelcomeSeller);
        tvMyReservationsCount = findViewById(R.id.tvMyReservationsCount);
        tvMyClientsCount = findViewById(R.id.tvMyClientsCount);

        cvCrearReserva = findViewById(R.id.cvCrearReserva);
        cvMisReservas = findViewById(R.id.cvMisReservas);
        cvGestionarClientes = findViewById(R.id.cvGestionarClientes);

        // btnNotificationsSeller = findViewById(R.id.btnNotificationsSeller); // Ya no está en el layout
        btnLogoutSeller = findViewById(R.id.btnLogoutSeller);
    }

    private void cargarDatosUsuario() {
        if (usuarioFirebaseActual != null) {
            String uid = usuarioFirebaseActual.getUid();
            referenciaUsuarioActualBD = FirebaseDatabase.getInstance()
                    .getReference(ConstantesApp.NODO_USUARIOS)
                    .child(uid);

            referenciaUsuarioActualBD.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            tvWelcomeSeller.setText("Bienvenido, " + usuario.getNombreCompleto());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(ETIQUETA_DEBUG, "Error al cargar datos del usuario: ", databaseError.toException());
                    tvWelcomeSeller.setText("Bienvenido, Vendedor");
                }
            });
        } else {
            tvWelcomeSeller.setText("Bienvenido, Vendedor");
        }
    }

    private void cargarEstadisticasVendedor() {
        if (usuarioFirebaseActual == null) return;

        // Contar mis reservaciones (donde createdBy es mi UID)
        Query misReservasQuery = referenciaReservacionesBD.orderByChild("createdBy").equalTo(usuarioFirebaseActual.getUid());
        misReservasQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Podrías añadir filtros aquí, por ejemplo, no contar "cancelada" o "entregada" si quieres
                    // String status = snapshot.child("status").getValue(String.class);
                    // if (status != null && !(status.equals("entregada") || status.equals("cancelada"))) {
                    //    count++;
                    // }
                    count++; // Por ahora cuenta todas las creadas por el vendedor
                }
                tvMyReservationsCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al contar mis reservaciones: ", databaseError.toException());
                tvMyReservationsCount.setText("N/A");
            }
        });

        // Contar mis clientes (donde creado_por es mi UID)
        // NOTA: La estructura del nodo "clientes" debe tener un campo "creado_por" con el UID del vendedor.
        // Si no es así, esta consulta no funcionará como se espera.
        // Si los clientes no están directamente ligados a un vendedor que los creó,
        // podrías necesitar otra lógica o simplemente mostrar "N/A" o un conteo total de clientes si el admin lo permite.
        Query misClientesQuery = referenciaClientesBD.orderByChild("creado_por").equalTo(usuarioFirebaseActual.getUid());
        misClientesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tvMyClientsCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al contar mis clientes: ", databaseError.toException());
                tvMyClientsCount.setText("N/A");
            }
        });
    }


    private void establecerListeners() {
        btnLogoutSeller.setOnClickListener(v -> cerrarSesion());

        //btnNotificationsSeller.setOnClickListener(v -> {
        //    Toast.makeText(this, "Próximamente: Notificaciones del Vendedor", Toast.LENGTH_SHORT).show();
        //});

        cvCrearReserva.setOnClickListener(v -> {
            Toast.makeText(this, "Ir a Crear Nueva Reserva", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(SellerDashboardActivity.this, CrearReservaActivity.class);
            // startActivity(intent);
        });

        cvMisReservas.setOnClickListener(v -> {
            Toast.makeText(this, "Ir a Mis Reservas", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(SellerDashboardActivity.this, MisReservasActivity.class);
            // startActivity(intent);
        });

        cvGestionarClientes.setOnClickListener(v -> {
            Toast.makeText(this, "Ir a Gestionar Mis Clientes", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(SellerDashboardActivity.this, GestionClientesActivity.class);
            // intent.putExtra("FILTRO_VENDEDOR_UID", usuarioFirebaseActual.getUid()); // Para filtrar solo sus clientes
            // startActivity(intent);
        });
    }

    private void cerrarSesion() {
        autenticacionFirebase.signOut();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SellerDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}