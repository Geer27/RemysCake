package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.models.Reserva;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat; // Para formatear moneda
import java.util.Calendar;
import java.util.Locale;

public class GenerarReportesActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "GenerarReportes";

    // Vistas UI
    private ImageButton btnBackReportes, btnHistorialReportes;
    private TextView tvVentasMes, tvPedidosMes, tvClientesNuevos;
    private CardView cvReporteVentas, cvReporteClientes, cvReporteProductos, cvReporteRendimiento, cvReportePersonalizado;
    private FloatingActionButton fabGenerarReporte;

    private DatabaseReference referenciaReservacionesBD;
    private DatabaseReference referenciaClientesBD;
    // private DatabaseReference referenciaProductosBD; // Si reportas sobre productos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_reportes);

        referenciaReservacionesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);
        referenciaClientesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CLIENTES);
        // referenciaProductosBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CATALOGO_PASTELES);


        inicializarVistas();
        establecerListeners();
        cargarResumenDelMes(); // Cargar estadísticas básicas
    }

    private void inicializarVistas() {
        btnBackReportes = findViewById(R.id.btnBackReportes);
        btnHistorialReportes = findViewById(R.id.btnHistorialReportes);

        tvVentasMes = findViewById(R.id.tvVentasMes);
        tvPedidosMes = findViewById(R.id.tvPedidosMes);
        tvClientesNuevos = findViewById(R.id.tvClientesNuevos);

        cvReporteVentas = findViewById(R.id.cvReporteVentas);
        cvReporteClientes = findViewById(R.id.cvReporteClientes);
        cvReporteProductos = findViewById(R.id.cvReporteProductos);
        cvReporteRendimiento = findViewById(R.id.cvReporteRendimiento);
        cvReportePersonalizado = findViewById(R.id.cvReportePersonalizado);

        fabGenerarReporte = findViewById(R.id.fabGenerarReporte);
    }

    private void establecerListeners() {
        btnBackReportes.setOnClickListener(v -> onBackPressed());

        btnHistorialReportes.setOnClickListener(v -> {
            Toast.makeText(this, "Historial de Reportes (Próximamente)", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, HistorialReportesActivity.class);
            // startActivity(intent);
        });

        View.OnClickListener listenerReportePlaceholder = v -> {
            String tag = v.getTag() != null ? v.getTag().toString() : "Reporte";
            Toast.makeText(this, tag + " (Próximamente)", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, VistaReporteActivity.class);
            // intent.putExtra("TIPO_REPORTE", tag);
            // startActivity(intent);
        };

        cvReporteVentas.setTag("Reporte de Ventas");
        cvReporteVentas.setOnClickListener(listenerReportePlaceholder);

        cvReporteClientes.setTag("Reporte de Clientes");
        cvReporteClientes.setOnClickListener(listenerReportePlaceholder);

        cvReporteProductos.setTag("Reporte de Productos");
        cvReporteProductos.setOnClickListener(listenerReportePlaceholder);

        cvReporteRendimiento.setTag("Reporte de Rendimiento");
        cvReporteRendimiento.setOnClickListener(listenerReportePlaceholder);

        cvReportePersonalizado.setTag("Reporte Personalizado");
        cvReportePersonalizado.setOnClickListener(listenerReportePlaceholder);

        fabGenerarReporte.setOnClickListener(v -> {
            Toast.makeText(this, "Generar Reporte Rápido (Próximamente)", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarResumenDelMes() {
        // Obtener el primer y último día del mes actual
        Calendar calendario = Calendar.getInstance();
        calendario.set(Calendar.DAY_OF_MONTH, 1);
        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        long inicioMesMillis = calendario.getTimeInMillis();

        calendario.add(Calendar.MONTH, 1);
        calendario.add(Calendar.MILLISECOND, -1); // Último milisegundo del mes
        long finMesMillis = calendario.getTimeInMillis();

        Query queryReservasMes = referenciaReservacionesBD
                .orderByChild("createdAt")
                .startAt(inicioMesMillis)
                .endAt(finMesMillis);

        queryReservasMes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double ventasTotalesMes = 0;
                long pedidosTotalesMes = 0;

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Reserva reserva = snapshot.getValue(Reserva.class);
                        if (reserva != null && reserva.getPayment() != null) {
                            pedidosTotalesMes++;

                            // Para ventas, solo contar si el pago está completado
                            if ("completado".equalsIgnoreCase(reserva.getPayment().getStatusPago())) {
                                ventasTotalesMes += reserva.getPayment().getAmount();
                            }
                        }
                    }
                }
                NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "SV")); // Para formato $
                tvVentasMes.setText(formatoMoneda.format(ventasTotalesMes));
                tvPedidosMes.setText(String.valueOf(pedidosTotalesMes));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar resumen de reservaciones: ", databaseError.toException());
                tvVentasMes.setText("$0.00");
                tvPedidosMes.setText("0");
            }
        });


        // 2. Clientes Nuevos del Mes (desde Clientes)
        Query queryClientesNuevos = referenciaClientesBD
                .orderByChild("fecha_registro") // Necesitas este campo en tu modelo Cliente y datos
                .startAt(inicioMesMillis)
                .endAt(finMesMillis);

        queryClientesNuevos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tvClientesNuevos.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar clientes nuevos: ", databaseError.toException());
                tvClientesNuevos.setText("0");
            }
        });
    }


    // private void generarReporteVentas() { ... }
    // private void generarReporteClientes() { ... }
}