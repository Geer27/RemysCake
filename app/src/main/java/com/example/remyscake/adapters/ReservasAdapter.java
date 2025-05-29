package com.example.remyscake.adapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.remyscake.R;
import com.example.remyscake.models.ItemPedido;
import com.example.remyscake.models.Payment;
import com.example.remyscake.models.Reserva;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservasAdapter extends RecyclerView.Adapter<ReservasAdapter.ReservaViewHolder> {

    private List<Reserva> listaReservas;
    private Context contexto;
    private OnReservaActionsListener listener;

    public interface OnReservaActionsListener {
        void onVerDetalleClick(Reserva reserva);
        void onEditarClick(Reserva reserva);
        void onCambiarEstadoClick(Reserva reserva);
    }

    public ReservasAdapter(List<Reserva> listaReservas, Context contexto, OnReservaActionsListener listener) {
        this.listaReservas = listaReservas;
        this.contexto = contexto;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_reserva_admin, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = listaReservas.get(position);
        holder.bind(reserva, listener, contexto);
    }

    @Override
    public int getItemCount() {
        return listaReservas == null ? 0 : listaReservas.size();
    }

    public void actualizarLista(List<Reserva> nuevaLista) {
        this.listaReservas = nuevaLista;
        notifyDataSetChanged();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView tvReservaId, tvReservaEstado, tvReservaCliente, tvReservaTotal, tvReservaEntrega, tvReservaSeller, tvReservaPagoEstado;
        CardView cvEstadoReserva;
        RecyclerView rvReservaItems;
        MaterialButton btnVerDetalleReserva, btnEditarReserva, btnCambiarEstadoReserva;

        ReservaItemsAdapter itemsAdapter;
        List<ItemPedido> listaItemsInterna;


        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReservaId = itemView.findViewById(R.id.tvReservaId);
            tvReservaEstado = itemView.findViewById(R.id.tvReservaEstado);
            cvEstadoReserva = itemView.findViewById(R.id.cvEstadoReserva);
            tvReservaCliente = itemView.findViewById(R.id.tvReservaCliente);
            tvReservaTotal = itemView.findViewById(R.id.tvReservaTotal);
            tvReservaEntrega = itemView.findViewById(R.id.tvReservaEntrega);
            tvReservaSeller = itemView.findViewById(R.id.tvReservaSeller);
            rvReservaItems = itemView.findViewById(R.id.rvReservaItems);
            tvReservaPagoEstado = itemView.findViewById(R.id.tvReservaPagoEstado);
            btnVerDetalleReserva = itemView.findViewById(R.id.btnVerDetalleReserva);
            btnEditarReserva = itemView.findViewById(R.id.btnEditarReserva);
            btnCambiarEstadoReserva = itemView.findViewById(R.id.btnCambiarEstadoReserva);

            rvReservaItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            listaItemsInterna = new ArrayList<>(); // Inicializar la lista
            itemsAdapter = new ReservaItemsAdapter(listaItemsInterna, itemView.getContext());
            rvReservaItems.setAdapter(itemsAdapter);
        }

        public void bind(final Reserva reserva, final OnReservaActionsListener listener, Context context) {
            tvReservaId.setText("#" + (reserva.getId() != null ? reserva.getId().substring(Math.max(0, reserva.getId().length() - 6)) : "N/A"));
            tvReservaCliente.setText("Cliente: " + (reserva.getClienteNombre() != null ? reserva.getClienteNombre() : "Desconocido"));
            tvReservaSeller.setText("Por: " + (reserva.getSellerNombre() != null ? reserva.getSellerNombre() : "N/A"));

            SimpleDateFormat sdfFechaHora = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            if (reserva.getDeliveryAt() > 0) { // Evitar error si el timestamp es 0
                tvReservaEntrega.setText("Entrega: " + sdfFechaHora.format(new Date(reserva.getDeliveryAt())));
            } else {
                tvReservaEntrega.setText("Entrega: N/A");
            }


            String estadoReserva = reserva.getStatus() != null ? reserva.getStatus() : "desconocido";
            tvReservaEstado.setText(estadoReserva.substring(0, 1).toUpperCase() + estadoReserva.substring(1));
            configurarColorEstadoReserva(estadoReserva, tvReservaEstado, cvEstadoReserva, context);

            listaItemsInterna.clear();
            if (reserva.getItems() != null) {
                for (Map.Entry<String, ItemPedido> entry : reserva.getItems().entrySet()) {
                    ItemPedido item = entry.getValue();
                    if (item != null && item.getProductoNombre() != null) {
                        listaItemsInterna.add(item);
                    } else if (item != null) {
                        Log.w("ReservasAdapter", "Item " + entry.getKey() + " no tiene nombre de producto o es nulo.");
                    } else {
                        Log.w("ReservasAdapter", "Item nulo encontrado para la clave: " + entry.getKey());
                    }
                }
            }
            itemsAdapter.notifyDataSetChanged(); // Notificar al adaptador anidado
            rvReservaItems.setVisibility(listaItemsInterna.isEmpty() ? View.GONE : View.VISIBLE);


            if (reserva.getPayment() != null) {
                Payment pago = reserva.getPayment();
                tvReservaTotal.setText(String.format(Locale.getDefault(), "$%.2f", pago.getAmount()));
                String estadoPago = pago.getStatusPago() != null ? pago.getStatusPago() : "desconocido";
                tvReservaPagoEstado.setText(estadoPago.substring(0, 1).toUpperCase() + estadoPago.substring(1));
                configurarColorEstadoPago(estadoPago, tvReservaPagoEstado, context);
            } else {
                tvReservaTotal.setText("$0.00");
                tvReservaPagoEstado.setText("N/D");
                tvReservaPagoEstado.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                View parentPago = (View) tvReservaPagoEstado.getParent();
                if (parentPago instanceof CardView) {
                    ((CardView)parentPago).setCardBackgroundColor(ContextCompat.getColor(context, R.color.background_light)); // Un color neutral
                }
            }

            btnVerDetalleReserva.setOnClickListener(v -> listener.onVerDetalleClick(reserva));
            btnEditarReserva.setOnClickListener(v -> listener.onEditarClick(reserva));
            btnCambiarEstadoReserva.setOnClickListener(v -> listener.onCambiarEstadoClick(reserva));
        }

        private void configurarColorEstadoReserva(String estado, TextView tvEstado, CardView cvFondoEstado, Context context) {
            int colorTexto;
            int colorFondo;
            switch (estado.toLowerCase()) {
                case "pendiente": case "confirmada":
                    colorTexto = R.color.warning_color; colorFondo = R.color.warning_light; break;
                case "en_produccion": case "en produccion":
                    colorTexto = R.color.info_color; colorFondo = R.color.info_light; break;
                case "lista_para_entrega": case "lista para entrega":
                    colorTexto = R.color.production_ready; colorFondo = R.color.production_ready_light; break;
                case "entregada":
                    colorTexto = R.color.success_color; colorFondo = R.color.success_light; break;
                case "cancelada":
                    colorTexto = R.color.error_color; colorFondo = R.color.error_light; break;
                default:
                    colorTexto = R.color.text_secondary; colorFondo = R.color.background_light; break;
            }
            tvEstado.setTextColor(ContextCompat.getColor(context, colorTexto));
            cvFondoEstado.setCardBackgroundColor(ContextCompat.getColor(context, colorFondo));
        }

        private void configurarColorEstadoPago(String estadoPago, TextView tvEstadoPago, Context context) {
            int colorTexto;
            int colorFondo;
            View parentView = (View) tvEstadoPago.getParent();

            switch (estadoPago.toLowerCase()) {
                case "pendiente":
                    colorTexto = R.color.error_color; colorFondo = R.color.error_light; break;
                case "completado":
                    colorTexto = R.color.success_color; colorFondo = R.color.success_light; break;
                case "fallido": case "reembolsado":
                    colorTexto = R.color.text_primary; colorFondo = R.color.text_tertiary; break;
                default:
                    colorTexto = R.color.text_secondary; colorFondo = R.color.background_light; break;
            }
            tvEstadoPago.setTextColor(ContextCompat.getColor(context, colorTexto));
            if (parentView instanceof CardView) {
                ((CardView) parentView).setCardBackgroundColor(ContextCompat.getColor(context, colorFondo));
            }
        }
    }
}