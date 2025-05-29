package com.example.remyscake.activities;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat; // Para getColor
import androidx.core.content.FileProvider;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog; // Para el diálogo de salida
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.remyscake.R;
import com.example.remyscake.models.Pastel;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AgregarEditarProductoActivity extends AppCompatActivity {

    private static final String ETIQUETA_DEBUG = "AddEditProducto";
    // Códigos de solicitud de permiso
    // private static final int CODIGO_PERMISO_CAMARA = 101;
    // private static final int CODIGO_PERMISO_ALMACENAMIENTO = 102;


    // Vistas
    private ImageButton btnBackProducto, btnGuardarRapido;
    private TextView tvTituloActividad, tvSubtituloActividad, tvProgressMessage;
    private CardView cvImagenProducto;
    private ImageView ivImagenProducto;
    private LinearLayout llOverlayImagen;
    private ProgressBar pbCargandoImagen;
    private FrameLayout progressOverlay;
    private MaterialButton btnSeleccionarGaleria, btnTomarFoto, btnCancelarProducto, btnGuardarProducto;
    private TextInputLayout tilNombreProducto, tilDescripcionProducto, tilCategoriaProducto, tilPrecioProducto, tilIngredientesEspeciales, tilNotasInternas;
    private TextInputEditText etNombreProducto, etDescripcionProducto, etPrecioProducto, etIngredientesEspeciales, etNotasInternas;
    private AutoCompleteTextView actvCategoriaProducto;
    private MaterialSwitch swDisponibilidadProducto;


    // Firebase
    private DatabaseReference referenciaCatalogoBD;
    private StorageReference referenciaAlmacenamientoImagenes;

    // Estado
    private String idProductoExistente = null;
    private Pastel pastelExistente = null;
    private Uri uriImagenSeleccionada = null;
    private String urlImagenSubidaOriginal = null;
    private String urlImagenParaGuardar = null; // URL final de la imagen a guardar en DB
    private boolean hayCambiosSinGuardar = false;
    private String rutaFotoCamara;

    // Launchers
    private ActivityResultLauncher<String> selectorGaleriaLauncher;
    private ActivityResultLauncher<Uri> tomadorFotoLauncher;
    private ActivityResultLauncher<String[]> solicitadorPermisosMultiplesLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_editar_producto);

        referenciaCatalogoBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_CATALOGO_PASTELES);
        referenciaAlmacenamientoImagenes = FirebaseStorage.getInstance().getReference().child("imagenes_catalogo");

        inicializarVistas();
        configurarLaunchers();
        configurarListeners();
        cargarCategoriasDesdeRecursos();

        if (getIntent().hasExtra("ID_PRODUCTO")) {
            idProductoExistente = getIntent().getStringExtra("ID_PRODUCTO");
            tvTituloActividad.setText("Editar Producto");
            tvSubtituloActividad.setText("Actualizar detalles del producto");
            cargarDatosProductoExistente();
        } else {
            tvTituloActividad.setText("Agregar Producto");
            tvSubtituloActividad.setText("Nuevo producto para el catálogo");
            habilitarBotonGuardar(false); // Deshabilitar inicialmente para nuevo producto
        }
        // La detección de cambios se maneja con listeners individuales
    }

    private void inicializarVistas() {
        btnBackProducto = findViewById(R.id.btnBackProducto);
        tvTituloActividad = findViewById(R.id.tvTituloActividad);
        tvSubtituloActividad = findViewById(R.id.tvSubtituloActividad);
        btnGuardarRapido = findViewById(R.id.btnGuardarRapido);

        cvImagenProducto = findViewById(R.id.cvImagenProducto);
        ivImagenProducto = findViewById(R.id.ivImagenProducto);
        llOverlayImagen = findViewById(R.id.llOverlayImagen);
        pbCargandoImagen = findViewById(R.id.pbCargandoImagen);
        btnSeleccionarGaleria = findViewById(R.id.btnSeleccionarGaleria);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);

        tilNombreProducto = findViewById(R.id.tilNombreProducto);
        etNombreProducto = findViewById(R.id.etNombreProducto);
        tilDescripcionProducto = findViewById(R.id.tilDescripcionProducto);
        etDescripcionProducto = findViewById(R.id.etDescripcionProducto);
        tilCategoriaProducto = findViewById(R.id.tilCategoriaProducto);
        actvCategoriaProducto = findViewById(R.id.actvCategoriaProducto);
        tilPrecioProducto = findViewById(R.id.tilPrecioProducto);
        etPrecioProducto = findViewById(R.id.etPrecioProducto);
        swDisponibilidadProducto = findViewById(R.id.swDisponibilidadProducto);

        tilIngredientesEspeciales = findViewById(R.id.tilIngredientesEspeciales);
        etIngredientesEspeciales = findViewById(R.id.etIngredientesEspeciales);
        tilNotasInternas = findViewById(R.id.tilNotasInternas);
        etNotasInternas = findViewById(R.id.etNotasInternas);

        btnCancelarProducto = findViewById(R.id.btnCancelarProducto);
        btnGuardarProducto = findViewById(R.id.btnGuardarProducto);

        progressOverlay = findViewById(R.id.progressOverlay);
        tvProgressMessage = findViewById(R.id.tvProgressMessage);
    }

    private void configurarLaunchers() {
        selectorGaleriaLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Log.d(ETIQUETA_DEBUG, "Imagen seleccionada de galería: " + uri.toString());
                        uriImagenSeleccionada = uri;
                        urlImagenParaGuardar = null;
                        Glide.with(this).load(uri).centerCrop().placeholder(R.drawable.ic_cake).into(ivImagenProducto);
                        ivImagenProducto.clearColorFilter();
                        llOverlayImagen.setVisibility(View.VISIBLE);
                        marcarCambios();
                    } else {
                        Log.d(ETIQUETA_DEBUG, "No se seleccionó imagen de galería.");
                    }
                });

        tomadorFotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && rutaFotoCamara != null) {
                        uriImagenSeleccionada = Uri.fromFile(new File(rutaFotoCamara));
                        urlImagenParaGuardar = null;
                        Glide.with(this).load(uriImagenSeleccionada).centerCrop().into(ivImagenProducto);
                        ivImagenProducto.clearColorFilter();
                        llOverlayImagen.setVisibility(View.VISIBLE);
                        marcarCambios();
                    } else {
                        rutaFotoCamara = null;
                    }
                });

        solicitadorPermisosMultiplesLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean camaraConcedida = permissions.getOrDefault(Manifest.permission.CAMERA, false);
                    boolean lecturaAlmacenamientoConcedida = permissions.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
                    boolean escrituraAlmacenamientoConcedida = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q ?
                            permissions.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false) : true;

                    // Determinar qué acción se intentaba basándose en qué permisos se pidieron.
                    if (permissions.containsKey(Manifest.permission.CAMERA)) {
                        if (camaraConcedida && escrituraAlmacenamientoConcedida) {
                            Log.d(ETIQUETA_DEBUG, "Permisos de cámara concedidos por el launcher. Abriendo cámara.");
                            abrirCamara();
                        } else {
                            Log.d(ETIQUETA_DEBUG, "Permisos de cámara denegados por el launcher.");
                            Toast.makeText(this, "Permiso de cámara o escritura denegado.", Toast.LENGTH_LONG).show();
                        }
                    } else if (permissions.containsKey(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if (lecturaAlmacenamientoConcedida) {
                            Log.d(ETIQUETA_DEBUG, "Permiso de lectura de almacenamiento concedido por el launcher. Abriendo galería.");
                            selectorGaleriaLauncher.launch("image/*");
                        } else {
                            Log.d(ETIQUETA_DEBUG, "Permiso de lectura de almacenamiento denegado por el launcher.");
                            Toast.makeText(this, "Permiso de almacenamiento denegado.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void configurarListeners() {
        btnBackProducto.setOnClickListener(v -> manejarSalida());
        btnGuardarRapido.setOnClickListener(v -> validarYGuardarProducto());
        btnGuardarProducto.setOnClickListener(v -> validarYGuardarProducto());
        btnCancelarProducto.setOnClickListener(v -> manejarSalida());

        View.OnClickListener listenerSeleccionImagen = v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Seleccionar Imagen")
                    .setItems(new CharSequence[]{"Abrir Galería", "Tomar Foto"}, (dialog, which) -> {
                        if (which == 0) { // Galería
                            solicitarPermisoAlmacenamientoParaGaleria(); // MÉTODO PARA GALERÍA
                        } else { // Cámara
                            solicitarPermisoCamara();
                        }
                    })
                    .show();
        };
        cvImagenProducto.setOnClickListener(listenerSeleccionImagen);
        if(llOverlayImagen != null) llOverlayImagen.setOnClickListener(listenerSeleccionImagen);

        btnSeleccionarGaleria.setOnClickListener(v -> solicitarPermisoAlmacenamientoParaGaleria());
        btnTomarFoto.setOnClickListener(v -> solicitarPermisoCamara());
        cvImagenProducto.setOnClickListener(listenerSeleccionImagen);
        llOverlayImagen.setOnClickListener(listenerSeleccionImagen); // También el overlay

        btnSeleccionarGaleria.setOnClickListener(v -> solicitarPermisoAlmacenamiento());
        btnTomarFoto.setOnClickListener(v -> solicitarPermisoCamara());

        TextWatcher textWatcherCambios = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { marcarCambios(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        etNombreProducto.addTextChangedListener(textWatcherCambios);
        etDescripcionProducto.addTextChangedListener(textWatcherCambios);
        etPrecioProducto.addTextChangedListener(textWatcherCambios);
        actvCategoriaProducto.addTextChangedListener(textWatcherCambios);
        etIngredientesEspeciales.addTextChangedListener(textWatcherCambios);
        etNotasInternas.addTextChangedListener(textWatcherCambios);
        swDisponibilidadProducto.setOnCheckedChangeListener((buttonView, isChecked) -> marcarCambios());
    }

    private void solicitarPermisoAlmacenamientoParaGaleria() {
        String permisoRequerido = Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permisoRequerido) == PackageManager.PERMISSION_GRANTED) {
            Log.d(ETIQUETA_DEBUG, "Permiso de lectura de almacenamiento ya concedido. Abriendo galería.");
            selectorGaleriaLauncher.launch("image/*"); // Lanzar selector de galería
        } else {
            if (shouldShowRequestPermissionRationale(permisoRequerido)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permiso Necesario")
                        .setMessage("Se necesita acceso al almacenamiento para seleccionar una imagen de la galería.")
                        .setPositiveButton("Entendido", (dialog, which) -> {
                            solicitadorPermisosMultiplesLauncher.launch(new String[]{permisoRequerido});
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                // Solicitar permiso directamente
                Log.d(ETIQUETA_DEBUG, "Solicitando permiso de lectura de almacenamiento.");
                solicitadorPermisosMultiplesLauncher.launch(new String[]{permisoRequerido});
            }
        }
    }

    private void marcarCambios() {
        hayCambiosSinGuardar = true;
        habilitarBotonGuardar(true);
    }

    private void cargarCategoriasDesdeRecursos() {
        String[] categoriasArray = getResources().getStringArray(R.array.categorias_productos);
        List<String> listaCategorias = Arrays.asList(categoriasArray);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                listaCategorias
        );
        actvCategoriaProducto.setAdapter(adapter);
        // Para permitir que el usuario empiece a escribir y se muestren sugerencias
        actvCategoriaProducto.setThreshold(1);
    }


    private void solicitarPermisoCamara() {
        String[] permisos;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            permisos = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else {
            permisos = new String[]{Manifest.permission.CAMERA};
        }

        boolean todosConcedidos = true;
        for (String permiso : permisos) {
            if (checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
                todosConcedidos = false;
                break;
            }
        }

        if (todosConcedidos) {
            abrirCamara();
        } else {
            solicitadorPermisosMultiplesLauncher.launch(permisos);
        }
    }

    private void solicitarPermisoAlmacenamiento() {
        String[] permisos = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectorGaleriaLauncher.launch("image/*");
        } else {
            solicitadorPermisosMultiplesLauncher.launch(permisos);
        }
    }


    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = crearArchivoImagenTemporal();
        } catch (IOException ex) {
            Log.e(ETIQUETA_DEBUG, "Error creando archivo temporal para foto", ex);
            Toast.makeText(this, "Error al preparar la cámara", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    photoFile);
            tomadorFotoLauncher.launch(photoURI);
        }
    }

    private File crearArchivoImagenTemporal() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        rutaFotoCamara = image.getAbsolutePath();
        return image;
    }

    private void cargarDatosProductoExistente() {
        if (idProductoExistente == null) return;
        mostrarProgresoGeneral(true, "Cargando producto...");
        referenciaCatalogoBD.child(idProductoExistente).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mostrarProgresoGeneral(false, null);
                if (dataSnapshot.exists()) {
                    pastelExistente = dataSnapshot.getValue(Pastel.class);
                    if (pastelExistente != null) {
                        etNombreProducto.setText(pastelExistente.getNombre());
                        etDescripcionProducto.setText(pastelExistente.getDescripcion());
                        etPrecioProducto.setText(String.format(Locale.US, "%.2f", pastelExistente.getPrecioBase()));
                        actvCategoriaProducto.setText(pastelExistente.getCategoria(), false);
                        swDisponibilidadProducto.setChecked(pastelExistente.isDisponible());

                        urlImagenSubidaOriginal = pastelExistente.getImagenUrl(); // Guardar URL original
                        urlImagenParaGuardar = urlImagenSubidaOriginal;

                        if (urlImagenSubidaOriginal != null && !urlImagenSubidaOriginal.isEmpty()) {
                            Glide.with(AgregarEditarProductoActivity.this)
                                    .load(urlImagenSubidaOriginal)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_cake)
                                    .into(ivImagenProducto);
                            ivImagenProducto.clearColorFilter();
                            llOverlayImagen.setVisibility(View.VISIBLE);
                        } else {
                            ivImagenProducto.setImageResource(R.drawable.ic_add_photo_alternate);
                            // ivImagenProducto.setColorFilter(ContextCompat.getColor(AgregarEditarProductoActivity.this, R.color.text_tertiary));
                            llOverlayImagen.setVisibility(View.GONE);
                        }
                        hayCambiosSinGuardar = false;
                        habilitarBotonGuardar(false);
                    }
                } else {
                    Toast.makeText(AgregarEditarProductoActivity.this, "Producto no encontrado.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mostrarProgresoGeneral(false, null);
                Log.e(ETIQUETA_DEBUG, "Error al cargar producto: ", databaseError.toException());
                Toast.makeText(AgregarEditarProductoActivity.this, "Error al cargar datos.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void validarYGuardarProducto() {
        String nombre = etNombreProducto.getText().toString().trim();
        String descripcion = etDescripcionProducto.getText().toString().trim();
        String precioStr = etPrecioProducto.getText().toString().trim();
        String categoria = actvCategoriaProducto.getText().toString().trim();
        boolean disponible = swDisponibilidadProducto.isChecked();
        // String ingredientesEsp = etIngredientesEspeciales.getText().toString().trim();
        // String notasInt = etNotasInternas.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            tilNombreProducto.setError("El nombre es requerido."); etNombreProducto.requestFocus(); return;
        } else { tilNombreProducto.setError(null); }

        if (TextUtils.isEmpty(precioStr)) {
            tilPrecioProducto.setError("El precio es requerido."); etPrecioProducto.requestFocus(); return;
        } else { tilPrecioProducto.setError(null); }

        double precioBase;
        try {
            precioBase = Double.parseDouble(precioStr);
            if (precioBase <= 0) {
                tilPrecioProducto.setError("El precio debe ser > 0."); etPrecioProducto.requestFocus(); return;
            }
        } catch (NumberFormatException e) {
            tilPrecioProducto.setError("Precio inválido."); etPrecioProducto.requestFocus(); return;
        }

        if (TextUtils.isEmpty(categoria)) {
            tilCategoriaProducto.setError("La categoría es requerida.");
            Toast.makeText(this, "Seleccione una categoría.", Toast.LENGTH_SHORT).show(); return;
        } else { tilCategoriaProducto.setError(null); }

        // Si se seleccionó una nueva imagen local
        // urlImagenParaGuardar será null en este caso hasta que se suba.
        if (uriImagenSeleccionada != null && urlImagenParaGuardar == null) {
            subirImagenYLuegoGuardarProducto(nombre, descripcion, precioBase, categoria, disponible /*,ingredientesEsp, notasInt*/);
        } else {
            guardarDatosProducto(nombre, descripcion, precioBase, categoria, urlImagenParaGuardar, disponible /*, ingredientesEsp, notasInt*/);
        }
    }

    private void subirImagenYLuegoGuardarProducto(String nombre, String descripcion, double precioBase, String categoria, boolean disponible /*, String ingredientesEsp, String notasInt*/) {
        mostrarProgresoGeneral(true, "Subiendo imagen...");
        pbCargandoImagen.setVisibility(View.VISIBLE);

        final StorageReference archivoRef = referenciaAlmacenamientoImagenes.child(System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg");
        UploadTask uploadTask = archivoRef.putFile(uriImagenSeleccionada);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return archivoRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            pbCargandoImagen.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                urlImagenParaGuardar = downloadUri.toString(); // Actualizar con la nueva URL
                guardarDatosProducto(nombre, descripcion, precioBase, categoria, urlImagenParaGuardar, disponible /*, ingredientesEsp, notasInt*/);
            } else {
                mostrarProgresoGeneral(false, null);
                Log.e(ETIQUETA_DEBUG, "Error al subir imagen: ", task.getException());
                Toast.makeText(AgregarEditarProductoActivity.this, "Error al subir imagen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void guardarDatosProducto(String nombre, String descripcion, double precioBase, String categoria, String imagenFinalUrl, boolean disponible /*, String ingredientesEsp, String notasInt*/) {
        mostrarProgresoGeneral(true, idProductoExistente == null ? "Guardando producto..." : "Actualizando producto...");

        Pastel producto = new Pastel(nombre, descripcion, precioBase, categoria, imagenFinalUrl, disponible, null /* opcionesPersonalizacion */);
        // pastel.setIngredientesEspeciales(ingredientesEsp); // Si tienes estos campos en el modelo
        // pastel.setNotasInternas(notasInt);

        DatabaseReference productoRefConcreta;
        String mensajeExito;

        if (idProductoExistente != null) {
            productoRefConcreta = referenciaCatalogoBD.child(idProductoExistente);
            mensajeExito = "Producto actualizado.";
        } else {
            idProductoExistente = referenciaCatalogoBD.push().getKey();
            if (idProductoExistente == null) {
                mostrarProgresoGeneral(false, null);
                Toast.makeText(this, "Error generando ID.", Toast.LENGTH_SHORT).show();
                return;
            }
            productoRefConcreta = referenciaCatalogoBD.child(idProductoExistente);
            mensajeExito = "Producto agregado.";
        }

        productoRefConcreta.setValue(producto)
                .addOnSuccessListener(aVoid -> {
                    mostrarProgresoGeneral(false, null);
                    Toast.makeText(AgregarEditarProductoActivity.this, mensajeExito, Toast.LENGTH_LONG).show();
                    hayCambiosSinGuardar = false;
                    habilitarBotonGuardar(false);
                    finish(); // Volver a la lista de catálogo
                })
                .addOnFailureListener(e -> {
                    mostrarProgresoGeneral(false, null);
                    Toast.makeText(AgregarEditarProductoActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(ETIQUETA_DEBUG, "Error al guardar: ", e);
                });
    }

    private void habilitarBotonGuardar(boolean habilitar) {
        btnGuardarProducto.setEnabled(habilitar);
        btnGuardarRapido.setEnabled(habilitar);
    }

    private void mostrarProgresoGeneral(boolean mostrar, String mensaje) {
        if (mostrar) {
            if (mensaje != null) tvProgressMessage.setText(mensaje);
            progressOverlay.setVisibility(View.VISIBLE);
            habilitarBotonGuardar(false); // Deshabilitar botones mientras se guarda
        } else {
            progressOverlay.setVisibility(View.GONE);
            // El estado del botón de guardar se restablece
            if(hayCambiosSinGuardar){ // Si aún hay cambios
                habilitarBotonGuardar(true);
            }
        }
    }

    private void manejarSalida() {
        if (hayCambiosSinGuardar) {
            new AlertDialog.Builder(this)
                    .setTitle("Descartar Cambios")
                    .setMessage("¿Estás seguro de que quieres salir? Los cambios no guardados se perderán.")
                    .setPositiveButton("Salir", (dialog, which) -> finish())
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            finish();
        }
    }
/*
    @Override
    public void onBackPressed() {
        manejarSalida();
    }
    */
}