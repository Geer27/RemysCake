package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.remyscake.R;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GestionarClientesSellerActivity extends AppCompatActivity /* implements ClientesAdapter.OnClienteActionsListener */ {

    private static final String ETIQUETA_DEBUG = "GestionClientesSeller";

    private ImageButton btnBackGestionClientesSeller;
    private RecyclerView rvGestionClientesSeller;
    private TextView tvSinClientesSeller;
    private FloatingActionButton fabAgregarClienteSeller;

    // private ClientesAdapter adapter;
    // private List<Cliente> listaMisClientes = new ArrayList<>();

    private DatabaseReference referenciaClientesBD;
    private FirebaseUser usuarioActual;
    private ValueEventListener listenerMisClientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_clientes_seller);
        setTitle("Mis Clientes");

        usuarioActual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioActual == null) {
            Toast.makeText(this, "Error de sesión.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        referenciaClientesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CLIENTES);

        btnBackGestionClientesSeller = findViewById(R.id.btnBackGestionClientesSeller);
        rvGestionClientesSeller = findViewById(R.id.rvGestionClientesSeller);
        tvSinClientesSeller = findViewById(R.id.tvSinClientesSeller);
        fabAgregarClienteSeller = findViewById(R.id.fabAgregarClienteSeller);

        btnBackGestionClientesSeller.setOnClickListener(v -> finish());
        fabAgregarClienteSeller.setOnClickListener(v -> {
            Toast.makeText(this, "Agregar Nuevo Cliente (Próximamente)", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, CrearEditarClienteActivity.class);
            // startActivity(intent);
        });

        configurarRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMisClientes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (listenerMisClientes != null) {
            Query query = referenciaClientesBD.orderByChild("creado_por").equalTo(usuarioActual.getUid());
            query.removeEventListener(listenerMisClientes);
        }
    }

    private void configurarRecyclerView() {
        rvGestionClientesSeller.setLayoutManager(new LinearLayoutManager(this));
        // adapter = new ClientesAdapter(listaMisClientes, this, this);
        // rvGestionClientesSeller.setAdapter(adapter);
    }

    private void cargarMisClientes() {
        tvSinClientesSeller.setText("Cargando tus clientes...");
        tvSinClientesSeller.setVisibility(View.VISIBLE);
        rvGestionClientesSeller.setVisibility(View.GONE);

        if (listenerMisClientes != null) {
            Query oldQuery = referenciaClientesBD.orderByChild("creado_por").equalTo(usuarioActual.getUid());
            oldQuery.removeEventListener(listenerMisClientes);
        }

        Query queryMisClientes = referenciaClientesBD.orderByChild("creado_por").equalTo(usuarioActual.getUid());

        listenerMisClientes = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // listaMisClientes.clear();
                // if (dataSnapshot.exists()) {
                //    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                //        Cliente cliente = snapshot.getValue(Cliente.class);
                //        if (cliente != null) {
                //            cliente.setId(snapshot.getKey());
                //            listaMisClientes.add(cliente);
                //        }
                //    }
                //    Collections.sort(listaMisClientes, Comparator.comparing(Cliente::getNombreCompleto, String.CASE_INSENSITIVE_ORDER));
                // }

                // if (listaMisClientes.isEmpty()) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) { // Simulación
                    tvSinClientesSeller.setText("No has registrado clientes aún.");
                    tvSinClientesSeller.setVisibility(View.VISIBLE);
                    rvGestionClientesSeller.setVisibility(View.GONE);
                } else {
                    tvSinClientesSeller.setVisibility(View.GONE);
                    rvGestionClientesSeller.setVisibility(View.VISIBLE);
                    // adapter.actualizarLista(listaMisClientes);
                }
                Toast.makeText(GestionarClientesSellerActivity.this, "Carga de clientes (Pendiente)", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar mis clientes: ", databaseError.toException());
                Toast.makeText(GestionarClientesSellerActivity.this, "Error al cargar tus clientes.", Toast.LENGTH_SHORT).show();
                tvSinClientesSeller.setText("Error al cargar clientes.");
            }
        };
        queryMisClientes.addValueEventListener(listenerMisClientes);
    }

    // Implementación de OnClienteActionsListener
    // @Override
    // public void onEditClienteClick(Cliente cliente) {
    //    Intent intent = new Intent(this, CrearEditarClienteActivity.class);
    //    intent.putExtra("ID_CLIENTE", cliente.getId());
    //    startActivity(intent);
    // }
}