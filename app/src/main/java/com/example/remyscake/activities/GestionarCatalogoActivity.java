package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.remyscake.R;
import com.example.remyscake.adapters.CatalogoAdapter;
import com.example.remyscake.models.Pastel;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet; // Para categorías únicas
import java.util.List;
import java.util.Set;    // Para categorías únicas

public class GestionarCatalogoActivity extends AppCompatActivity implements CatalogoAdapter.OnItemClickListener { // Implementa la interfaz

    private static final String ETIQUETA_DEBUG = "GestionCatalogo";

    private ImageButton btnBackCatalogo, btnSearchCatalogo;
    private TextView tvTotalProductos, tvTotalCategorias;
    private CardView cvAgregarProductoCardView;
    private ChipGroup chipGroupCategorias;
    private RecyclerView rvProductos;
    private FloatingActionButton fabAgregarProducto;

    private CatalogoAdapter catalogoAdapter;            // DESCOMENTADO
    private List<Pastel> listaTodosLosProductos = new ArrayList<>(); // DESCOMENTADO
    private List<Pastel> listaProductosFiltrados = new ArrayList<>();// DESCOMENTADO
    private List<String> listaCategorias = new ArrayList<>();     // DESCOMENTADO

    private DatabaseReference referenciaCatalogoBD;
    private ValueEventListener listenerCatalogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_catalogo);

        referenciaCatalogoBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CATALOGO_PASTELES);

        inicializarVistas();
        configurarRecyclerView(); // Ahora se configurará completamente
        establecerListeners();
        // cargarDatosCatalogo() se llamará en onResume o después de configurar listeners si es necesario
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosCatalogo(); // Cargar o recargar datos cuando la actividad se vuelve visible
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Eliminar el listener cuando la actividad no está visible para ahorrar recursos
        if (referenciaCatalogoBD != null && listenerCatalogo != null) {
            referenciaCatalogoBD.removeEventListener(listenerCatalogo);
        }
    }


    private void inicializarVistas() {
        btnBackCatalogo = findViewById(R.id.btnBackCatalogo);
        btnSearchCatalogo = findViewById(R.id.btnSearchCatalogo);
        tvTotalProductos = findViewById(R.id.tvTotalProductos);
        tvTotalCategorias = findViewById(R.id.tvTotalCategorias);
        cvAgregarProductoCardView = findViewById(R.id.cvAgregarProducto);
        chipGroupCategorias = findViewById(R.id.chipGroupCategorias);
        rvProductos = findViewById(R.id.rvProductos);
        fabAgregarProducto = findViewById(R.id.fabAgregarProducto);

        tvTotalProductos.setText("0");
        tvTotalCategorias.setText("0");
    }

    private void configurarRecyclerView() {
        rvProductos.setLayoutManager(new LinearLayoutManager(this));
        listaProductosFiltrados = new ArrayList<>(); // Asegura que la lista no sea nula
        catalogoAdapter = new CatalogoAdapter(listaProductosFiltrados, this, this); // Pasando el listener
        rvProductos.setAdapter(catalogoAdapter);
    }

    private void establecerListeners() {
        btnBackCatalogo.setOnClickListener(v -> onBackPressed());

        btnSearchCatalogo.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad de Búsqueda (Próximamente)", Toast.LENGTH_SHORT).show();
        });

        View.OnClickListener listenerIrAAgregarProducto = v -> {
            Intent intent = new Intent(GestionarCatalogoActivity.this, AgregarEditarProductoActivity.class);
            startActivity(intent);
        };
        cvAgregarProductoCardView.setOnClickListener(listenerIrAAgregarProducto);
        fabAgregarProducto.setOnClickListener(listenerIrAAgregarProducto);

        chipGroupCategorias.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Si es posible deseleccionar todos (singleSelection="false"), mostrar todos
                filtrarProductosPorCategoria("Todos");
            } else {
                // Asumiendo singleSelection="true" o tomamos el primer ID si hay varios
                Chip chipSeleccionado = findViewById(checkedIds.get(0));
                if (chipSeleccionado != null) {
                    filtrarProductosPorCategoria(chipSeleccionado.getText().toString());
                } else { // Si el ID no corresponde a un chip (raro, pero por si acaso)
                    filtrarProductosPorCategoria("Todos");
                }
            }
        });
    }

    private void cargarDatosCatalogo() {
        Toast.makeText(this, "Cargando catálogo...", Toast.LENGTH_SHORT).show();
        if (listenerCatalogo != null) { // Remover listener anterior si existe para evitar duplicados
            referenciaCatalogoBD.removeEventListener(listenerCatalogo);
        }

        listenerCatalogo = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaTodosLosProductos.clear();
                Set<String> categoriasUnicas = new HashSet<>(); // Usar Set para evitar duplicados

                if (dataSnapshot.exists()) {
                    for (DataSnapshot productoSnapshot : dataSnapshot.getChildren()) {
                        Pastel pastel = productoSnapshot.getValue(Pastel.class);
                        if (pastel != null) {
                            pastel.setId(productoSnapshot.getKey()); // Guardar el ID de Firebase
                            listaTodosLosProductos.add(pastel);

                            if (pastel.getCategoria() != null && !pastel.getCategoria().isEmpty()) {
                                categoriasUnicas.add(pastel.getCategoria());
                            }
                        }
                    }
                }

                // Convertir Set a List y ordenar para los chips
                listaCategorias.clear();
                listaCategorias.add("Todos"); // Opción por defecto
                List<String> sortedCategorias = new ArrayList<>(categoriasUnicas);
                Collections.sort(sortedCategorias); // Ordenar alfabéticamente
                listaCategorias.addAll(sortedCategorias);


                tvTotalProductos.setText(String.valueOf(listaTodosLosProductos.size()));
                tvTotalCategorias.setText(String.valueOf(categoriasUnicas.size())); // Conteo real de categorías

                actualizarChipsCategorias();

                // Aplicar filtro inicial (ej. "Todos" o el primer chip seleccionado si ya hay uno)
                int checkedChipId = chipGroupCategorias.getCheckedChipId();
                if (checkedChipId != View.NO_ID) {
                    Chip chipInicial = findViewById(checkedChipId);
                    if (chipInicial != null) {
                        filtrarProductosPorCategoria(chipInicial.getText().toString());
                    } else {
                        filtrarProductosPorCategoria("Todos"); // Fallback
                    }
                } else {
                    // Si no hay nada chequeado, seleccionar y filtrar por "Todos"
                    if (!listaCategorias.isEmpty() && chipGroupCategorias.getChildCount() > 0) {
                        Chip primerChip = (Chip) chipGroupCategorias.getChildAt(0); // Asumir que "Todos" es el primero
                        if (primerChip != null && primerChip.getText().toString().equals("Todos")) {
                            primerChip.setChecked(true); // Esto debería disparar el listener y filtrar
                        } else {
                            filtrarProductosPorCategoria("Todos");
                        }
                    } else {
                        filtrarProductosPorCategoria("Todos");
                    }
                }

                if (listaTodosLosProductos.isEmpty()) {
                    Toast.makeText(GestionarCatalogoActivity.this, "El catálogo está vacío.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar catálogo: ", databaseError.toException());
                Toast.makeText(GestionarCatalogoActivity.this, "Error al cargar datos.", Toast.LENGTH_LONG).show();
                tvTotalProductos.setText("-");
                tvTotalCategorias.setText("-");
            }
        };
        // Usar addListenerForSingleValueEvent si solo quieres cargar una vez al entrar
        // o addValueEventListener para actualizaciones en tiempo real.
        // Para un catálogo, single event suele ser suficiente a menos que esperes cambios frecuentes de otros admins.
        referenciaCatalogoBD.addListenerForSingleValueEvent(listenerCatalogo);
    }

    private void actualizarChipsCategorias() {
        chipGroupCategorias.removeAllViews(); // Limpiar chips anteriores

        for (String categoria : listaCategorias) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_filtro_item, chipGroupCategorias, false); // Necesitas un layout para el chip
            // O crear programáticamente:
            // Chip chip = new Chip(this);
            chip.setText(categoria);
            // chip.setCheckable(true); // Ya está en el estilo de chip_filtro_item
            // chip.setClickable(true); // Ya está en el estilo de chip_filtro_item

            // Si "Todos" es el primer chip y no hay selección previa, marcarlo
            if (categoria.equals("Todos") && chipGroupCategorias.getCheckedChipId() == View.NO_ID) {
                chip.setChecked(true);
            }
            chipGroupCategorias.addView(chip);
        }
        chipGroupCategorias.setVisibility(listaCategorias.size() > 1 ? View.VISIBLE : View.GONE); // Ocultar si solo está "Todos"
    }

    private void filtrarProductosPorCategoria(String categoria) {
        listaProductosFiltrados.clear();
        if (categoria == null || categoria.equalsIgnoreCase("Todos")) {
            listaProductosFiltrados.addAll(listaTodosLosProductos);
        } else {
            for (Pastel pastel : listaTodosLosProductos) {
                if (pastel.getCategoria() != null && pastel.getCategoria().equalsIgnoreCase(categoria)) {
                    listaProductosFiltrados.add(pastel);
                }
            }
        }

        // Opcional: Ordenar listaProductosFiltrados si se desea
        // Collections.sort(listaProductosFiltrados, Comparator.comparing(Pastel::getNombre));

        if (catalogoAdapter != null) {
            catalogoAdapter.actualizarLista(listaProductosFiltrados); // Usa el método del adaptador
        } else {
            Log.w(ETIQUETA_DEBUG, "El adaptador del catálogo es nulo al intentar filtrar.");
        }

        // Actualizar UI si la lista filtrada está vacía
        if (listaProductosFiltrados.isEmpty() && !categoria.equalsIgnoreCase("Todos")) {
            Toast.makeText(this, "No hay productos en la categoría: " + categoria, Toast.LENGTH_SHORT).show();
            // rvProductos.setVisibility(View.GONE);
            // Podrías mostrar un TextView indicando que no hay productos.
        } else if (listaProductosFiltrados.isEmpty() && categoria.equalsIgnoreCase("Todos") && !listaTodosLosProductos.isEmpty()){
            // Esto no debería pasar si "Todos" muestra listaTodosLosProductos
        }
        else {
            // rvProductos.setVisibility(View.VISIBLE);
        }
    }

    // Implementación de la interfaz del adaptador
    @Override
    public void onItemClick(Pastel pastel) {
        // Podrías abrir una vista de detalle del producto o directamente editar
        Toast.makeText(this, "Ver detalle de: " + pastel.getNombre(), Toast.LENGTH_SHORT).show();
        onEditClick(pastel); // O llamar a editar directamente
    }

    @Override
    public void onEditClick(Pastel pastel) {
        Intent intent = new Intent(GestionarCatalogoActivity.this, AgregarEditarProductoActivity.class);
        intent.putExtra("ID_PRODUCTO", pastel.getId());
        startActivity(intent);
    }

    // No es necesario onDestroy() si usas addListenerForSingleValueEvent.
    // Si cambias a addValueEventListener, descomenta onDestroy() o usa onPause()/onResume().
    // @Override
    // protected void onDestroy() {
    //    super.onDestroy();
    //    if (referenciaCatalogoBD != null && listenerCatalogo != null) {
    //        referenciaCatalogoBD.removeEventListener(listenerCatalogo);
    //    }
    // }
}