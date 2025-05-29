package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.models.Cliente;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NuevoClienteActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "NuevoClienteActivity";

    private ImageButton btnBackNuevoCliente;
    private TextInputLayout tilNombreNuevoCliente, tilTelefonoNuevoCliente, tilEmailNuevoCliente;
    private TextInputEditText etNombreNuevoCliente, etTelefonoNuevoCliente, etEmailNuevoCliente;
    private Button btnGuardarNuevoCliente;
    private ProgressBar pbGuardandoNuevoCliente;

    private DatabaseReference referenciaClientesBD;
    private FirebaseUser usuarioFirebaseActual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_cliente);

        referenciaClientesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CLIENTES);
        usuarioFirebaseActual = FirebaseAuth.getInstance().getCurrentUser(); // Para registrar quién lo creó

        btnBackNuevoCliente = findViewById(R.id.btnBackNuevoCliente);
        tilNombreNuevoCliente = findViewById(R.id.tilNombreNuevoCliente);
        etNombreNuevoCliente = findViewById(R.id.etNombreNuevoCliente);
        tilTelefonoNuevoCliente = findViewById(R.id.tilTelefonoNuevoCliente);
        etTelefonoNuevoCliente = findViewById(R.id.etTelefonoNuevoCliente);
        tilEmailNuevoCliente = findViewById(R.id.tilEmailNuevoCliente);
        etEmailNuevoCliente = findViewById(R.id.etEmailNuevoCliente);
        btnGuardarNuevoCliente = findViewById(R.id.btnGuardarNuevoCliente);
        pbGuardandoNuevoCliente = findViewById(R.id.pbGuardandoNuevoCliente);

        btnBackNuevoCliente.setOnClickListener(v -> finish());
        btnGuardarNuevoCliente.setOnClickListener(v -> validarYGuardarCliente());
    }

    private void validarYGuardarCliente() {
        String nombre = etNombreNuevoCliente.getText().toString().trim();
        String telefono = etTelefonoNuevoCliente.getText().toString().trim();
        String email = etEmailNuevoCliente.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            tilNombreNuevoCliente.setError("El nombre es requerido.");
            etNombreNuevoCliente.requestFocus();
            return;
        } else {
            tilNombreNuevoCliente.setError(null);
        }

        if (TextUtils.isEmpty(telefono)) {
            tilTelefonoNuevoCliente.setError("El teléfono es requerido.");
            etTelefonoNuevoCliente.requestFocus();
            return;
        } else {
            tilTelefonoNuevoCliente.setError(null);
        }

        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmailNuevoCliente.setError("Ingrese un correo válido si desea añadir uno.");
            etEmailNuevoCliente.requestFocus();
            return;
        } else {
            tilEmailNuevoCliente.setError(null);
        }

        mostrarProgreso(true);

        String idNuevoCliente = referenciaClientesBD.push().getKey();
        if (idNuevoCliente == null) {
            Toast.makeText(this, "Error al generar ID para el cliente.", Toast.LENGTH_SHORT).show();
            mostrarProgreso(false);
            return;
        }

        Cliente nuevoCliente = new Cliente(nombre, telefono, TextUtils.isEmpty(email) ? null : email);
        if (usuarioFirebaseActual != null) {
            nuevoCliente.setCreado_por(usuarioFirebaseActual.getUid()); // Guardar quién creó el cliente
        }
        nuevoCliente.setId(idNuevoCliente); // Asignar el ID para devolverlo

        referenciaClientesBD.child(idNuevoCliente).setValue(nuevoCliente)
                .addOnSuccessListener(aVoid -> {
                    mostrarProgreso(false);
                    Toast.makeText(NuevoClienteActivity.this, "Cliente guardado exitosamente.", Toast.LENGTH_LONG).show();
                    Intent resultadoIntent = new Intent();
                    resultadoIntent.putExtra("CLIENTE_SELECCIONADO", nuevoCliente);
                    setResult(Activity.RESULT_OK, resultadoIntent);
                    finish(); // Cerrar y volver
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Toast.makeText(NuevoClienteActivity.this, "Error al guardar cliente: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(ETIQUETA_DEBUG, "Error guardando cliente: ", e);
                });
    }

    private void mostrarProgreso(boolean mostrar) {
        if (mostrar) {
            pbGuardandoNuevoCliente.setVisibility(View.VISIBLE);
            btnGuardarNuevoCliente.setEnabled(false);
        } else {
            pbGuardandoNuevoCliente.setVisibility(View.GONE);
            btnGuardarNuevoCliente.setEnabled(true);
        }
    }
}