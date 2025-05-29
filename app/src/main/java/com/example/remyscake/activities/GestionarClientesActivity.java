package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.remyscake.R;
import com.example.remyscake.adapters.ClienteAdminAdapter;
import com.example.remyscake.models.Cliente;
import com.example.remyscake.models.Usuario;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestionarClientesActivity extends AppCompatActivity implements ClienteAdminAdapter.OnClienteAdminActionsListener {

    private static final String ETIQUETA_DEBUG = "GestionClientes";

    private ImageButton btnBackClientes, btnSearchClientes, btnExportClientes;
    private TextView tvTotalClientes, tvClientesActivos, tvNuevosEsteMes; // Placeholder para los contadores de la cabecera
    private EditText etBuscarCliente;
    private ImageView ivClearSearch;
    private ChipGroup chipGroupSellers;
    private Spinner spinnerOrdenar;
    private RecyclerView rvClientes;
    private FloatingActionButton fabExportarClientes; // Reutilizado del layout, aunque el botón ya está en toolbar
    private TextView tvMensajeListaVacia; // Un TextView para mostrar si no hay clientes

    private DatabaseReference referenciaClientesBD;
    private DatabaseReference referenciaUsuariosBD; // Para cargar nombres de vendedores/sellers
    private ValueEventListener listenerClientes, listenerUsuarios;

    private ClienteAdminAdapter clienteAdminAdapter;
    private List<Cliente> listaTodosLosClientes = new ArrayList<>();
    private List<Cliente> listaClientesFiltrados = new ArrayList<>();
    private Map<String, String> mapaVendedores = new HashMap<>(); // UID -> Nombre del Vendedor
    private String filtroVendedorActualUID = "Todos"; // "Todos" o UID específico
    private String terminoBusquedaActual = "";
    private String ordenamientoActual = "Nombre (A-Z)";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_clientes);

        referenciaClientesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CLIENTES);
        referenciaUsuariosBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_USUARIOS);

        inicializarVistas();
        configurarRecyclerView();
        configurarSpinnerOrdenamiento();
        establecerListeners();

        // Cargar primero los vendedores para los chips, luego los clientes
        cargarVendedoresParaFiltro();
    }

    private void inicializarVistas() {
        btnBackClientes = findViewById(R.id.btnBackClientes);
        // btnSearchClientes = findViewById(R.id.btnSearchClientes); // El botón de search está, pero la lógica de búsqueda será en el EditText
        // btnExportClientes = findViewById(R.id.btnExportClientes); // Ya está en el FAB

        tvTotalClientes = findViewById(R.id.tvTotalClientes);
        tvClientesActivos = findViewById(R.id.tvClientesActivos);
        tvNuevosEsteMes = findViewById(R.id.tvNuevosEsteMes);

        etBuscarCliente = findViewById(R.id.etBuscarCliente);
        ivClearSearch = findViewById(R.id.ivClearSearch);
        chipGroupSellers = findViewById(R.id.chipGroupSellers);
        spinnerOrdenar = findViewById(R.id.spinnerOrdenar);
        rvClientes = findViewById(R.id.rvClientes);
        fabExportarClientes = findViewById(R.id.fabExportarClientes); // El FAB

        // tvMensajeListaVacia = findViewById(R.id.tvMensajeListaVaciaClientes); // Añade este TextView a tu layout si quieres
        // tvMensajeListaVacia.setVisibility(View.GONE);

        // Placeholder inicial de estadísticas
        tvTotalClientes.setText("0");
        tvClientesActivos.setText("0");
        tvNuevosEsteMes.setText("0");
    }

    private void configurarRecyclerView() {
        rvClientes.setLayoutManager(new LinearLayoutManager(this));
        listaClientesFiltrados = new ArrayList<>();
        clienteAdminAdapter = new ClienteAdminAdapter(listaClientesFiltrados, this, this);
        rvClientes.setAdapter(clienteAdminAdapter);
    }

    private void configurarSpinnerOrdenamiento() {
        String[] opciones = {"Nombre (A-Z)", "Nombre (Z-A)", "Fecha Registro (Nuevos)", "Fecha Registro (Antiguos)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrdenar.setAdapter(adapter);
    }

    private void establecerListeners() {
        btnBackClientes.setOnClickListener(v -> onBackPressed());

        fabExportarClientes.setOnClickListener(v -> {
            Toast.makeText(this, "Exportar clientes (Próximamente)", Toast.LENGTH_SHORT).show();
        });

        etBuscarCliente.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                terminoBusquedaActual = s.toString().trim().toLowerCase();
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                aplicarFiltrosYOrdenamiento();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        ivClearSearch.setOnClickListener(v -> etBuscarCliente.setText(""));

        spinnerOrdenar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ordenamientoActual = parent.getItemAtPosition(position).toString();
                aplicarFiltrosYOrdenamiento();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // En GestionarClientesActivity.java

        chipGroupSellers.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                filtroVendedorActualUID = "Todos";
                Log.d(ETIQUETA_DEBUG, "ChipGroup Sellers: Ningún chip seleccionado. Mostrando todos.");
            } else {
                int primerChipIdSeleccionado = checkedIds.get(0);
                Chip chipSeleccionado = findViewById(primerChipIdSeleccionado);

                if (chipSeleccionado != null) {
                    Object tag = chipSeleccionado.getTag();
                    filtroVendedorActualUID = (tag instanceof String) ? (String) tag : "Todos";
                    Log.d(ETIQUETA_DEBUG, "ChipGroup Sellers: Seleccionado chip con texto '" + chipSeleccionado.getText() + "', UID/Tag: " + filtroVendedorActualUID);
                } else {
                    filtroVendedorActualUID = "Todos";
                    Log.w(ETIQUETA_DEBUG, "ChipGroup Sellers: ID de chip seleccionado no encontrado. Mostrando todos.");
                }
            }
            aplicarFiltrosYOrdenamiento();
        });
    }

    private void cargarVendedoresParaFiltro() {
        mapaVendedores.clear(); // Limpiar antes de cargar
        listenerUsuarios = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mapaVendedores.put("Todos", "Todos los Vendedores"); // Opción para mostrar todos
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        Usuario usuario = userSnapshot.getValue(Usuario.class);
                        // Considerar solo roles 'seller' o 'admin' si los clientes solo pueden ser creados por ellos
                        if (usuario != null && (ConstantesApp.ROL_VENDEDOR.equals(usuario.getRol()) || ConstantesApp.ROL_ADMIN.equals(usuario.getRol()))) {
                            mapaVendedores.put(userSnapshot.getKey(), usuario.getNombreCompleto());
                        }
                    }
                }
                actualizarChipsVendedores();
                cargarClientes(); // Cargar clientes después de tener los vendedores
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar vendedores: ", databaseError.toException());
                actualizarChipsVendedores(); // Intentar actualizar con "Todos" al menos
                cargarClientes();
            }
        };
        // Cargar todos los usuarios para obtener los nombres de los vendedores.
        referenciaUsuariosBD.orderByChild("rol").addListenerForSingleValueEvent(listenerUsuarios);
    }

    private void actualizarChipsVendedores() {
        chipGroupSellers.removeAllViews();
        String vendedorSeleccionadoPreviamenteTag = filtroVendedorActualUID; // Guardar la selección
        boolean algunaSeleccionada = false;

        for (Map.Entry<String, String> entry : mapaVendedores.entrySet()) {
            String uidVendedor = entry.getKey();
            String nombreVendedor = entry.getValue();

            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_filtro_item, chipGroupSellers, false); // Reutilizar layout de chip
            chip.setText(nombreVendedor.equals("Todos los Vendedores") ? "Todos" : nombreVendedor);
            chip.setTag(uidVendedor); // Guardar el UID en el tag del chip
            chip.setOnClickListener(v -> { // Para re-chequear
                chipGroupSellers.check(chip.getId());
            });


            if (uidVendedor.equals(vendedorSeleccionadoPreviamenteTag)) {
                chip.setChecked(true);
                algunaSeleccionada = true;
            }
            chipGroupSellers.addView(chip);
        }

        if (!algunaSeleccionada && chipGroupSellers.getChildCount() > 0) {
            View primerChipView = chipGroupSellers.getChildAt(0);
            if (primerChipView instanceof Chip) {
                Chip primerChip = (Chip) primerChipView;
                if (primerChip.getTag() != null && primerChip.getTag().toString().equals("Todos")) {
                    chipGroupSellers.check(primerChip.getId());
                }
            }
        }
    }

    private void cargarClientes() {
        mostrarOverlay(true, "Cargando clientes...");
        listenerClientes = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaTodosLosClientes.clear();
                long clientesActivosCount = 0;
                long nuevosEsteMesCount = 0; // Necesitarías lógica para calcular esto basado en fechaRegistro

                if (dataSnapshot.exists()) {
                    for (DataSnapshot clienteSnapshot : dataSnapshot.getChildren()) {
                        Cliente cliente = clienteSnapshot.getValue(Cliente.class);
                        if (cliente != null) {
                            cliente.setId(clienteSnapshot.getKey());
                            listaTodosLosClientes.add(cliente);
                            if (cliente.isActivo()) {
                                clientesActivosCount++;
                            }
                            // TODO: Lógica para nuevosEsteMesCount comparando cliente.getFechaRegistro() con el mes actual
                        }
                    }
                }
                tvTotalClientes.setText(String.valueOf(listaTodosLosClientes.size()));
                tvClientesActivos.setText(String.valueOf(clientesActivosCount));
                // tvNuevosEsteMes.setText(String.valueOf(nuevosEsteMesCount));

                aplicarFiltrosYOrdenamiento();
                mostrarOverlay(false, null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar clientes: ", databaseError.toException());
                Toast.makeText(GestionarClientesActivity.this, "Error al cargar clientes.", Toast.LENGTH_SHORT).show();
                mostrarOverlay(false, null);
            }
        };
        referenciaClientesBD.addValueEventListener(listenerClientes); // Para actualizaciones en tiempo real
    }

    private void aplicarFiltrosYOrdenamiento() {
        listaClientesFiltrados.clear();
        List<Cliente> listaTemporal = new ArrayList<>();

        // 1. Filtrar por Vendedor
        if (filtroVendedorActualUID.equals("Todos")) {
            listaTemporal.addAll(listaTodosLosClientes);
        } else {
            for (Cliente cliente : listaTodosLosClientes) {
                if (cliente.getCreadoPor() != null && cliente.getCreadoPor().equals(filtroVendedorActualUID)) {
                    listaTemporal.add(cliente);
                }
            }
        }

        // 2. Filtrar por Término de Búsqueda
        if (!terminoBusquedaActual.isEmpty()) {
            List<Cliente> listaBuscada = new ArrayList<>();
            for (Cliente cliente : listaTemporal) {
                if ((cliente.getNombreCompleto() != null && cliente.getNombreCompleto().toLowerCase().contains(terminoBusquedaActual)) ||
                        (cliente.getTelefono() != null && cliente.getTelefono().toLowerCase().contains(terminoBusquedaActual)) ||
                        (cliente.getEmail() != null && cliente.getEmail().toLowerCase().contains(terminoBusquedaActual))) {
                    listaBuscada.add(cliente);
                }
            }
            listaTemporal.clear();
            listaTemporal.addAll(listaBuscada);
        }

        // 3. Aplicar Ordenamiento
        switch (ordenamientoActual) {
            case "Nombre (A-Z)":
                Collections.sort(listaTemporal, Comparator.comparing(Cliente::getNombreCompleto, Comparator.nullsLast(String::compareToIgnoreCase)));
                break;
            case "Nombre (Z-A)":
                Collections.sort(listaTemporal, Comparator.comparing(Cliente::getNombreCompleto, Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
                break;
            case "Fecha Registro (Nuevos)":
                Collections.sort(listaTemporal, Comparator.comparingLong(Cliente::getFechaRegistro).reversed());
                break;
            case "Fecha Registro (Antiguos)":
                Collections.sort(listaTemporal, Comparator.comparingLong(Cliente::getFechaRegistro));
                break;
        }

        listaClientesFiltrados.addAll(listaTemporal);
        clienteAdminAdapter.actualizarLista(listaClientesFiltrados);

        // Actualizar TextView de "XX clientes encontrados"
        TextView tvConteoResultados = findViewById(R.id.cvToolbar).findViewById(R.id.tvTituloActividad);


        // if (tvMensajeListaVacia != null) {
        //    tvMensajeListaVacia.setVisibility(listaClientesFiltrados.isEmpty() ? View.VISIBLE : View.GONE);
        // }
        rvClientes.setVisibility(listaClientesFiltrados.isEmpty() ? View.GONE : View.VISIBLE);

    }

    // Implementación de OnClienteAdminActionsListener
    @Override
    public void onClienteClick(Cliente cliente) {
        Toast.makeText(this, "Ver detalles de: " + cliente.getNombreCompleto(), Toast.LENGTH_SHORT).show();
        // Intent a una actividad de detalle de cliente
    }

    @Override
    public void onEditCliente(Cliente cliente) {
        Toast.makeText(this, "Editar: " + cliente.getNombreCompleto(), Toast.LENGTH_SHORT).show();
        // Intent a una actividad para editar el cliente, pasando el cliente.getId()
        // Intent intent = new Intent(this, AgregarEditarClienteActivity.class);
        // intent.putExtra("CLIENTE_ID", cliente.getId());
        // startActivity(intent);
    }

    @Override
    public void onDeleteCliente(Cliente cliente) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cliente")
                .setMessage("¿Estás seguro de que quieres eliminar a " + cliente.getNombreCompleto() + "? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    referenciaClientesBD.child(cliente.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(GestionarClientesActivity.this, "Cliente eliminado.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(GestionarClientesActivity.this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onVerHistorial(Cliente cliente) {
        Toast.makeText(this, "Ver historial de: " + cliente.getNombreCompleto(), Toast.LENGTH_SHORT).show();
        // Intent a una actividad de historial de compras del cliente
    }

    @Override
    public void onCrearReservaParaCliente(Cliente cliente) {
        Toast.makeText(this, "Crear reserva para: " + cliente.getNombreCompleto(), Toast.LENGTH_SHORT).show();
        // Intent a CrearReservaActivity, pasando el cliente.getId() o el objeto Cliente
        // Intent intent = new Intent(this, CrearReservaActivity.class);
        // intent.putExtra("CLIENTE_ID_PRESELECCIONADO", cliente.getId());
        // startActivity(intent);
    }

    private void mostrarOverlay(boolean mostrar, String mensaje) {
        if(mostrar && mensaje != null) Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show(); // Placeholder
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (referenciaClientesBD != null && listenerClientes != null) {
            referenciaClientesBD.removeEventListener(listenerClientes);
        }
        if (referenciaUsuariosBD != null && listenerUsuarios != null) {
            referenciaUsuariosBD.removeEventListener(listenerUsuarios);
        }
    }
}