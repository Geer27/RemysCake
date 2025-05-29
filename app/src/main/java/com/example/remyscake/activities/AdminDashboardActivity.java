package com.example.remyscake.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.models.Usuario;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String ETIQUETA_DEBUG = "AdminDashboard";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenuAdmin, btnNotificationsAdmin, btnLogoutAdmin;
    private TextView tvWelcomeAdmin;
    private TextView tvTotalReservations, tvTotalUsers;
    private CardView cvGestionarCatalogo, cvGestionarUsuarios, cvGestionarClientes, cvVerReservas, cvGenerarReportes;

    private FirebaseAuth autenticacionFirebase;
    private DatabaseReference referenciaUsuarioActualBD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        autenticacionFirebase = FirebaseAuth.getInstance();

        // Linking todas las vistas del layout
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnMenuAdmin = findViewById(R.id.btnMenuAdmin);
        // el botón de notificaciones fue removido del diseño pero mantengo la referencia por si lo vuelven a pedir
        // btnNotificationsAdmin = findViewById(R.id.btnNotificationsAdmin);
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin); // ahora está en la toolbar
        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);

        tvTotalReservations = findViewById(R.id.tvTotalReservations);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);

        cvGestionarCatalogo = findViewById(R.id.cvGestionarCatalogo);
        cvGestionarUsuarios = findViewById(R.id.cvGestionarUsuarios);
        cvGestionarClientes = findViewById(R.id.cvGestionarClientes);
        cvVerReservas = findViewById(R.id.cvVerReservas);
        cvGenerarReportes = findViewById(R.id.cvGenerarReportes);


        configurarDrawer();
        configurarNavigationView();
        cargarDatosUsuario();
        cargarEstadisticasRapidas(); // carga el contador de reservas y usuarios en tiempo real
        establecerListeners();
    }

    private void configurarDrawer() {
        // usamos un ImageButton personalizado en lugar del ActionBarDrawerToggle default
        // esto nos da más control sobre el diseño
        btnMenuAdmin.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void configurarNavigationView() {
        navigationView.setNavigationItemSelectedListener(this);
        // inflamos el menú manualmente para tener la opción de cambiarlo dinámicamente más adelante
        navigationView.inflateMenu(R.menu.admin_drawer_menu);
    }

    private void cargarDatosUsuario() {
        FirebaseUser usuarioFirebase = autenticacionFirebase.getCurrentUser();
        if (usuarioFirebase != null) {
            String uid = usuarioFirebase.getUid();
            referenciaUsuarioActualBD = FirebaseDatabase.getInstance()
                    .getReference(ConstantesApp.NODO_USUARIOS)
                    .child(uid);

            // solo leemos una vez los datos del usuario para el saludo
            referenciaUsuarioActualBD.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            tvWelcomeAdmin.setText("Bienvenido, " + usuario.getNombreCompleto());
                            // TODO: actualizar el header del NavigationView cuando lo implementemos
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(ETIQUETA_DEBUG, "Error al cargar datos del usuario: ", databaseError.toException());
                    // fallback por si falla la carga
                    tvWelcomeAdmin.setText("Bienvenido, Administrador");
                }
            });
        } else {
            // no debería pasar pero por si acaso
            tvWelcomeAdmin.setText("Bienvenido, Administrador");
        }
    }

    private void cargarEstadisticasRapidas() {
        // contador de reservas activas
        DatabaseReference refReservas = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);
        refReservas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // getChildrenCount() me da el total de nodos hijos (cada reserva es un hijo)
                tvTotalReservations.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // si no puede cargar mostramos N/A
                tvTotalReservations.setText("N/A");
            }
        });

        // contador de usuarios registrados
        DatabaseReference refUsuarios = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_USUARIOS);
        refUsuarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTotalUsers.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalUsers.setText("N/A");
            }
        });
    }


    private void establecerListeners() {
        btnLogoutAdmin.setOnClickListener(v -> cerrarSesion());

        // las notificaciones quedan pendientes para una próxima versión
        // btnNotificationsAdmin.setOnClickListener(v -> {
        //    Toast.makeText(this, "Próximamente: Notificaciones", Toast.LENGTH_SHORT).show();
        // });

        cvGestionarCatalogo.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, GestionarCatalogoActivity.class);
            startActivity(intent);
        });

        cvGestionarUsuarios.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, GestionarUsuariosActivity.class);
            startActivity(intent);
        });

        cvGestionarClientes.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, GestionarClientesActivity.class);
            startActivity(intent);
        });

        cvVerReservas.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, VerReservasActivity.class);
            startActivity(intent);
        });

        cvGenerarReportes.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, GenerarReportesActivity.class);
            startActivity(intent);
        });
    }

    private void cerrarSesion() {
        autenticacionFirebase.signOut();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        // limpiamos el stack de activities para que no pueda volver atrás
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int idElemento = item.getItemId();

        // tuve que usar if/else porque el switch con R.id no compila bien en las nuevas versiones
        if (idElemento == R.id.nav_admin_home) {
            // ya estamos en el dashboard, podríamos refrescar los datos
            Toast.makeText(this, "Inicio (Dashboard)", Toast.LENGTH_SHORT).show();
        } else if (idElemento == R.id.nav_admin_catalogo) {
            Toast.makeText(this, "Desde Nav: Gestionar Catálogo", Toast.LENGTH_SHORT).show();
            // TODO: descomentar cuando esté lista la activity
            // Intent intent = new Intent(this, GestionCatalogoActivity.class);
            // startActivity(intent);
        } else if (idElemento == R.id.nav_admin_usuarios) {
            Toast.makeText(this, "Desde Nav: Gestionar Usuarios", Toast.LENGTH_SHORT).show();
        } else if (idElemento == R.id.nav_admin_clientes) {
            Toast.makeText(this, "Desde Nav: Gestionar Clientes", Toast.LENGTH_SHORT).show();
        } else if (idElemento == R.id.nav_admin_reservas) {
            Toast.makeText(this, "Desde Nav: Ver Reservas", Toast.LENGTH_SHORT).show();
        } else if (idElemento == R.id.nav_admin_reportes) {
            Toast.makeText(this, "Desde Nav: Generar Reportes", Toast.LENGTH_SHORT).show();
        } else if (idElemento == R.id.nav_admin_perfil) {
            Toast.makeText(this, "Desde Nav: Mi Perfil", Toast.LENGTH_SHORT).show();
            // pendiente implementar el perfil del admin
            // Intent intent = new Intent(this, AdminProfileActivity.class);
            // startActivity(intent);
        } else if (idElemento == R.id.nav_admin_logout) {
            cerrarSesion();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        // si el drawer está abierto, lo cerramos primero
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // si no, dejamos el comportamiento normal (salir de la app probablemente)
            super.onBackPressed();
        }
    }
}