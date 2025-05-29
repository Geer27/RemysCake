package com.example.remyscake.activities;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remyscake.R;
import com.example.remyscake.adapters.ProductosSeleccionadosAdapter;
import com.example.remyscake.models.Cliente;
import com.example.remyscake.models.ItemPedido;
import com.example.remyscake.models.Pastel;
import com.example.remyscake.models.Payment;
import com.example.remyscake.models.Reserva;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CrearReservaActivity extends AppCompatActivity implements ProductosSeleccionadosAdapter.OnItemPedidoInteractionListener {

    private static final String ETIQUETA_DEBUG = "CrearReservaActivity";

    // Vistas
    private ImageButton btnBackCrearReserva, btnGuardarBorrador, btnCambiarCliente;
    private LinearLayout llClienteSeleccionado, llSeleccionarCliente, llSinProductos;
    private TextView tvClienteIniciales, tvClienteNombre, tvClienteTelefono;
    private MaterialButton btnBuscarCliente, btnNuevoCliente, btnAgregarProducto, btnCrearReservaFinal;
    private TextInputEditText etFechaEntrega, etHoraEntrega, etNotasEspeciales;
    private RecyclerView rvProductosSeleccionados;
    private TextView tvSubtotal, tvTotal;
    private TextView tvTituloActividadCrearReserva;

    // Firebase
    private FirebaseAuth autenticacionFirebase;
    private FirebaseUser usuarioFirebaseActual; // El vendedor que crea la reserva
    private DatabaseReference referenciaReservacionesBD;

    // Datos de la reserva
    private Cliente clienteSeleccionado;
    private List<ItemPedido> itemsSeleccionados = new ArrayList<>();
    private ProductosSeleccionadosAdapter productosAdapter;
    private Calendar calendarioEntrega = Calendar.getInstance();

    // Launchers
    private ActivityResultLauncher<Intent> selectorClienteLauncher;
    private ActivityResultLauncher<Intent> selectorProductoLauncher;
    private ActivityResultLauncher<Intent> editorReservaLauncher;

    private String idReservaAEditar = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_reserva);

        autenticacionFirebase = FirebaseAuth.getInstance();
        usuarioFirebaseActual = autenticacionFirebase.getCurrentUser();

        if (usuarioFirebaseActual == null) {
            Toast.makeText(this, "Error: Sesión de vendedor no válida.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        referenciaReservacionesBD = FirebaseDatabase.getInstance().getReference(ConstantesApp.NODO_RESERVACIONES);

        inicializarVistas();
        configurarLaunchers();
        configurarListenersUI();
        configurarRecyclerView();

        // Verificar si se está editando una reserva
        if (getIntent().hasExtra("ID_RESERVA_EDITAR")) {
            idReservaAEditar = getIntent().getStringExtra("ID_RESERVA_EDITAR");
            if (tvTituloActividadCrearReserva != null) {
                tvTituloActividadCrearReserva.setText("Editar Reserva");
            }
            btnCrearReservaFinal.setText("Actualizar Reserva");
            cargarDatosReservaParaEditar();
        } else {
            if (tvTituloActividadCrearReserva != null) {
                tvTituloActividadCrearReserva.setText("Crear Nueva Reserva");
            }
        }


        actualizarVistaProductos();
        actualizarTotales();
        verificarEstadoBotonCrearReserva();
    }

    private void inicializarVistas() {
        btnBackCrearReserva = findViewById(R.id.btnBackCrearReserva);
        btnGuardarBorrador = findViewById(R.id.btnGuardarBorrador);
        btnCambiarCliente = findViewById(R.id.btnCambiarCliente);

        llClienteSeleccionado = findViewById(R.id.llClienteSeleccionado);
        llSeleccionarCliente = findViewById(R.id.llSeleccionarCliente);
        tvClienteIniciales = findViewById(R.id.tvClienteIniciales);
        tvClienteNombre = findViewById(R.id.tvClienteNombre);
        tvClienteTelefono = findViewById(R.id.tvClienteTelefono);

        btnBuscarCliente = findViewById(R.id.btnBuscarCliente);
        btnNuevoCliente = findViewById(R.id.btnNuevoCliente);

        tvTituloActividadCrearReserva = findViewById(R.id.tvTituloActividadCrearReserva);

        etFechaEntrega = findViewById(R.id.etFechaEntrega);
        etHoraEntrega = findViewById(R.id.etHoraEntrega);

        btnAgregarProducto = findViewById(R.id.btnAgregarProducto);
        rvProductosSeleccionados = findViewById(R.id.rvProductosSeleccionados);
        llSinProductos = findViewById(R.id.llSinProductos);

        etNotasEspeciales = findViewById(R.id.etNotasEspeciales);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotal = findViewById(R.id.tvTotal);
        btnCrearReservaFinal = findViewById(R.id.btnCrearReserva); // El ID del botón de MaterialButton
    }

    private void configurarLaunchers() {
        selectorClienteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        clienteSeleccionado = (Cliente) result.getData().getSerializableExtra("CLIENTE_SELECCIONADO");
                        if (clienteSeleccionado != null) {
                            mostrarClienteSeleccionadoUI();
                        }
                        verificarEstadoBotonCrearReserva();
                    }
                });

        selectorProductoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Pastel pastel = (Pastel) result.getData().getSerializableExtra("PRODUCTO_SELECCIONADO");
                        int cantidad = result.getData().getIntExtra("CANTIDAD_SELECCIONADA", 1);
                        if (pastel != null) {
                            agregarOActualizarItemPedido(pastel, cantidad);
                        }
                    }
                });
    }

    private void configurarListenersUI() {
        btnBackCrearReserva.setOnClickListener(v -> onBackPressed());
        btnGuardarBorrador.setOnClickListener(v -> guardarBorrador());

        btnBuscarCliente.setOnClickListener(v -> {
            Intent intent = new Intent(this, BuscarClienteActivity.class);
            selectorClienteLauncher.launch(intent);
        });

        btnNuevoCliente.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevoClienteActivity.class);
            selectorClienteLauncher.launch(intent);
        });

        btnCambiarCliente.setOnClickListener(v -> {
            clienteSeleccionado = null; // Limpiar cliente
            mostrarClienteSeleccionadoUI(); // Esto ocultará la vista del cliente y mostrará los botones
            verificarEstadoBotonCrearReserva();
        });

        etFechaEntrega.setOnClickListener(v -> mostrarSelectorFecha());
        etHoraEntrega.setOnClickListener(v -> mostrarSelectorHora());

        btnAgregarProducto.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeleccionarProductoActivity.class);
            selectorProductoLauncher.launch(intent);
        });

        btnCrearReservaFinal.setOnClickListener(v -> procesarReserva());
    }

    private void configurarRecyclerView() {
        productosAdapter = new ProductosSeleccionadosAdapter(itemsSeleccionados, this, this);
        rvProductosSeleccionados.setLayoutManager(new LinearLayoutManager(this));
        rvProductosSeleccionados.setAdapter(productosAdapter);
    }

    private void mostrarClienteSeleccionadoUI() {
        if (clienteSeleccionado != null) {
            tvClienteNombre.setText(clienteSeleccionado.getNombreCompleto());
            tvClienteTelefono.setText(clienteSeleccionado.getTelefono());
            String[] nombres = clienteSeleccionado.getNombreCompleto().trim().split("\\s+");
            String iniciales = "";
            if (nombres.length > 0 && !nombres[0].isEmpty()) iniciales += nombres[0].charAt(0);
            if (nombres.length > 1 && !nombres[nombres.length - 1].isEmpty() && !nombres[0].equals(nombres[nombres.length -1])) iniciales += nombres[nombres.length - 1].charAt(0);
            else if (nombres.length == 1 && nombres[0].length() > 1) iniciales += nombres[0].charAt(1);

            tvClienteIniciales.setText(iniciales.toUpperCase());
            llClienteSeleccionado.setVisibility(View.VISIBLE);
            llSeleccionarCliente.setVisibility(View.GONE);
        } else {
            llClienteSeleccionado.setVisibility(View.GONE);
            llSeleccionarCliente.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarSelectorFecha() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendarioEntrega.set(Calendar.YEAR, year);
                    calendarioEntrega.set(Calendar.MONTH, monthOfYear);
                    calendarioEntrega.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    actualizarCampoFecha();
                    verificarEstadoBotonCrearReserva();
                },
                calendarioEntrega.get(Calendar.YEAR),
                calendarioEntrega.get(Calendar.MONTH),
                calendarioEntrega.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void mostrarSelectorHora() {
        // ... (código del selector de hora como antes) ...
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendarioEntrega.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendarioEntrega.set(Calendar.MINUTE, minute);
                    actualizarCampoHora();
                    verificarEstadoBotonCrearReserva();
                },
                calendarioEntrega.get(Calendar.HOUR_OF_DAY),
                calendarioEntrega.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void actualizarCampoFecha() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFechaEntrega.setText(sdf.format(calendarioEntrega.getTime()));
    }

    private void actualizarCampoHora() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        etHoraEntrega.setText(sdf.format(calendarioEntrega.getTime()));
    }

    private void agregarOActualizarItemPedido(Pastel pastel, int cantidadAAnadir) {
        boolean encontrado = false;
        for (ItemPedido item : itemsSeleccionados) {
            if (item.getProductoId().equals(pastel.getId())) {
                item.setCantidad(item.getCantidad() + cantidadAAnadir);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            ItemPedido nuevoItem = new ItemPedido(
                    pastel.getId(),
                    pastel.getNombre(),
                    cantidadAAnadir,
                    pastel.getPrecioBase(),
                    pastel.getImagenUrl()
            );
            itemsSeleccionados.add(nuevoItem);
        }
        productosAdapter.actualizarLista(itemsSeleccionados);
        actualizarVistaProductos();
        actualizarTotales();
        verificarEstadoBotonCrearReserva();
    }

    @Override
    public void onCantidadAumentada(ItemPedido item, int posicion) {
        item.setCantidad(item.getCantidad() + 1);
        productosAdapter.notifyItemChanged(posicion);
        actualizarTotales();
    }

    @Override
    public void onCantidadDisminuida(ItemPedido item, int posicion) {
        if (item.getCantidad() > 1) {
            item.setCantidad(item.getCantidad() - 1);
            productosAdapter.notifyItemChanged(posicion);
        } else { // Si la cantidad es 1 y se disminuye, se elimina
            itemsSeleccionados.remove(posicion);
            productosAdapter.notifyItemRemoved(posicion);
            productosAdapter.notifyItemRangeChanged(posicion, itemsSeleccionados.size());
            actualizarVistaProductos(); // Para mostrar el mensaje si la lista queda vacía
        }
        actualizarTotales();
    }

    @Override
    public void onItemEliminado(ItemPedido item, int posicion) {
        itemsSeleccionados.remove(posicion);
        productosAdapter.notifyItemRemoved(posicion);
        productosAdapter.notifyItemRangeChanged(posicion, itemsSeleccionados.size());
        actualizarVistaProductos();
        actualizarTotales();
        verificarEstadoBotonCrearReserva(); // El botón podría deshabilitarse si no quedan productos
    }


    private void actualizarVistaProductos() {
        if (itemsSeleccionados.isEmpty()) {
            rvProductosSeleccionados.setVisibility(View.GONE);
            llSinProductos.setVisibility(View.VISIBLE);
        } else {
            rvProductosSeleccionados.setVisibility(View.VISIBLE);
            llSinProductos.setVisibility(View.GONE);
        }
    }

    private void actualizarTotales() {
        double subtotal = 0;
        for (ItemPedido item : itemsSeleccionados) {
            subtotal += item.getPrecioUnitario() * item.getCantidad();
        }
        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal)); // Por ahora total = subtotal
    }

    private void verificarEstadoBotonCrearReserva() {
        boolean clienteOk = clienteSeleccionado != null;
        boolean fechaOk = !TextUtils.isEmpty(etFechaEntrega.getText());
        boolean horaOk = !TextUtils.isEmpty(etHoraEntrega.getText());
        boolean productosOk = !itemsSeleccionados.isEmpty();
        btnCrearReservaFinal.setEnabled(clienteOk && fechaOk && horaOk && productosOk);
    }

    private void guardarBorrador() {
        Toast.makeText(this, "Guardar Borrador (Próximamente)", Toast.LENGTH_SHORT).show();
    }

    private void procesarReserva() {
        if (clienteSeleccionado == null) {
            Toast.makeText(this, "Seleccione un cliente.", Toast.LENGTH_SHORT).show(); return;
        }
        if (TextUtils.isEmpty(etFechaEntrega.getText().toString())) {
            Toast.makeText(this, "Seleccione fecha de entrega.", Toast.LENGTH_SHORT).show(); return;
        }
        if (TextUtils.isEmpty(etHoraEntrega.getText().toString())) {
            Toast.makeText(this, "Seleccione hora de entrega.", Toast.LENGTH_SHORT).show(); return;
        }
        if (itemsSeleccionados.isEmpty()) {
            Toast.makeText(this, "Agregue al menos un producto.", Toast.LENGTH_SHORT).show(); return;
        }

        btnCrearReservaFinal.setEnabled(false);
        // Mostrar ProgressBar general si tienes uno
        // progressOverlay.setVisibility(View.VISIBLE);
        // tvProgressMessage.setText(idReservaAEditar == null ? "Creando reserva..." : "Actualizando reserva...");

        String idReservaFinal;
        if (idReservaAEditar != null) {
            idReservaFinal = idReservaAEditar;
        } else {
            idReservaFinal = referenciaReservacionesBD.push().getKey();
            if (idReservaFinal == null) {
                Toast.makeText(this, "Error al generar ID para la reserva.", Toast.LENGTH_SHORT).show();
                btnCrearReservaFinal.setEnabled(true);
                // progressOverlay.setVisibility(View.GONE);
                return;
            }
        }

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setId(idReservaFinal); // Aunque Firebase lo usa como key, es bueno tenerlo en el objeto local
        nuevaReserva.setClientId(clienteSeleccionado.getId());
        nuevaReserva.setClienteNombre(clienteSeleccionado.getNombreCompleto());
        nuevaReserva.setCreatedBy(usuarioFirebaseActual.getUid());
        // Para sellerNombre, necesitarías cargar los datos del vendedor (usuarioFirebaseActual)
        // String nombreVendedor = usuarioFirebaseActual.getDisplayName(); // O desde tu nodo /usuarios/{uid}
        // nuevaReserva.setSellerNombre(nombreVendedor != null ? nombreVendedor : "Vendedor App");
        nuevaReserva.setCreatedAt(idReservaAEditar == null ? System.currentTimeMillis() : /* obtener createdAt original si editas */ System.currentTimeMillis() ); // Para ServerValue.TIMESTAMP necesitarías un Map
        nuevaReserva.setDeliveryAt(calendarioEntrega.getTimeInMillis());
        nuevaReserva.setStatus("pendiente"); // O el estado original si se edita y no se cambia
        nuevaReserva.setNotasAdicionalesPedido(etNotasEspeciales.getText().toString().trim());

        Map<String, ItemPedido> itemsParaFirebase = new HashMap<>();
        for (ItemPedido item : itemsSeleccionados) {
            itemsParaFirebase.put(item.getProductoId(), item);
        }
        nuevaReserva.setItems(itemsParaFirebase);

        double totalCalculado = 0;
        for (ItemPedido item : itemsSeleccionados) {
            totalCalculado += item.getPrecioUnitario() * item.getCantidad();
        }
        Payment pago = new Payment(totalCalculado, "a_definir", "pendiente", 0L); // timestamp 0 o actual
        nuevaReserva.setPayment(pago);

        // Si es una nueva reserva, usa ServerValue.TIMESTAMP para createdAt
        Map<String, Object> reservaMap = new HashMap<>();
        reservaMap.put("clientId", nuevaReserva.getClientId());
        reservaMap.put("clienteNombre", nuevaReserva.getClienteNombre());
        reservaMap.put("createdBy", nuevaReserva.getCreatedBy());
        // reservaMap.put("sellerNombre", nuevaReserva.getSellerNombre()); // Asegúrate de tener este dato
        reservaMap.put("deliveryAt", nuevaReserva.getDeliveryAt());
        reservaMap.put("status", nuevaReserva.getStatus());
        reservaMap.put("notasAdicionalesPedido", nuevaReserva.getNotasAdicionalesPedido());
        reservaMap.put("items", nuevaReserva.getItems()); // Firebase mapeará el objeto ItemPedido
        reservaMap.put("payment", nuevaReserva.getPayment()); // Firebase mapeará el objeto Payment

        if (idReservaAEditar == null) { // Solo para nuevas reservas
            reservaMap.put("createdAt", ServerValue.TIMESTAMP);
        } else { // Para editar, mantener el createdAt original
            reservaMap.put("createdAt", nuevaReserva.getCreatedAt()); // Esto sería del objeto cargado si editas
            reservaMap.put("updatedAt", ServerValue.TIMESTAMP); // Buena práctica añadir updatedAt
        }


        referenciaReservacionesBD.child(idReservaFinal).setValue(reservaMap)
                .addOnSuccessListener(aVoid -> {
                    // progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(CrearReservaActivity.this, idReservaAEditar == null ? "Reserva creada." : "Reserva actualizada.", Toast.LENGTH_LONG).show();
                    setResult(Activity.RESULT_OK); // Indicar que la operación fue exitosa
                    finish();
                })
                .addOnFailureListener(e -> {
                    // progressOverlay.setVisibility(View.GONE);
                    btnCrearReservaFinal.setEnabled(true);
                    Toast.makeText(CrearReservaActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(ETIQUETA_DEBUG, "Error al guardar reserva: ", e);
                });
    }

    private void cargarDatosReservaParaEditar() {
        Toast.makeText(this, "Cargando datos de reserva para editar (Pendiente)", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED); // Indicar que se canceló
        super.onBackPressed();
    }
}