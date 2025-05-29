package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Para el diálogo de cantidad
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType; // Para el EditText del diálogo
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater; // Para inflar el diálogo
import android.view.View;
import android.widget.EditText; // Para el diálogo de cantidad
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.adapters.CatalogoSeleccionAdapter;
import com.example.remyscake.models.Categoria;
import com.example.remyscake.models.Pastel;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale; // Para la búsqueda
import java.util.Set;

public class SeleccionarProductoActivity extends AppCompatActivity implements CatalogoSeleccionAdapter.OnProductoSeleccionadoListener {

    private static final String ETIQUETA_DEBUG = "SelectProducto";

    private ImageButton btnBackSeleccionarProducto;
    private SearchView searchViewProductosCatalogo;
    private ChipGroup chipGroupCategoriasSeleccion;
    private RecyclerView rvProductosCatalogoSeleccion;
    private TextView tvSinProductosCatalogo;

    private CatalogoSeleccionAdapter adapter;
    private List<Pastel> listaTodosLosProductos = new ArrayList<>();
    private List<Pastel> listaProductosFiltrados = new ArrayList<>();
    private List<String> nombresCategoriasParaChips = new ArrayList<>();

    private DatabaseReference referenciaCatalogoBD;
    private DatabaseReference referenciaCategoriasBD;
    private ValueEventListener listenerCatalogo;
    private ValueEventListener listenerCategorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_producto);
        setTitle("Seleccionar Producto");

        referenciaCatalogoBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CATALOGO_PASTELES);
        referenciaCategoriasBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CATEGORIAS_CATALOGO);

        inicializarVistas();
        configurarRecyclerView();
        configurarSearchView();
        establecerListenersChips(); // Configurar listener de chips aquí
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCategoriasParaChips(); // Carga categorías y luego productos
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (referenciaCatalogoBD != null && listenerCatalogo != null) {
            referenciaCatalogoBD.removeEventListener(listenerCatalogo);
        }
        if (referenciaCategoriasBD != null && listenerCategorias != null) {
            referenciaCategoriasBD.removeEventListener(listenerCategorias);
        }
    }

    private void inicializarVistas() {
        btnBackSeleccionarProducto = findViewById(R.id.btnBackSeleccionarProducto);
        searchViewProductosCatalogo = findViewById(R.id.searchViewProductosCatalogo);
        chipGroupCategoriasSeleccion = findViewById(R.id.chipGroupCategoriasSeleccion);
        rvProductosCatalogoSeleccion = findViewById(R.id.rvProductosCatalogoSeleccion);
        tvSinProductosCatalogo = findViewById(R.id.tvSinProductosCatalogo);

        btnBackSeleccionarProducto.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED); // Indicar que no se seleccionó nada
            finish();
        });
    }

    private void configurarRecyclerView() {
        rvProductosCatalogoSeleccion.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CatalogoSeleccionAdapter(listaProductosFiltrados, this, this);
        rvProductosCatalogoSeleccion.setAdapter(adapter);
    }

    private void configurarSearchView() {
        searchViewProductosCatalogo.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarProductos(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarProductos(newText);
                return false;
            }
        });
    }

    private void establecerListenersChips() {
        chipGroupCategoriasSeleccion.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, int checkedId) {
                // El parámetro 'checkedId' aquí SÍ es un int
                if (checkedId == View.NO_ID) {
                    Log.d(ETIQUETA_DEBUG, "ChipGroup: No hay chip seleccionado (NO_ID).");
                    filtrarProductos(searchViewProductosCatalogo.getQuery().toString()); // Re-filtra con el texto de búsqueda actual
                } else {
                    Chip chipSeleccionado = group.findViewById(checkedId); // Ahora checkedId es un int
                    if (chipSeleccionado != null) {
                        Log.d(ETIQUETA_DEBUG, "ChipGroup: Chip seleccionado -> " + chipSeleccionado.getText());
                        filtrarProductos(searchViewProductosCatalogo.getQuery().toString()); // Re-filtra
                    } else {
                        Log.d(ETIQUETA_DEBUG, "ChipGroup: Chip con ID " + checkedId + " no encontrado, mostrando con filtro de búsqueda.");
                        filtrarProductos(searchViewProductosCatalogo.getQuery().toString());
                    }
                }
            }
        });
    }



    private void cargarCategoriasParaChips() {
        // tvSinProductosCatalogo.setText("Cargando categorías...");
        // tvSinProductosCatalogo.setVisibility(View.VISIBLE);
        // rvProductosCatalogoSeleccion.setVisibility(View.GONE);

        if (listenerCategorias != null) {
            referenciaCategoriasBD.removeEventListener(listenerCategorias);
        }
        listenerCategorias = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nombresCategoriasParaChips.clear();
                nombresCategoriasParaChips.add("Todos");

                if (dataSnapshot.exists()) {
                    for (DataSnapshot catSnapshot : dataSnapshot.getChildren()) {
                        Categoria categoria = catSnapshot.getValue(Categoria.class);
                        if (categoria != null && categoria.getNombre() != null) {
                            nombresCategoriasParaChips.add(categoria.getNombre());
                        }
                    }
                }
                if (nombresCategoriasParaChips.size() > 1) { // Más que solo "Todos"
                    List<String> tempSort = new ArrayList<>(nombresCategoriasParaChips.subList(1, nombresCategoriasParaChips.size()));
                    Collections.sort(tempSort);
                    nombresCategoriasParaChips = new ArrayList<>(nombresCategoriasParaChips.subList(0,1));
                    nombresCategoriasParaChips.addAll(tempSort);
                }
                actualizarChipsCategoriasUI();
                cargarDatosCatalogo(); // Cargar productos después de tener las categorías
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar categorías para chips: ", databaseError.toException());
                actualizarChipsCategoriasUI(); // Mostrar "Todos" al menos
                cargarDatosCatalogo();
            }
        };
        referenciaCategoriasBD.addValueEventListener(listenerCategorias);
    }

    private void actualizarChipsCategoriasUI() {
        String categoriaSeleccionadaPreviamenteNombre = null;
        int checkedChipIdActual = chipGroupCategoriasSeleccion.getCheckedChipId(); // Obtener el ID del chip actualmente chequeado
        if(checkedChipIdActual != View.NO_ID){
            Chip chipActualmenteSeleccionado = findViewById(checkedChipIdActual);
            if(chipActualmenteSeleccionado != null){
                categoriaSeleccionadaPreviamenteNombre = chipActualmenteSeleccionado.getText().toString();
            }
        }

        chipGroupCategoriasSeleccion.removeAllViews();
        boolean seleccionRestaurada = false;

        for (String nombreCategoria : nombresCategoriasParaChips) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_filtro_item, chipGroupCategoriasSeleccion, false);
            chip.setText(nombreCategoria);
            // No es necesario un OnClickListener aquí si usamos el listener del ChipGroup
            // y singleSelection="true"

            if (nombreCategoria.equals(categoriaSeleccionadaPreviamenteNombre)) {
                chip.setChecked(true); // Esto no disparará el listener del ChipGroup inmediatamente
                seleccionRestaurada = true;
            }
            chipGroupCategoriasSeleccion.addView(chip);
        }

        if (!seleccionRestaurada && chipGroupCategoriasSeleccion.getChildCount() > 0) {
            View primerChipView = chipGroupCategoriasSeleccion.getChildAt(0);
            if (primerChipView instanceof Chip) {
                Chip primerChip = (Chip) primerChipView;
                if (primerChip.getText().toString().equals("Todos")) {
                    // Para singleSelection="true", llamar a check() es la forma de seleccionar
                    // y esto SÍ disparará el OnCheckedChangeListener del ChipGroup.
                    chipGroupCategoriasSeleccion.check(primerChip.getId());
                }
            }
        }
        chipGroupCategoriasSeleccion.setVisibility(nombresCategoriasParaChips.size() > 0 ? View.VISIBLE : View.GONE);
    }


    private void cargarDatosCatalogo() {
        tvSinProductosCatalogo.setText("Cargando productos...");
        tvSinProductosCatalogo.setVisibility(View.VISIBLE);
        rvProductosCatalogoSeleccion.setVisibility(View.GONE);

        if (listenerCatalogo != null) {
            referenciaCatalogoBD.removeEventListener(listenerCatalogo);
        }
        listenerCatalogo = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaTodosLosProductos.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot productoSnapshot : dataSnapshot.getChildren()) {
                        Pastel pastel = productoSnapshot.getValue(Pastel.class);
                        if (pastel != null && pastel.isDisponible()) { // Solo mostrar productos disponibles
                            pastel.setId(productoSnapshot.getKey());
                            listaTodosLosProductos.add(pastel);
                        }
                    }
                }
                // Ordenar alfabéticamente por nombre por defecto
                Collections.sort(listaTodosLosProductos, Comparator.comparing(Pastel::getNombre, String.CASE_INSENSITIVE_ORDER));
                filtrarProductos(searchViewProductosCatalogo.getQuery().toString()); // Aplicar filtro actual
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar catálogo: ", databaseError.toException());
                Toast.makeText(SeleccionarProductoActivity.this, "Error al cargar productos.", Toast.LENGTH_SHORT).show();
                tvSinProductosCatalogo.setText("Error al cargar productos.");
            }
        };
        referenciaCatalogoBD.addValueEventListener(listenerCatalogo);
    }

    private void filtrarProductos(String textoBusqueda) {
        listaProductosFiltrados.clear();
        String categoriaFiltrada = "Todos"; // Por defecto

        int idChipSeleccionado = chipGroupCategoriasSeleccion.getCheckedChipId();
        if (idChipSeleccionado != View.NO_ID) {
            Chip chip = chipGroupCategoriasSeleccion.findViewById(idChipSeleccionado);
            if (chip != null) {
                categoriaFiltrada = chip.getText().toString();
            }
        }
        Log.d(ETIQUETA_DEBUG, "Filtrando con categoría: " + categoriaFiltrada + " y búsqueda: " + textoBusqueda);


        for (Pastel pastel : listaTodosLosProductos) {
            boolean coincideCategoria = categoriaFiltrada.equalsIgnoreCase("Todos") ||
                    (pastel.getCategoria() != null && pastel.getCategoria().equalsIgnoreCase(categoriaFiltrada));

            boolean coincideBusqueda = TextUtils.isEmpty(textoBusqueda) ||
                    (pastel.getNombre() != null && pastel.getNombre().toLowerCase(Locale.getDefault()).contains(textoBusqueda.toLowerCase(Locale.getDefault())));

            if (coincideCategoria && coincideBusqueda) {
                listaProductosFiltrados.add(pastel);
            }
        }

        if (adapter != null) {
            adapter.actualizarLista(listaProductosFiltrados);
        }

        if (listaProductosFiltrados.isEmpty()) {
            tvSinProductosCatalogo.setText("No se encontraron productos que coincidan.");
            tvSinProductosCatalogo.setVisibility(View.VISIBLE);
            rvProductosCatalogoSeleccion.setVisibility(View.GONE);
        } else {
            tvSinProductosCatalogo.setVisibility(View.GONE);
            rvProductosCatalogoSeleccion.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onProductoSeleccionado(Pastel pastel) {
        // Mostrar diálogo para ingresar cantidad
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ingresar Cantidad para: " + pastel.getNombre());

        // Configurar el input
        final EditText inputCantidad = new EditText(this);
        inputCantidad.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputCantidad.setHint("Cantidad (ej: 1)");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(40, 20, 40, 20); // Añadir márgenes
        inputCantidad.setLayoutParams(lp);
        builder.setView(inputCantidad);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String cantidadStr = inputCantidad.getText().toString();
            if (TextUtils.isEmpty(cantidadStr)) {
                Toast.makeText(this, "Por favor, ingrese una cantidad.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int cantidad = Integer.parseInt(cantidadStr);
                if (cantidad <= 0) {
                    Toast.makeText(this, "La cantidad debe ser mayor a cero.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Devolver el producto y la cantidad a CrearReservaActivity
                Intent resultadoIntent = new Intent();
                resultadoIntent.putExtra("PRODUCTO_SELECCIONADO", pastel);
                resultadoIntent.putExtra("CANTIDAD_SELECCIONADA", cantidad);
                setResult(Activity.RESULT_OK, resultadoIntent);
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Cantidad inválida.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}