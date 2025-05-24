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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "RegisterActivity";

    private TextInputEditText etNombreCompleto, etCorreo, etContrasena;
    private Button btnRegistrarme;
    private TextView tvIrALogin;
    private ProgressBar pbCargando;

    private FirebaseAuth autenticacionFirebase;
    private DatabaseReference referenciaUsuariosBD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        autenticacionFirebase = FirebaseAuth.getInstance();
        referenciaUsuariosBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_USUARIOS);

        etNombreCompleto = findViewById(R.id.etNombreRegistro);
        etCorreo = findViewById(R.id.etCorreoRegistro);
        etContrasena = findViewById(R.id.etContrasenaRegistro);
        btnRegistrarme = findViewById(R.id.btnRegistrarme);
        tvIrALogin = findViewById(R.id.tvIrALogin);
        pbCargando = findViewById(R.id.pbCargandoRegistro);

        btnRegistrarme.setOnClickListener(vista -> procesarRegistroUsuario());

        tvIrALogin.setOnClickListener(vista -> {
            Intent intencion = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intencion);
            finish();
        });
    }

    private void procesarRegistroUsuario() {
        final String nombre = etNombreCompleto.getText().toString().trim();
        final String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            etNombreCompleto.setError("El nombre es requerido.");
            etNombreCompleto.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(correo) || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Ingrese un correo electrónico válido.");
            etCorreo.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(contrasena) || contrasena.length() < 6) {
            etContrasena.setError("La contraseña debe tener al menos 6 caracteres.");
            etContrasena.requestFocus();
            return;
        }

        mostrarBarraProgreso(true);

        autenticacionFirebase.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, tareaCreacion -> {
                    if (tareaCreacion.isSuccessful()) {
                        Log.d(ETIQUETA_DEBUG, "createUserWithEmail: Éxito");
                        FirebaseUser usuarioFirebase = autenticacionFirebase.getCurrentUser();
                        if (usuarioFirebase != null) {
                            String idUsuario = usuarioFirebase.getUid();
                            String rolPorDefecto = ConstantesApp.ROL_VENDEDOR;

                            Usuario nuevoUsuario = new Usuario(nombre, correo, rolPorDefecto);

                            referenciaUsuariosBD.child(idUsuario).setValue(nuevoUsuario)
                                    .addOnCompleteListener(tareaGuardadoBD -> {
                                        mostrarBarraProgreso(false);
                                        if (tareaGuardadoBD.isSuccessful()) {
                                            Log.d(ETIQUETA_DEBUG, "Datos de usuario guardados en Realtime Database.");
                                            Toast.makeText(RegisterActivity.this, "Registro exitoso. Por favor, verifica tu correo.", Toast.LENGTH_LONG).show();

                                            usuarioFirebase.sendEmailVerification()
                                                    .addOnCompleteListener(tareaEnvioCorreo -> {
                                                        if (tareaEnvioCorreo.isSuccessful()) {
                                                            Log.d(ETIQUETA_DEBUG, "Correo de verificación enviado.");
                                                        } else {
                                                            Log.e(ETIQUETA_DEBUG, "Error al enviar correo de verificación.", tareaEnvioCorreo.getException());
                                                        }
                                                    });

                                            Intent intencion = new Intent(RegisterActivity.this, LoginActivity.class);
                                            intencion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intencion);
                                            finish();
                                        } else {
                                            Log.e(ETIQUETA_DEBUG, "Error al guardar datos de usuario en BD.", tareaGuardadoBD.getException());
                                            Toast.makeText(RegisterActivity.this, "Error al guardar datos del perfil: " + tareaGuardadoBD.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            mostrarBarraProgreso(false);
                            Log.e(ETIQUETA_DEBUG, "FirebaseUser es null después de un registro exitoso.");
                            Toast.makeText(RegisterActivity.this, "Error inesperado durante el registro.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mostrarBarraProgreso(false);
                        Log.w(ETIQUETA_DEBUG, "createUserWithEmail: Falla", tareaCreacion.getException());
                        if (tareaCreacion.getException() instanceof FirebaseAuthUserCollisionException) {
                            etCorreo.setError("Este correo electrónico ya está registrado.");
                            etCorreo.requestFocus();
                            Toast.makeText(RegisterActivity.this, "Este correo ya se encuentra registrado.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error de registro: " + tareaCreacion.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void mostrarBarraProgreso(boolean mostrar) {
        if (mostrar) {
            pbCargando.setVisibility(View.VISIBLE);
            btnRegistrarme.setEnabled(false);
        } else {
            pbCargando.setVisibility(View.GONE);
            btnRegistrarme.setEnabled(true);
        }
    }
}