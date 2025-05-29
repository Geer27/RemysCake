package com.example.remyscake.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;

import java.util.ArrayList;
// import java.util.List;

public class NotificacionesActivity extends AppCompatActivity {

    private ImageButton btnBackNotificaciones;
    private RecyclerView rvNotificaciones;
    private TextView tvSinNotificaciones;
    // private NotificacionesAdapter adapter;
    // private List<Notificacion> listaNotificaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);
        setTitle("Notificaciones");

        btnBackNotificaciones = findViewById(R.id.btnBackNotificaciones);
        rvNotificaciones = findViewById(R.id.rvNotificaciones);
        tvSinNotificaciones = findViewById(R.id.tvSinNotificaciones);

        btnBackNotificaciones.setOnClickListener(v -> finish());

        configurarRecyclerView();
        cargarNotificaciones(); // Implementar esta lógica
    }

    private void configurarRecyclerView() {
        rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
        // adapter = new NotificacionesAdapter(listaNotificaciones, this);
        // rvNotificaciones.setAdapter(adapter);
    }

    private void cargarNotificaciones() {
        // Aquí iría la lógica para cargar notificaciones (desde Firebase, SharedPreferences, etc.)
        // Por ahora, simularemos que no hay
        // listaNotificaciones.clear();

        if (/*listaNotificaciones.isEmpty()*/ true) {
            tvSinNotificaciones.setVisibility(View.VISIBLE);
            rvNotificaciones.setVisibility(View.GONE);
        } else {
            tvSinNotificaciones.setVisibility(View.GONE);
            rvNotificaciones.setVisibility(View.VISIBLE);
            // adapter.notifyDataSetChanged();
        }
        Toast.makeText(this, "Carga de notificaciones (Pendiente)", Toast.LENGTH_SHORT).show();
    }
}