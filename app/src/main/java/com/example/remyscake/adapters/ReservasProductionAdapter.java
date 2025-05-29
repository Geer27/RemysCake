package com.example.remyscake.adapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.remyscake.R;
import com.example.remyscake.models.ItemPedido;
import com.example.remyscake.models.Reserva;
import com.example.remyscake.utils.ConstantesApp;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReservasProductionAdapter extends RecyclerView.Adapter<ReservasProductionAdapter.ViewHolder> {

    private List<Reserva> listaReservas;
    private Context contexto;
    private OnReservaProductionActionsListener listener;

    public interface OnReservaProductionActionsListener {
        void onCambiarEstadoClick(Reserva reserva, String nuevoEstado);
        void onVerDetalleProduccionClick(Reserva reserva);
    }

    public ReservasProductionAdapter(List<Reserva> listaReservas, Context contexto, OnReservaProductionActionsListener listener) {
        this.listaReservas = listaReservas;
        this.contexto = contexto;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_reserva_produccion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReservaProdId, tvPrioridadReserva, tvReservaProdCliente, tvReservaProdVendedor;
        TextView tvTiempoRestante, tvReservaProdFechaEntrega, tvReservaProdTiempoEstimado;
        TextView tvReservaProdNotas, tvProgressPercentage, tvReservaProdEstado;
        CardView cvPrioridadReserva, cvEstadoReservaPrincipalItem; // ID del CardView para el estado principal
        RecyclerView rvProductosProducir;
        LinearLayout llNotasProduccion, llProgressProduccion;
        ProgressBar pbProgresoProduccion;
        MaterialButton btnIniciarProduccion, btnVerDetalleProduccion, btnMarcarTerminado;

        ItemsProduccionAdapter itemsAdapter;
        List<ItemPedido> listaItemsInterna;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReservaProdId = itemView.findViewById(R.id.tvReservaIdProduccion);
            cvPrioridadReserva = itemView.findViewById(R.id.cvPrioridadReserva);
            tvPrioridadReserva = itemView.findViewById(R.id.tvPrioridadReserva);
            tvReservaProdCliente = itemView.findViewById(R.id.tvReservaClienteProduccion);
            tvReservaProdVendedor = itemView.findViewById(R.id.tvReservaVendedorProduccion);
            tvTiempoRestante = itemView.findViewById(R.id.tvTiempoRestante);
            tvReservaProdFechaEntrega = itemView.findViewById(R.id.tvReservaEntregaProduccion);
            tvReservaProdTiempoEstimado = itemView.findViewById(R.id.tvReservaTiempoEstimado);
            rvProductosProducir = itemView.findViewById(R.id.rvProductosProducir);
            llNotasProduccion = itemView.findViewById(R.id.llNotasProduccion);
            tvReservaProdNotas = itemView.findViewById(R.id.tvNotasProduccion);

            llProgressProduccion = itemView.findViewById(R.id.llProgressProduccion);
            tvProgressPercentage = itemView.findViewById(R.id.tvProgressPercentage);
            pbProgresoProduccion = itemView.findViewById(R.id.pbProgresoProduccion);

            btnIniciarProduccion = itemView.findViewById(R.id.btnIniciarProduccion);
            btnVerDetalleProduccion = itemView.findViewById(R.id.btnVerDetalleProduccion);
            btnMarcarTerminado = itemView.findViewById(R.id.btnMarcarTerminado);

            tvReservaProdEstado = itemView.findViewById(R.id.tvReservaProdEstado);
            cvEstadoReservaPrincipalItem = itemView.findViewById(R.id.cvEstadoReservaPrincipalItem);


            listaItemsInterna = new ArrayList<>();
            itemsAdapter = new ItemsProduccionAdapter(listaItemsInterna, itemView.getContext());
            rvProductosProducir.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvProductosProducir.setAdapter(itemsAdapter);
        }

        public void bind(final Reserva reserva, final OnReservaProductionActionsListener listener, Context context) {
            String estadoActual = reserva.getStatus() != null ? reserva.getStatus().toLowerCase() : "desconocido";
            if(tvReservaProdEstado != null){
                tvReservaProdEstado.setText(estadoActual.substring(0, 1).toUpperCase() + estadoActual.substring(1));
                configurarColorEstado(estadoActual, tvReservaProdEstado, cvEstadoReservaPrincipalItem, context);
            }
        }

        private void configurarColorEstado(String estado, TextView tvEstado, CardView cvFondoEstado, Context context) {
            int colorTexto; int colorFondo;
            switch (estado.toLowerCase()) {
                case ConstantesApp.ESTADO_RESERVA_PENDIENTE:
                case ConstantesApp.ESTADO_RESERVA_CONFIRMADA:
                    colorTexto = R.color.warning_color; colorFondo = R.color.warning_light; break;
                case ConstantesApp.ESTADO_RESERVA_EN_PRODUCCION:
                    colorTexto = R.color.info_color; colorFondo = R.color.info_light; break;
                case ConstantesApp.ESTADO_RESERVA_LISTA_ENTREGA:
                    colorTexto = R.color.production_ready; colorFondo = R.color.production_ready_light; break;
                case ConstantesApp.ESTADO_RESERVA_ENTREGADA:
                    colorTexto = R.color.success_color; colorFondo = R.color.success_light; break;
                case ConstantesApp.ESTADO_RESERVA_CANCELADA:
                    colorTexto = R.color.error_color; colorFondo = R.color.error_light; break;
                default:
                    colorTexto = R.color.text_secondary; colorFondo = R.color.background_light; break;
            }
            if (tvEstado != null) tvEstado.setTextColor(ContextCompat.getColor(context, colorTexto));
            if (cvFondoEstado != null) {
                cvFondoEstado.setCardBackgroundColor(ContextCompat.getColor(context, colorFondo));
            }
        }
    }
}