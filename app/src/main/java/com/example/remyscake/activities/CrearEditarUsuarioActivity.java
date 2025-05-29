package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.models.Usuario;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrearEditarUsuarioActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "CrearEditUsuario";

    private ImageButton btnBackCrearEditarUsuario;
    private TextView tvTituloCrearEditarUsuario;
    private TextInputLayout tilNombreCompletoUsuario, tilEmailUsuario, tilContrasenaUsuario, tilRolUsuario;
    private TextInputEditText etNombreCompletoUsuario, etEmailUsuario, etContrasenaUsuario;
    private AutoCompleteTextView actvRolUsuario;
    private Button btnGuardarUsuario;
    private ProgressBar pbGuardandoUsuario;

    private FirebaseAuth autenticacionFirebase;
    private DatabaseReference referenciaUsuariosBD;

    private String uidUsuarioAEditar = null;
    private Usuario usuarioAEditar = null;
    private String emailOriginalUsuarioAEditar = null; // Para verificar si el email cambió

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_editar_usuario);

        autenticacionFirebase = FirebaseAuth.getInstance();
        referenciaUsuariosBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_USUARIOS);

        inicializarVistas();
        configurarSpinnerRoles();
        establecerListeners();

        if (getIntent().hasExtra("UID_USUARIO")) {
            uidUsuarioAEditar = getIntent().getStringExtra("UID_USUARIO");
            tvTituloCrearEditarUsuario.setText("Editar Usuario");
            btnGuardarUsuario.setText("Actualizar Usuario");
            cargarDatosUsuarioAEditar();
        } else {
            tvTituloCrearEditarUsuario.setText("Agregar Nuevo Usuario");
            btnGuardarUsuario.setText("Crear Usuario");
        }
    }

    private void inicializarVistas() {
        btnBackCrearEditarUsuario = findViewById(R.id.btnBackCrearEditarUsuario);
        tvTituloCrearEditarUsuario = findViewById(R.id.tvTituloCrearEditarUsuario);

        tilNombreCompletoUsuario = findViewById(R.id.tilNombreCompletoUsuario);
        etNombreCompletoUsuario = findViewById(R.id.etNombreCompletoUsuario);
        tilEmailUsuario = findViewById(R.id.tilEmailUsuario);
        etEmailUsuario = findViewById(R.id.etEmailUsuario);
        tilContrasenaUsuario = findViewById(R.id.tilContrasenaUsuario);
        etContrasenaUsuario = findViewById(R.id.etContrasenaUsuario);
        tilRolUsuario = findViewById(R.id.tilRolUsuario);
        actvRolUsuario = findViewById(R.id.actvRolUsuario);

        btnGuardarUsuario = findViewById(R.id.btnGuardarUsuario);
        pbGuardandoUsuario = findViewById(R.id.pbGuardandoUsuario);
    }

    private void configurarSpinnerRoles() {
        // Roles definidos en ConstantesApp
        List<String> listaRoles = Arrays.asList(
                ConstantesApp.ROL_VENDEDOR,
                ConstantesApp.ROL_PRODUCCION,
                ConstantesApp.ROL_ADMIN
        );
        ArrayAdapter<String> adapterRoles = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                listaRoles
        );
        actvRolUsuario.setAdapter(adapterRoles);
    }

    private void establecerListeners() {
        btnBackCrearEditarUsuario.setOnClickListener(v -> finish()); // Simplemente cierra la actividad
        btnGuardarUsuario.setOnClickListener(v -> validarYProcesarUsuario());
    }

    private void cargarDatosUsuarioAEditar() {
        if (uidUsuarioAEditar == null) return;
        mostrarProgreso(true);
        referenciaUsuariosBD.child(uidUsuarioAEditar).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mostrarProgreso(false);
                if (dataSnapshot.exists()) {
                    usuarioAEditar = dataSnapshot.getValue(Usuario.class);
                    if (usuarioAEditar != null) {
                        etNombreCompletoUsuario.setText(usuarioAEditar.getNombreCompleto());
                        etEmailUsuario.setText(usuarioAEditar.getCorreoElectronico());
                        emailOriginalUsuarioAEditar = usuarioAEditar.getCorreoElectronico(); // Guardar email original
                        // No mostramos la contraseña existente
                        etContrasenaUsuario.setHint("Dejar vacío para no cambiar");
                        actvRolUsuario.setText(usuarioAEditar.getRol(), false); // false para no filtrar
                    }
                } else {
                    Toast.makeText(CrearEditarUsuarioActivity.this, "Usuario no encontrado.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mostrarProgreso(false);
                Log.e(ETIQUETA_DEBUG, "Error al cargar usuario: ", databaseError.toException());
                Toast.makeText(CrearEditarUsuarioActivity.this, "Error al cargar datos.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void validarYProcesarUsuario() {
        String nombre = etNombreCompletoUsuario.getText().toString().trim();
        String email = etEmailUsuario.getText().toString().trim();
        String contrasena = etContrasenaUsuario.getText().toString().trim();
        String rol = actvRolUsuario.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombre)) {
            tilNombreCompletoUsuario.setError("Nombre requerido."); etNombreCompletoUsuario.requestFocus(); return;
        } else { tilNombreCompletoUsuario.setError(null); }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmailUsuario.setError("Email inválido."); etEmailUsuario.requestFocus(); return;
        } else { tilEmailUsuario.setError(null); }

        if (uidUsuarioAEditar == null && TextUtils.isEmpty(contrasena)) { // Contraseña requerida solo para nuevos usuarios
            tilContrasenaUsuario.setError("Contraseña requerida para nuevo usuario."); etContrasenaUsuario.requestFocus(); return;
        } else { tilContrasenaUsuario.setError(null); }

        if (!TextUtils.isEmpty(contrasena) && contrasena.length() < 6) {
            tilContrasenaUsuario.setError("La contraseña debe tener al menos 6 caracteres."); etContrasenaUsuario.requestFocus(); return;
        } else if (!TextUtils.isEmpty(contrasena)) {
            tilContrasenaUsuario.setError(null);
        }


        if (TextUtils.isEmpty(rol)) {
            tilRolUsuario.setError("Rol requerido.");
            Toast.makeText(this, "Seleccione un rol para el usuario.", Toast.LENGTH_SHORT).show();
            return;
        } else { tilRolUsuario.setError(null); }


        mostrarProgreso(true);

        if (uidUsuarioAEditar != null) {
            // Editar usuario existente
            actualizarUsuarioEnFirebase(nombre, email, contrasena, rol);
        } else {
            // Crear nuevo usuario
            crearUsuarioEnFirebase(nombre, email, contrasena, rol);
        }
    }

    private void crearUsuarioEnFirebase(String nombre, String email, String contrasena, String rol) {
        autenticacionFirebase.createUserWithEmailAndPassword(email, contrasena)
                .addOnCompleteListener(this, taskAuth -> {
                    if (taskAuth.isSuccessful()) {
                        FirebaseUser firebaseUser = autenticacionFirebase.getCurrentUser();
                        if (firebaseUser != null) {
                            String nuevoUid = firebaseUser.getUid();
                            Usuario nuevoUsuario = new Usuario(nombre, email, rol);

                            referenciaUsuariosBD.child(nuevoUid).setValue(nuevoUsuario)
                                    .addOnCompleteListener(taskDb -> {
                                        mostrarProgreso(false);
                                        if (taskDb.isSuccessful()) {
                                            Toast.makeText(this, "Usuario creado exitosamente.", Toast.LENGTH_LONG).show();
                                            firebaseUser.sendEmailVerification(); // Opcional
                                            finish(); // Volver a la lista de usuarios
                                        } else {
                                            Toast.makeText(this, "Error al guardar datos del usuario en DB.", Toast.LENGTH_LONG).show();
                                            Log.e(ETIQUETA_DEBUG, "Error guardando en DB: ", taskDb.getException());
                                            // Considerar borrar de Auth si falla el guardado en DB
                                            firebaseUser.delete();
                                        }
                                    });
                        } else { // Caso raro
                            mostrarProgreso(false);
                            Toast.makeText(this, "Error: No se pudo obtener el usuario de Firebase Auth.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        mostrarProgreso(false);
                        if (taskAuth.getException() instanceof FirebaseAuthUserCollisionException) {
                            tilEmailUsuario.setError("Este correo ya está en uso.");
                            etEmailUsuario.requestFocus();
                        } else {
                            Toast.makeText(this, "Error al crear usuario en Auth: " + taskAuth.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        Log.e(ETIQUETA_DEBUG, "Error creando en Auth: ", taskAuth.getException());
                    }
                });
    }

    private void actualizarUsuarioEnFirebase(String nuevoNombre, String nuevoEmail, String nuevaContrasena, String nuevoRol) {
        if (usuarioAEditar == null || uidUsuarioAEditar == null) {
            mostrarProgreso(false);
            Toast.makeText(this, "Error: No se pudo cargar el usuario a editar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar en Realtime Database
        Map<String, Object> actualizacionesUsuario = new HashMap<>();
        actualizacionesUsuario.put("nombreCompleto", nuevoNombre);
        actualizacionesUsuario.put("correoElectronico", nuevoEmail); // El email en DB debe coincidir con Auth para login futuro
        actualizacionesUsuario.put("rol", nuevoRol);

        referenciaUsuariosBD.child(uidUsuarioAEditar).updateChildren(actualizacionesUsuario)
                .addOnSuccessListener(aVoid -> {
                    mostrarProgreso(false);
                    Toast.makeText(this, "Datos del usuario actualizados en la base de datos.", Toast.LENGTH_LONG).show();
                    if (!TextUtils.isEmpty(nuevaContrasena)) {
                        // ESTA ES SOLO UNA NOTIFICACIÓN. LA CONTRASEÑA EN AUTH NO CAMBIÓ.
                        Toast.makeText(this, "Si ingresaste una nueva contraseña, comunícasela al usuario.", Toast.LENGTH_LONG).show();
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Toast.makeText(this, "Error al actualizar datos en DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(ETIQUETA_DEBUG, "Error actualizando en DB: ", e);
                });

        /*
        // Lógica para actualizar contraseña en Firebase Auth (si el admin pudiera hacerlo)
        FirebaseUser usuarioAuthActual = FirebaseAuth.getInstance().getCurrentUser(); // El admin
        // NO HAY FORMA DIRECTA DE OBTENER EL FirebaseUser DE uidUsuarioAEditar PARA MODIFICARLO
        if (!TextUtils.isEmpty(nuevaContrasena)) {
           // Aquí necesitarías llamar a una Cloud Function o un endpoint de tu backend
           // que use el Admin SDK para cambiar la contraseña del usuario con uidUsuarioAEditar.
           // Ejemplo conceptual (NO FUNCIONA EN CLIENTE):
           // FirebaseUser usuarioQueSeEdita = FirebaseAuth.getInstance().getUser(uidUsuarioAEditar); <--- MÉTODO NO EXISTE
           // usuarioQueSeEdita.updatePassword(nuevaContrasena)...
        }

        // Lógica para actualizar email en Firebase Auth (si el admin pudiera hacerlo)
        if (!nuevoEmail.equals(emailOriginalUsuarioAEditar)) {
            // Similar a la contraseña, esto requeriría Admin SDK en backend.
            // Además, cambiar el email de un usuario a menudo requiere verificación del nuevo email.
        }
        */
    }


    private void mostrarProgreso(boolean mostrar) {
        if (mostrar) {
            pbGuardandoUsuario.setVisibility(View.VISIBLE);
            btnGuardarUsuario.setEnabled(false);
        } else {
            pbGuardandoUsuario.setVisibility(View.GONE);
            btnGuardarUsuario.setEnabled(true);
        }
    }
}