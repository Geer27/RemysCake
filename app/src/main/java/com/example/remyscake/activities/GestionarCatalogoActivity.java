package com.example.remyscake.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.adapters.CatalogoAdapter;
import com.example.remyscake.models.Categoria;
import com.example.remyscake.models.Pastel;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.Set;

public class GestionarCatalogoActivity extends AppCompatActivity implements CatalogoAdapter.OnItemClickListener {

    private static final String ETIQUETA_DEBUG = "GestionCatalogo";

    private ImageButton btnBackCatalogo, btnSearchCatalogo, btnAdministrarCategorias; // btnAdministrarCategorias añadido
    private TextView tvTotalProductos, tvTotalCategorias, tvSinProductos; // tvSinProductos añadido
    private CardView cvAgregarProductoCardView;
    private ChipGroup chipGroupCategorias;
    private RecyclerView rvProductos;
    private FloatingActionButton fabAgregarProducto;

    private CatalogoAdapter catalogoAdapter;
    private List<Pastel> listaTodosLosProductos = new ArrayList<>();
    private List<Pastel> listaProductosFiltrados = new ArrayList<>();
    private List<String> nombresCategoriasParaChips = new ArrayList<>(); // Usaremos solo nombres para los chips

    private DatabaseReference referenciaCatalogoBD;
    private DatabaseReference referenciaCategoriasBD; // Referencia para categorías
    private ValueEventListener listenerCatalogo;
    private ValueEventListener listenerCategorias;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_catalogo);

        referenciaCatalogoBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CATALOGO_PASTELES);
        referenciaCategoriasBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CATEGORIAS_CATALOGO);

        inicializarVistas();
        configurarRecyclerView();
        establecerListeners();
        // La carga de datos se iniciará desde onResume -> cargarCategoriasParaChips -> cargarDatosCatalogo
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCategoriasParaChips(); // Cargar primero las categorías para los filtros
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
        btnBackCatalogo = findViewById(R.id.btnBackCatalogo);
        btnSearchCatalogo = findViewById(R.id.btnSearchCatalogo);
        btnAdministrarCategorias = findViewById(R.id.btnAdministrarCategorias); // Vinculación del nuevo botón
        tvTotalProductos = findViewById(R.id.tvTotalProductos);
        tvTotalCategorias = findViewById(R.id.tvTotalCategorias);
        cvAgregarProductoCardView = findViewById(R.id.cvAgregarProducto);
        chipGroupCategorias = findViewById(R.id.chipGroupCategorias);
        rvProductos = findViewById(R.id.rvProductos);
        fabAgregarProducto = findViewById(R.id.fabAgregarProducto);
        tvSinProductos = findViewById(R.id.tvSinProductos); // Vinculación del TextView

        tvTotalProductos.setText("0");
        tvTotalCategorias.setText("0");
    }

    private void configurarRecyclerView() {
        rvProductos.setLayoutManager(new LinearLayoutManager(this));
        listaProductosFiltrados = new ArrayList<>();
        catalogoAdapter = new CatalogoAdapter(listaProductosFiltrados, this, this);
        rvProductos.setAdapter(catalogoAdapter);
    }

    private void establecerListeners() {
        btnBackCatalogo.setOnClickListener(v -> onBackPressed());

        btnSearchCatalogo.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad de Búsqueda (Próximamente)", Toast.LENGTH_SHORT).show();
        });

        btnAdministrarCategorias.setOnClickListener(v -> {
            mostrarDialogoAgregarEditarCategoria(null, null); // null para agregar nueva
        });

        View.OnClickListener listenerIrAAgregarProducto = v -> {
            Intent intent = new Intent(GestionarCatalogoActivity.this, AgregarEditarProductoActivity.class);
            startActivity(intent);
        };
        cvAgregarProductoCardView.setOnClickListener(listenerIrAAgregarProducto);
        fabAgregarProducto.setOnClickListener(listenerIrAAgregarProducto);

        chipGroupCategorias.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.get(0) == View.NO_ID) {
                filtrarProductosPorCategoria("Todos");
            } else {
                Chip chipSeleccionado = findViewById(checkedIds.get(0));
                if (chipSeleccionado != null) {
                    filtrarProductosPorCategoria(chipSeleccionado.getText().toString());
                } else {
                    filtrarProductosPorCategoria("Todos");
                }
            }
        });
    }

    private void cargarCategoriasParaChips() {
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
                        if (categoria != null && categoria.getNombre() != null && !categoria.getNombre().isEmpty()) {
                            nombresCategoriasParaChips.add(categoria.getNombre());
                        }
                    }
                }
                if (nombresCategoriasParaChips.size() > 1) {
                    Set<String> setUnico = new HashSet<>(nombresCategoriasParaChips.subList(1, nombresCategoriasParaChips.size()));
                    List<String> tempSort = new ArrayList<>(setUnico);
                    Collections.sort(tempSort);
                    nombresCategoriasParaChips = new ArrayList<>();
                    nombresCategoriasParaChips.add("Todos");
                    nombresCategoriasParaChips.addAll(tempSort);
                }


                actualizarChipsCategoriasUI();
                tvTotalCategorias.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                cargarDatosCatalogo(); // Cargar productos después de tener las categorías para los filtros
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar categorías: ", databaseError.toException());
                actualizarChipsCategoriasUI(); // Intentar mostrar "Todos" al menos
                cargarDatosCatalogo();
            }
        };
        referenciaCategoriasBD.addValueEventListener(listenerCategorias);
    }

    private void actualizarChipsCategoriasUI() {
        String categoriaSeleccionadaPreviamente = null;
        int checkedChipId = chipGroupCategorias.getCheckedChipId();
        if(checkedChipId != View.NO_ID){
            Chip chipActualmenteSeleccionado = findViewById(checkedChipId);
            if(chipActualmenteSeleccionado != null){
                categoriaSeleccionadaPreviamente = chipActualmenteSeleccionado.getText().toString();
            }
        }

        chipGroupCategorias.removeAllViews();
        boolean algunaRestaurada = false;

        for (String nombreCategoria : nombresCategoriasParaChips) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_filtro_item, chipGroupCategorias, false);
            chip.setText(nombreCategoria);

            if (nombreCategoria.equals(categoriaSeleccionadaPreviamente)) {
                chip.setChecked(true);
                algunaRestaurada = true;
            }
            chipGroupCategorias.addView(chip);
        }

        if (!algunaRestaurada && chipGroupCategorias.getChildCount() > 0) {
            View primerChipView = chipGroupCategorias.getChildAt(0);
            if (primerChipView instanceof Chip) {
                Chip primerChip = (Chip) primerChipView;
                if (primerChip.getText().toString().equals("Todos")) {
                    chipGroupCategorias.check(primerChip.getId()); // Dispara el listener del grupo
                }
            }
        } else if (algunaRestaurada) {
            // Si se restauró una selección, forzar el filtrado inicial con esa selección
            filtrarProductosPorCategoria(categoriaSeleccionadaPreviamente);
        }


        chipGroupCategorias.setVisibility(nombresCategoriasParaChips.size() > 0 ? View.VISIBLE : View.GONE);
    }


    private void cargarDatosCatalogo() {
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
                        if (pastel != null) {
                            pastel.setId(productoSnapshot.getKey());
                            listaTodosLosProductos.add(pastel);
                        }
                    }
                }
                tvTotalProductos.setText(String.valueOf(listaTodosLosProductos.size()));

                int checkedChipId = chipGroupCategorias.getCheckedChipId();
                String categoriaAFiltrar = "Todos";
                if (checkedChipId != View.NO_ID) {
                    Chip chipActual = findViewById(checkedChipId);
                    if (chipActual != null) {
                        categoriaAFiltrar = chipActual.getText().toString();
                    }
                }
                filtrarProductosPorCategoria(categoriaAFiltrar);

                if (listaTodosLosProductos.isEmpty() && chipGroupCategorias.getCheckedChipId() == View.NO_ID) {
                    // Este caso es cuando no hay productos
                    tvSinProductos.setText("El catálogo está vacío. ¡Agrega productos!");
                    tvSinProductos.setVisibility(View.VISIBLE);
                    rvProductos.setVisibility(View.GONE);
                } else if (listaTodosLosProductos.isEmpty()){
                    // Si hay filtros pero la lista general está vacía
                    tvSinProductos.setText("El catálogo está vacío. ¡Agrega productos!");
                    tvSinProductos.setVisibility(View.VISIBLE);
                    rvProductos.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar catálogo: ", databaseError.toException());
                Toast.makeText(GestionarCatalogoActivity.this, "Error al cargar productos.", Toast.LENGTH_SHORT).show();
                tvTotalProductos.setText("-");
                listaProductosFiltrados.clear();
                if(catalogoAdapter != null) catalogoAdapter.actualizarLista(listaProductosFiltrados);
                tvSinProductos.setText("Error al cargar productos.");
                tvSinProductos.setVisibility(View.VISIBLE);
                rvProductos.setVisibility(View.GONE);
            }
        };
        referenciaCatalogoBD.addValueEventListener(listenerCatalogo);
    }


    private void filtrarProductosPorCategoria(String categoria) {
        Log.d(ETIQUETA_DEBUG, "Filtrando por: " + categoria);
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
        Collections.sort(listaProductosFiltrados, Comparator.comparing(Pastel::getNombre));

        if (catalogoAdapter != null) {
            catalogoAdapter.actualizarLista(listaProductosFiltrados);
        }

        if (listaProductosFiltrados.isEmpty()) {
            rvProductos.setVisibility(View.GONE);
            tvSinProductos.setVisibility(View.VISIBLE);
            if (categoria.equalsIgnoreCase("Todos") && listaTodosLosProductos.isEmpty()) {
                tvSinProductos.setText("El catálogo está vacío. ¡Agrega productos!");
            } else if (categoria.equalsIgnoreCase("Todos") && !listaTodosLosProductos.isEmpty()){
                tvSinProductos.setText("El catálogo está vacío. ¡Agrega productos!");
            }
            else {
                tvSinProductos.setText("No hay productos en la categoría: " + categoria);
            }
        } else {
            rvProductos.setVisibility(View.VISIBLE);
            tvSinProductos.setVisibility(View.GONE);
        }
    }

    private void mostrarDialogoAgregarEditarCategoria(String idCategoriaExistente, Categoria categoriaExistente) {
        MaterialAlertDialogBuilder dialogo = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View vistaDialogo = inflater.inflate(R.layout.dialog_agregar_editar_categoria, null);
        dialogo.setView(vistaDialogo);

        TextInputEditText etNombreCat = vistaDialogo.findViewById(R.id.etNombreCategoriaDialog);
        TextInputEditText etDescCat = vistaDialogo.findViewById(R.id.etDescCategoriaDialog);

        if (categoriaExistente != null) {
            dialogo.setTitle("Editar Categoría"); // Implementar lógica de edición si es necesario
            etNombreCat.setText(categoriaExistente.getNombre());
            etDescCat.setText(categoriaExistente.getDescripcion());
            Toast.makeText(this, "Funcionalidad de Editar Categoría (Próximamente)", Toast.LENGTH_SHORT).show();
            // Por ahora, el diálogo solo agrega. Para editar, necesitarías pasar el ID y actualizar.
        } else {
            dialogo.setTitle("Agregar Nueva Categoría");
        }

        dialogo.setPositiveButton(categoriaExistente != null ? "Actualizar" : "Agregar", (d, which) -> {
            String nombre = etNombreCat.getText().toString().trim();
            String desc = etDescCat.getText().toString().trim();

            if (TextUtils.isEmpty(nombre)) {
                Toast.makeText(this, "El nombre de la categoría es requerido.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Verificar si la categoría ya existe (opcional pero recomendado)
            if (nombresCategoriasParaChips.contains(nombre) && idCategoriaExistente == null) { // Solo para nuevas
                Toast.makeText(this, "La categoría '" + nombre + "' ya existe.", Toast.LENGTH_SHORT).show();
                return;
            }


            DatabaseReference refParaGuardar;
            String idFinalCategoria;

            if (idCategoriaExistente != null) { // Lógica de edición
                // idFinalCategoria = idCategoriaExistente;
                // refParaGuardar = referenciaCategoriasBD.child(idFinalCategoria);
                Toast.makeText(this, "Edición de categoría no implementada en este diálogo.", Toast.LENGTH_SHORT).show();
                return; // Salir si es intento de edición no soportado por este simple diálogo
            } else { // Lógica de agregar
                idFinalCategoria = referenciaCategoriasBD.push().getKey();
                if (idFinalCategoria == null) {
                    Toast.makeText(this, "Error al generar ID para categoría.", Toast.LENGTH_SHORT).show();
                    return;
                }
                refParaGuardar = referenciaCategoriasBD.child(idFinalCategoria);
            }

            Categoria nuevaCategoria = new Categoria(nombre, desc);
            refParaGuardar.setValue(nuevaCategoria)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Categoría '" + nombre + "' agregada.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        dialogo.setNegativeButton("Cancelar", (d, which) -> d.dismiss());
        dialogo.show();
    }


    @Override
    public void onItemClick(Pastel pastel) {
        onEditClick(pastel);
    }

    @Override
    public void onEditClick(Pastel pastel) {
        Intent intent = new Intent(GestionarCatalogoActivity.this, AgregarEditarProductoActivity.class);
        intent.putExtra("ID_PRODUCTO", pastel.getId());
        startActivity(intent);
    }
}