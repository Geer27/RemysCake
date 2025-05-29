package com.example.remyscake.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Random;
import android.graphics.drawable.GradientDrawable;
import androidx.core.content.ContextCompat;

import com.example.remyscake.R;
import com.example.remyscake.models.Cliente;


public class ClientesSeleccionAdapter extends RecyclerView.Adapter<ClientesSeleccionAdapter.ClienteViewHolder> {

    private List<Cliente> listaClientes;
    private Context contexto;
    private OnClienteSeleccionadoListener listener;
    private int[] coloresAvatar;


    public interface OnClienteSeleccionadoListener {
        void onClienteSeleccionado(Cliente cliente);
    }

    public ClientesSeleccionAdapter(List<Cliente> listaClientes, Context contexto, OnClienteSeleccionadoListener listener) {
        this.listaClientes = listaClientes;
        this.contexto = contexto;
        this.listener = listener;
        coloresAvatar = new int[]{
                ContextCompat.getColor(contexto, R.color.seller_primary),
                ContextCompat.getColor(contexto, R.color.admin_secondary),
                ContextCompat.getColor(contexto, R.color.production_primary),
                ContextCompat.getColor(contexto, R.color.accent_color)
        };
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_cliente_seleccion, parent, false);
        return new ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente cliente = listaClientes.get(position);
        holder.bind(cliente, listener, coloresAvatar);
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
        TextView tvInicialesClienteItemSeleccion, tvNombreClienteItemSeleccion, tvTelefonoClienteItemSeleccion;

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInicialesClienteItemSeleccion = itemView.findViewById(R.id.tvInicialesClienteItemSeleccion);
            tvNombreClienteItemSeleccion = itemView.findViewById(R.id.tvNombreClienteItemSeleccion);
            tvTelefonoClienteItemSeleccion = itemView.findViewById(R.id.tvTelefonoClienteItemSeleccion);
        }

        public void bind(final Cliente cliente, final OnClienteSeleccionadoListener listener, final int[] colores) {
            tvNombreClienteItemSeleccion.setText(cliente.getNombreCompleto());
            tvTelefonoClienteItemSeleccion.setText("Tel: " + (cliente.getTelefono() != null ? cliente.getTelefono() : "N/A"));

            // Poner iniciales y color de fondo
            if (cliente.getNombreCompleto() != null && !cliente.getNombreCompleto().trim().isEmpty()) {
                String[] partesNombre = cliente.getNombreCompleto().trim().split("\\s+");
                String iniciales = "";
                if (partesNombre.length > 0 && !partesNombre[0].isEmpty()) {
                    iniciales += partesNombre[0].charAt(0);
                }
                if (partesNombre.length > 1 && !partesNombre[partesNombre.length - 1].isEmpty()) {
                    iniciales += partesNombre[partesNombre.length - 1].charAt(0);
                }
                tvInicialesClienteItemSeleccion.setText(iniciales.toUpperCase());
            } else {
                tvInicialesClienteItemSeleccion.setText("C");
            }
            Random random = new Random();
            GradientDrawable fondoCircular = (GradientDrawable) tvInicialesClienteItemSeleccion.getBackground();
            if (fondoCircular != null) {
                fondoCircular.setColor(colores[random.nextInt(colores.length)]);
            } else {
                tvInicialesClienteItemSeleccion.setBackgroundColor(colores[random.nextInt(colores.length)]);
            }


            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClienteSeleccionado(cliente);
                }
            });
        }
    }
}