package com.example.remyscake.activities;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnCerrarSesionAdmin; // Botón para cerrar sesión

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Panel Administrador");

        btnCerrarSesionAdmin = new Button(this);
        btnCerrarSesionAdmin.setText("Cerrar Sesión (Admin)");
        btnCerrarSesionAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(AdminDashboardActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                // Redirigir a LoginActivity
                startActivity(new android.content.Intent(AdminDashboardActivity.this, LoginActivity.class)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        setContentView(btnCerrarSesionAdmin);
    }
}