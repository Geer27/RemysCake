package com.example.remyscake.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.models.Usuario;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "LoginActivity";

    private TextInputEditText etCorreo, etContrasena;
    private Button btnIngresar;
    private TextView tvIrARegistro;
    private ProgressBar pbCargando;

    private FirebaseAuth autenticacionFirebase;
    private DatabaseReference referenciaUsuariosBD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacionFirebase = FirebaseAuth.getInstance();
        referenciaUsuariosBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_USUARIOS);

        etCorreo = findViewById(R.id.etCorreoLogin);
        etContrasena = findViewById(R.id.etContrasenaLogin);
        btnIngresar = findViewById(R.id.btnIngresarLogin);
        tvIrARegistro = findViewById(R.id.tvIrARegistro);
        pbCargando = findViewById(R.id.pbCargandoLogin);

        btnIngresar.setOnClickListener(vista -> procesarInicioSesion());

        tvIrARegistro.setOnClickListener(vista -> {
            Intent intencion = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intencion);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioActual = autenticacionFirebase.getCurrentUser();
        if (usuarioActual != null) {
            Log.d(ETIQUETA_DEBUG, "Usuario ya autenticado: " + usuarioActual.getUid());
            mostrarBarraProgreso(true);
            obtenerRolUsuarioYRedirigir(usuarioActual.getUid());
        }

    }

    private void procesarInicioSesion() {
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(correo) || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Ingrese un correo electrónico válido.");
            etCorreo.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(contrasena)) {
            etContrasena.setError("La contraseña es requerida.");
            etContrasena.requestFocus();
            return;
        }

        mostrarBarraProgreso(true);

        autenticacionFirebase.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, tareaInicioSesion -> {
                    if (tareaInicioSesion.isSuccessful()) {
                        Log.d(ETIQUETA_DEBUG, "signInWithEmail: Éxito");
                        FirebaseUser usuarioFirebase = autenticacionFirebase.getCurrentUser();
                        if (usuarioFirebase != null) {
                            // Aquí sí es correcto llamar a obtenerRolUsuarioYRedirigir
                            // porque es el resultado de un intento de login manual.
                            obtenerRolUsuarioYRedirigir(usuarioFirebase.getUid());
                        } else {
                            mostrarBarraProgreso(false);
                            Log.e(ETIQUETA_DEBUG, "FirebaseUser es null después de un inicio de sesión exitoso.");
                            Toast.makeText(LoginActivity.this, "Error inesperado al iniciar sesión.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mostrarBarraProgreso(false);
                        Log.w(ETIQUETA_DEBUG, "signInWithEmail: Falla", tareaInicioSesion.getException());
                        Toast.makeText(LoginActivity.this, "Error: Credenciales incorrectas o usuario no encontrado.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void obtenerRolUsuarioYRedirigir(String idUsuario) {
        Log.d(ETIQUETA_DEBUG, "Obteniendo rol para UID: " + idUsuario);
        referenciaUsuariosBD.child(idUsuario).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mostrarBarraProgreso(false);

                if (dataSnapshot.exists()) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    if (usuario != null && usuario.getRol() != null) {
                        String rol = usuario.getRol();
                        Log.d(ETIQUETA_DEBUG, "Rol del usuario: " + rol);
                        Toast.makeText(LoginActivity.this, "¡Bienvenido/a " + usuario.getNombreCompleto() + "!", Toast.LENGTH_SHORT).show();

                        Intent intencion;
                        switch (rol) {
                            case ConstantesApp.ROL_ADMIN:
                                intencion = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                break;
                            case ConstantesApp.ROL_VENDEDOR:
                                intencion = new Intent(LoginActivity.this, SellerDashboardActivity.class);
                                break;
                            case ConstantesApp.ROL_PRODUCCION:
                                intencion = new Intent(LoginActivity.this, ProductionDashboardActivity.class);
                                break;
                            default:
                                Log.e(ETIQUETA_DEBUG, "Rol de usuario no reconocido: " + rol);
                                Toast.makeText(LoginActivity.this, "Rol de usuario desconocido. Contacte al administrador.", Toast.LENGTH_LONG).show();
                                autenticacionFirebase.signOut();
                                return;
                        }
                        intencion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intencion);
                        finish();
                    } else {
                        Log.e(ETIQUETA_DEBUG, "Datos de usuario o rol son nulos en la BD para UID: " + idUsuario);
                        Toast.makeText(LoginActivity.this, "No se pudo obtener la información completa del perfil. Contacte al administrador.", Toast.LENGTH_LONG).show();
                        autenticacionFirebase.signOut();
                    }
                } else {
                    Log.e(ETIQUETA_DEBUG, "No existe snapshot de usuario en la BD para UID: " + idUsuario);
                    Toast.makeText(LoginActivity.this, "El perfil de usuario no existe en la base de datos. Contacte al administrador.", Toast.LENGTH_LONG).show();
                    autenticacionFirebase.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mostrarBarraProgreso(false);
                Log.e(ETIQUETA_DEBUG, "Error al leer rol del usuario.", databaseError.toException());
                Toast.makeText(LoginActivity.this, "Error al leer datos del perfil: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                autenticacionFirebase.signOut();
            }
        });
    }

    private void mostrarBarraProgreso(boolean mostrar) {
        if (mostrar) {
            pbCargando.setVisibility(View.VISIBLE);
            btnIngresar.setEnabled(false);
        } else {
            pbCargando.setVisibility(View.GONE);
            btnIngresar.setEnabled(true);
        }
    }
}