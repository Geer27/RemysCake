package com.example.remyscake.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView; // Para los iconos pequeños
import android.widget.PopupMenu; // Para el botón de opciones
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
// import com.ejemplo.pasteleriaonline.models.Usuario; // Si vas a mostrar el nombre del vendedor
// import com.google.firebase.database.DataSnapshot; // Para cargar nombre del vendedor
// import com.google.firebase.database.DatabaseError; // Para cargar nombre del vendedor
// import com.google.firebase.database.FirebaseDatabase; // Para cargar nombre del vendedor
// import com.google.firebase.database.ValueEventListener; // Para cargar nombre del vendedor

import com.example.remyscake.R;
import com.example.remyscake.models.Cliente;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class ClienteAdminAdapter extends RecyclerView.Adapter<ClienteAdminAdapter.ClienteViewHolder> {

    private List<Cliente> listaClientes;
    private Context contexto;
    private OnClienteAdminActionsListener listener;

    public interface OnClienteAdminActionsListener {
        void onClienteClick(Cliente cliente);
        void onEditCliente(Cliente cliente);
        void onDeleteCliente(Cliente cliente);
        void onVerHistorial(Cliente cliente);
        void onCrearReservaParaCliente(Cliente cliente);
    }

    public ClienteAdminAdapter(List<Cliente> listaClientes, Context contexto, OnClienteAdminActionsListener listener) {
        this.listaClientes = listaClientes;
        this.contexto = contexto;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_cliente_admin, parent, false);
        return new ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente clienteActual = listaClientes.get(position);
        holder.bind(clienteActual, listener, contexto /*, mapaNombresVendedores*/);
    }

    @Override
    public int getItemCount() {
        return listaClientes == null ? 0 : listaClientes.size();
    }

    public void actualizarLista(List<Cliente> nuevaLista) {
        this.listaClientes = nuevaLista;
        notifyDataSetChanged();
    }


    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvClienteIniciales, tvClienteNombre, tvClienteTelefono, tvClienteCompras;
        TextView tvClienteSeller, tvClienteUltimaCompra;
        ImageButton btnClienteOpciones;
        com.google.android.material.button.MaterialButton btnVerHistorial, btnCrearReservaCliente;
        CardView cvInicialesBackground;

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClienteIniciales = itemView.findViewById(R.id.tvClienteIniciales);
            tvClienteNombre = itemView.findViewById(R.id.tvClienteNombre);
            tvClienteTelefono = itemView.findViewById(R.id.tvClienteTelefono);
            tvClienteCompras = itemView.findViewById(R.id.tvClienteCompras); // Nuevo

            tvClienteSeller = itemView.findViewById(R.id.tvClienteSeller); // Nuevo
            tvClienteUltimaCompra = itemView.findViewById(R.id.tvClienteUltimaCompra); // Nuevo
            btnClienteOpciones = itemView.findViewById(R.id.btnClienteOpciones); // Nuevo

            btnVerHistorial = itemView.findViewById(R.id.btnVerHistorial); // Nuevo
            btnCrearReservaCliente = itemView.findViewById(R.id.btnCrearReservaCliente); // Nuevo
        }

        public void bind(final Cliente cliente, final OnClienteAdminActionsListener listener, Context contexto /*, Map<String, String> mapaNombresVendedores*/) {
            tvClienteNombre.setText(cliente.getNombreCompleto() != null ? cliente.getNombreCompleto() : "N/A");
            tvClienteTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "N/A");

            // Iniciales
            if (cliente.getNombreCompleto() != null && !cliente.getNombreCompleto().isEmpty()) {
                String[] nombres = cliente.getNombreCompleto().split("\\s+");
                StringBuilder iniciales = new StringBuilder();
                if (nombres.length > 0 && !nombres[0].isEmpty()) iniciales.append(nombres[0].charAt(0));
                if (nombres.length > 1 && !nombres[nombres.length - 1].isEmpty()) iniciales.append(nombres[nombres.length - 1].charAt(0));
                tvClienteIniciales.setText(iniciales.toString().toUpperCase());
            } else {
                tvClienteIniciales.setText("?");
            }

            tvClienteCompras.setText("0"); // TODO: Cargar número real de compras
             // TODO: Cargar fecha real
            tvClienteUltimaCompra.setText("Última: N/A");


            // Placeholder para vendedor
            if (cliente.getCreadoPor() != null) {
                tvClienteSeller.setText("Vendedor: ID " + cliente.getCreadoPor().substring(0, Math.min(6, cliente.getCreadoPor().length()))+"...");
            } else {
                tvClienteSeller.setText("Vendedor: N/A");
            }


            itemView.setOnClickListener(v -> listener.onClienteClick(cliente));
            btnVerHistorial.setOnClickListener(v -> listener.onVerHistorial(cliente));
            btnCrearReservaCliente.setOnClickListener(v -> listener.onCrearReservaParaCliente(cliente));

            btnClienteOpciones.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(contexto, btnClienteOpciones);
                popupMenu.getMenuInflater().inflate(R.menu.menu_opciones_cliente, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.opcion_editar_cliente) {
                        listener.onEditCliente(cliente);
                        return true;
                    } else if (itemId == R.id.opcion_eliminar_cliente) {
                        listener.onDeleteCliente(cliente);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }
    }
}