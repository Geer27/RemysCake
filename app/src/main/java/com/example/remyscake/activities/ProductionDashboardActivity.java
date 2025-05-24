package com.example.remyscake.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class ProductionDashboardActivity extends AppCompatActivity {

    private Button btnCerrarSesionProduccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_production_dashboard);
        setTitle("Panel Producción");

        btnCerrarSesionProduccion = new Button(this);
        btnCerrarSesionProduccion.setText("Cerrar Sesión (Producción)");
        btnCerrarSesionProduccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(ProductionDashboardActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                startActivity(new android.content.Intent(ProductionDashboardActivity.this, LoginActivity.class)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });
        setContentView(btnCerrarSesionProduccion);
    }
}