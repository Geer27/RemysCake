package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.adapters.ClientesSeleccionAdapter;
import com.example.remyscake.models.Cliente;
import com.example.remyscake.utils.ConstantesApp;
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

public class BuscarClienteActivity extends AppCompatActivity implements ClientesSeleccionAdapter.OnClienteSeleccionadoListener {

    private static final String ETIQUETA_DEBUG = "BuscarClienteActivity";

    private ImageButton btnBackBuscarCliente;
    private SearchView searchViewClientes;
    private RecyclerView rvClientesEncontrados;
    private TextView tvNoSeEncontraronClientes;

    private ClientesSeleccionAdapter adapter;
    private List<Cliente> listaTodosLosClientes = new ArrayList<>();
    private List<Cliente> listaClientesFiltrados = new ArrayList<>();

    private DatabaseReference referenciaClientesBD;
    private FirebaseUser usuarioFirebaseActual; // Para filtrar por clientes del vendedor si es necesario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_cliente);

        referenciaClientesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CLIENTES);
        usuarioFirebaseActual = FirebaseAuth.getInstance().getCurrentUser();

        btnBackBuscarCliente = findViewById(R.id.btnBackBuscarCliente);
        searchViewClientes = findViewById(R.id.searchViewClientes);
        rvClientesEncontrados = findViewById(R.id.rvClientesEncontrados);
        tvNoSeEncontraronClientes = findViewById(R.id.tvNoSeEncontraronClientes);

        btnBackBuscarCliente.setOnClickListener(v -> finish());

        configurarRecyclerView();
        configurarSearchView();
        cargarTodosLosClientes();
    }

    private void configurarRecyclerView() {
        rvClientesEncontrados.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClientesSeleccionAdapter(listaClientesFiltrados, this, this);
        rvClientesEncontrados.setAdapter(adapter);
    }

    private void configurarSearchView() {
        searchViewClientes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarClientes(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarClientes(newText);
                return false;
            }
        });
    }

    private void cargarTodosLosClientes() {
        tvNoSeEncontraronClientes.setText("Cargando clientes...");
        tvNoSeEncontraronClientes.setVisibility(View.VISIBLE);
        rvClientesEncontrados.setVisibility(View.GONE);

        Query query = referenciaClientesBD.orderByChild("nombreCompleto");


        query.addListenerForSingleValueEvent(new ValueEventListener() { // Cargar solo una vez
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaTodosLosClientes.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Cliente cliente = snapshot.getValue(Cliente.class);
                        if (cliente != null) {
                            cliente.setId(snapshot.getKey());
                            listaTodosLosClientes.add(cliente);
                        }
                    }
                }
                // Inicialmente mostrar todos o aplicar un filtro vacío
                filtrarClientes("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar clientes: ", databaseError.toException());
                Toast.makeText(BuscarClienteActivity.this, "Error al cargar lista de clientes.", Toast.LENGTH_SHORT).show();
                tvNoSeEncontraronClientes.setText("Error al cargar clientes.");
            }
        });
    }

    private void filtrarClientes(String textoBusqueda) {
        listaClientesFiltrados.clear();
        if (TextUtils.isEmpty(textoBusqueda)) {
            listaClientesFiltrados.addAll(listaTodosLosClientes); // Mostrar todos si no hay búsqueda
        } else {
            String busquedaLower = textoBusqueda.toLowerCase().trim();
            for (Cliente cliente : listaTodosLosClientes) {
                if ((cliente.getNombreCompleto() != null && cliente.getNombreCompleto().toLowerCase().contains(busquedaLower)) ||
                        (cliente.getTelefono() != null && cliente.getTelefono().contains(textoBusqueda))) { // Teléfono sin toLowerCase
                    listaClientesFiltrados.add(cliente);
                }
            }
        }

        adapter.actualizarLista(listaClientesFiltrados);

        if (listaClientesFiltrados.isEmpty() && !TextUtils.isEmpty(textoBusqueda)) {
            tvNoSeEncontraronClientes.setText("No se encontraron clientes para: '" + textoBusqueda + "'");
            tvNoSeEncontraronClientes.setVisibility(View.VISIBLE);
            rvClientesEncontrados.setVisibility(View.GONE);
        } else if (listaClientesFiltrados.isEmpty() && TextUtils.isEmpty(textoBusqueda) && listaTodosLosClientes.isEmpty()){
            tvNoSeEncontraronClientes.setText("No hay clientes registrados.");
            tvNoSeEncontraronClientes.setVisibility(View.VISIBLE);
            rvClientesEncontrados.setVisibility(View.GONE);
        }
        else {
            tvNoSeEncontraronClientes.setVisibility(View.GONE);
            rvClientesEncontrados.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClienteSeleccionado(Cliente cliente) {
        Intent resultadoIntent = new Intent();
        resultadoIntent.putExtra("CLIENTE_SELECCIONADO", cliente);
        setResult(Activity.RESULT_OK, resultadoIntent);
        finish(); // Cerrar esta actividad y volver a CrearReservaActivity
    }
}