package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.remyscake.R;
import com.example.remyscake.utils.ConstantesApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PortadaActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "PortadaActivity";
    private Button btnIrALogin, btnIrARegistro;
    private FirebaseAuth autenticacionFirebase;
    private DatabaseReference referenciaUsuariosBD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portada);

        autenticacionFirebase = FirebaseAuth.getInstance();
        referenciaUsuariosBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_USUARIOS);

        btnIrALogin = findViewById(R.id.btnIrALoginPortada);
        btnIrARegistro = findViewById(R.id.btnIrARegistroPortada);

        btnIrALogin.setOnClickListener(vista -> {
            Intent intencion = new Intent(PortadaActivity.this, LoginActivity.class);
            startActivity(intencion);
        });

        btnIrARegistro.setOnClickListener(vista -> {
            Intent intencion = new Intent(PortadaActivity.this, RegisterActivity.class);
            startActivity(intencion);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Comprobar si el usuario ya ha iniciado sesión
        FirebaseUser usuarioActual = autenticacionFirebase.getCurrentUser();
        if (usuarioActual != null) {
            // Si hay un usuario, intentar redirigir directamente a su dashboard
            // Esto evita que el usuario vea la PortadaActivity si ya está logueado
            Log.d(ETIQUETA_DEBUG, "Usuario ya autenticado: " + usuarioActual.getUid() + ". Redirigiendo...");
            // Aquí no mostramos ProgressBar ya que la PortadaActivity es visualmente diferente.
            // La ProgressBar se manejará en LoginActivity si es necesario un nuevo login.
            obtenerRolUsuarioYRedirigir(usuarioActual.getUid());
        } else {
            Log.d(ETIQUETA_DEBUG, "No hay usuario autenticado. Mostrando opciones de portada.");
            // Si no hay usuario, la actividad simplemente se muestra con los botones.
        }
    }

    private void obtenerRolUsuarioYRedirigir(String idUsuario) {
        referenciaUsuariosBD.child(idUsuario).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isDestroyed() || isFinishing()) { // Evitar operaciones si la actividad ya no existe
                    return;
                }
                if (dataSnapshot.exists()) {
                    com.example.remyscake.models.Usuario usuario = dataSnapshot.getValue(com.example.remyscake.models.Usuario.class);
                    if (usuario != null && usuario.getRol() != null) {
                        String rol = usuario.getRol();
                        Log.d(ETIQUETA_DEBUG, "Rol del usuario: " + rol + ". Redirigiendo a dashboard.");

                        Intent intencion;
                        switch (rol) {
                            case ConstantesApp.ROL_ADMIN:
                                intencion = new Intent(PortadaActivity.this, AdminDashboardActivity.class);
                                break;
                            case ConstantesApp.ROL_VENDEDOR:
                                intencion = new Intent(PortadaActivity.this, SellerDashboardActivity.class);
                                break;
                            case ConstantesApp.ROL_PRODUCCION:
                                intencion = new Intent(PortadaActivity.this, ProductionDashboardActivity.class);
                                break;
                            default:
                                Log.e(ETIQUETA_DEBUG, "Rol de usuario no reconocido: " + rol + ". Deslogueando.");
                                autenticacionFirebase.signOut(); // Desloguear si el rol es inválido
                                return; // No redirigir, el usuario verá la PortadaActivity la próxima vez.
                        }
                        intencion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intencion);
                        finish(); // Finaliza PortadaActivity para que no quede en el stack
                    } else {
                        Log.e(ETIQUETA_DEBUG, "Datos de usuario o rol son nulos en la BD para UID: " + idUsuario + ". Deslogueando.");
                        autenticacionFirebase.signOut();
                    }
                } else {
                    Log.e(ETIQUETA_DEBUG, "No existe snapshot de usuario en la BD para UID: " + idUsuario + ". Deslogueando.");
                    autenticacionFirebase.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isDestroyed() || isFinishing()) {
                    return;
                }
                Log.e(ETIQUETA_DEBUG, "Error al leer rol del usuario.", databaseError.toException());
                autenticacionFirebase.signOut(); // Desloguear en caso de error crítico de DB
            }
        });
    }
}