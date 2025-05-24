package com.example.remyscake.activities;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class SellerDashboardActivity extends AppCompatActivity {

    private Button btnCerrarSesionVendedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_seller_dashboard);
        setTitle("Panel Vendedor");

        btnCerrarSesionVendedor = new Button(this);
        btnCerrarSesionVendedor.setText("Cerrar Sesión (Vendedor)");
        btnCerrarSesionVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(SellerDashboardActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                startActivity(new android.content.Intent(SellerDashboardActivity.this, LoginActivity.class)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });
        setContentView(btnCerrarSesionVendedor);
    }
}