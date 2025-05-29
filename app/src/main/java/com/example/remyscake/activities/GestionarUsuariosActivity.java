package com.example.remyscake.activities;



import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.adapters.UsuariosAdapter;
import com.example.remyscake.models.Usuario;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GestionarUsuariosActivity extends AppCompatActivity implements UsuariosAdapter.OnUsuarioActionsListener {

    private static final String ETIQUETA_DEBUG = "GestionUsuarios";

    // Vistas UI
    private ImageButton btnBackUsuarios, btnFilterUsuarios;
    private TextView tvTotalUsuarios, tvSellers, tvProduction, tvSinUsuarios;
    private CardView cvAgregarUsuario;
    private RecyclerView rvUsuarios;
    private FloatingActionButton fabAgregarUsuario;

    // Firebase
    private DatabaseReference referenciaUsuariosBD;
    private ValueEventListener listenerUsuarios;

    // Listas y Adaptador
    private UsuariosAdapter usuariosAdapter;
    private List<Usuario> listaTodosLosUsuarios = new ArrayList<>();
    // private List<Usuario> listaUsuariosFiltrados = new ArrayList<>();

    private ActivityResultLauncher<Intent> crearEditarUsuarioLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_usuarios);

        referenciaUsuariosBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_USUARIOS);

        inicializarVistas();
        configurarRecyclerView();
        establecerListeners();
        configurarLauncherResultado();

        // cargarDatosUsuarios() se llamará en onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuarios(); // Cargar o recargar datos cuando la actividad se vuelve visible
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (referenciaUsuariosBD != null && listenerUsuarios != null) {
            referenciaUsuariosBD.removeEventListener(listenerUsuarios);
        }
    }

    private void inicializarVistas() {
        btnBackUsuarios = findViewById(R.id.btnBackUsuarios);
        btnFilterUsuarios = findViewById(R.id.btnFilterUsuarios);
        tvTotalUsuarios = findViewById(R.id.tvTotalUsuarios);
        tvSellers = findViewById(R.id.tvSellers);
        tvProduction = findViewById(R.id.tvProduction);
        tvSinUsuarios = findViewById(R.id.tvSinUsuarios);
        cvAgregarUsuario = findViewById(R.id.cvAgregarUsuario);
        rvUsuarios = findViewById(R.id.rvUsuarios);
        fabAgregarUsuario = findViewById(R.id.fabAgregarUsuario);
    }

    private void configurarRecyclerView() {
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        // Inicializar la lista que usa el adaptador (puede ser la misma que listaTodosLosUsuarios si no hay filtros complejos)
        usuariosAdapter = new UsuariosAdapter(new ArrayList<>(), this, this); // Iniciar con lista vacía
        rvUsuarios.setAdapter(usuariosAdapter);
    }

    private void configurarLauncherResultado() {
        crearEditarUsuarioLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(this, "Lista de usuarios actualizada.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void establecerListeners() {
        btnBackUsuarios.setOnClickListener(v -> onBackPressed());

        btnFilterUsuarios.setOnClickListener(v -> {
            Toast.makeText(this, "Filtro de Usuarios (Próximamente)", Toast.LENGTH_SHORT).show();
        });

        View.OnClickListener listenerAgregar = v -> {
            Intent intent = new Intent(GestionarUsuariosActivity.this, CrearEditarUsuarioActivity.class);
            crearEditarUsuarioLauncher.launch(intent); // Usar launcher
        };
        cvAgregarUsuario.setOnClickListener(listenerAgregar);
        fabAgregarUsuario.setOnClickListener(listenerAgregar);
    }

    private void cargarDatosUsuarios() {
        tvSinUsuarios.setText("Cargando usuarios...");
        tvSinUsuarios.setVisibility(View.VISIBLE);
        rvUsuarios.setVisibility(View.GONE);

        if (listenerUsuarios != null) {
            referenciaUsuariosBD.removeEventListener(listenerUsuarios);
        }

        listenerUsuarios = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaTodosLosUsuarios.clear();
                long countSellers = 0;
                long countProduction = 0;

                if (dataSnapshot.exists()) {
                    for (DataSnapshot usuarioSnapshot : dataSnapshot.getChildren()) {
                        Usuario usuario = usuarioSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            usuario.setId(usuarioSnapshot.getKey()); // Guardar el UID de Firebase
                            listaTodosLosUsuarios.add(usuario);

                            if (usuario.getRol() != null) {
                                switch (usuario.getRol()) {
                                    case ConstantesApp.ROL_VENDEDOR:
                                        countSellers++;
                                        break;
                                    case ConstantesApp.ROL_PRODUCCION:
                                        countProduction++;
                                        break;
                                }
                            }
                        }
                    }
                }

                // Ordenar la lista, por ejemplo, por nombre
                Collections.sort(listaTodosLosUsuarios, Comparator.comparing(Usuario::getNombreCompleto, String.CASE_INSENSITIVE_ORDER));

                // Actualizar estadísticas
                tvTotalUsuarios.setText(String.valueOf(listaTodosLosUsuarios.size()));
                tvSellers.setText(String.valueOf(countSellers));
                tvProduction.setText(String.valueOf(countProduction));

                // Actualizar RecyclerView
                if (usuariosAdapter != null) {
                    usuariosAdapter.actualizarLista(listaTodosLosUsuarios);
                }

                // Mostrar u ocultar mensaje "Sin Usuarios"
                if (listaTodosLosUsuarios.isEmpty()) {
                    tvSinUsuarios.setText("No hay usuarios registrados en el sistema.");
                    tvSinUsuarios.setVisibility(View.VISIBLE);
                    rvUsuarios.setVisibility(View.GONE);
                } else {
                    tvSinUsuarios.setVisibility(View.GONE);
                    rvUsuarios.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(ETIQUETA_DEBUG, "Error al cargar usuarios: ", databaseError.toException());
                Toast.makeText(GestionarUsuariosActivity.this, "Error al cargar lista de usuarios.", Toast.LENGTH_LONG).show();
                tvSinUsuarios.setText("Error al cargar usuarios.");
                tvSinUsuarios.setVisibility(View.VISIBLE);
                rvUsuarios.setVisibility(View.GONE);
            }
        };
        referenciaUsuariosBD.addValueEventListener(listenerUsuarios); // Para actualizaciones en tiempo real
    }

    // Implementación de OnUsuarioActionsListener del UsuariosAdapter
    @Override
    public void onEditRolClick(Usuario usuario) {
        Intent intent = new Intent(GestionarUsuariosActivity.this, CrearEditarUsuarioActivity.class);
        intent.putExtra("UID_USUARIO", usuario.getId()); // Pasar el UID para que la actividad sepa que es edición
        crearEditarUsuarioLauncher.launch(intent); // Usar launcher
    }


    // @Override
    // public void onUsuarioClick(Usuario usuario) {
    //    Toast.makeText(this, "Usuario seleccionado: " + usuario.getNombreCompleto(), Toast.LENGTH_SHORT).show();
    // }
}