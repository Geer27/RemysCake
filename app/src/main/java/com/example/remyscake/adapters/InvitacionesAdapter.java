package com.example.remyscake.adapters;


import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.remyscake.R;
import com.example.remyscake.models.Invitacion;
import com.example.remyscake.utils.ConstantesApp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class InvitacionesAdapter extends RecyclerView.Adapter<InvitacionesAdapter.InvitacionViewHolder> {

    private List<Invitacion> listaInvitaciones;
    private Context contexto;
    private OnInvitacionActionListener listener;

    public interface OnInvitacionActionListener {
        void onReenviarClick(Invitacion invitacion, int posicion);
        void onCancelarClick(Invitacion invitacion, int posicion);
    }

    public InvitacionesAdapter(List<Invitacion> listaInvitaciones, Context contexto, OnInvitacionActionListener listener) {
        this.listaInvitaciones = listaInvitaciones;
        this.contexto = contexto;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InvitacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_invitacion_pendiente, parent, false);
        return new InvitacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitacionViewHolder holder, int position) {
        Invitacion invitacion = listaInvitaciones.get(position);
        holder.bind(invitacion, listener, contexto);
    }

    @Override
    public int getItemCount() {
        return listaInvitaciones == null ? 0 : listaInvitaciones.size();
    }

    public void actualizarLista(List<Invitacion> nuevaLista) {
        this.listaInvitaciones = nuevaLista;
        notifyDataSetChanged();
    }

    static class InvitacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvitacionEmail, tvInvitacionRol, tvInvitacionExpira;
        ImageButton btnReenviarInvitacion, btnCancelarInvitacion;

        public InvitacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvitacionEmail = itemView.findViewById(R.id.tvInvitacionEmail);
            tvInvitacionRol = itemView.findViewById(R.id.tvInvitacionRol);
            tvInvitacionExpira = itemView.findViewById(R.id.tvInvitacionExpira);
            btnReenviarInvitacion = itemView.findViewById(R.id.btnReenviarInvitacion);
            btnCancelarInvitacion = itemView.findViewById(R.id.btnCancelarInvitacion);
        }

        public void bind(final Invitacion invitacion, final OnInvitacionActionListener listener, Context context) {
            tvInvitacionEmail.setText(invitacion.getEmailInvitado());
            tvInvitacionRol.setText(capitalizar(invitacion.getRolAsignado()));

            long ahora = System.currentTimeMillis();
            long fechaCreacion = invitacion.getFechaCreacion();
            long diasDesdeCreacion = TimeUnit.MILLISECONDS.toDays(ahora - fechaCreacion);
            long diasParaExpirar = 7 - diasDesdeCreacion; // Asumiendo 7 días de validez

            if ("usada".equalsIgnoreCase(invitacion.getEstado())) {
                tvInvitacionExpira.setText("Usada");
                tvInvitacionExpira.setTextColor(ContextCompat.getColor(context, R.color.success_color));
                btnReenviarInvitacion.setVisibility(View.GONE);
                btnCancelarInvitacion.setVisibility(View.GONE);
            } else if ("expirada".equalsIgnoreCase(invitacion.getEstado()) || diasParaExpirar < 0) {
                tvInvitacionExpira.setText("Expirada");
                tvInvitacionExpira.setTextColor(ContextCompat.getColor(context, R.color.error_color));
                btnReenviarInvitacion.setVisibility(View.VISIBLE); // Podría permitirse reenviar una expirada
                btnCancelarInvitacion.setVisibility(View.VISIBLE); // O solo cancelar/eliminar
            } else {
                tvInvitacionExpira.setText("Expira en " + diasParaExpirar + (diasParaExpirar == 1 ? " día" : " días"));
                tvInvitacionExpira.setTextColor(ContextCompat.getColor(context, R.color.warning_color));
                btnReenviarInvitacion.setVisibility(View.VISIBLE);
                btnCancelarInvitacion.setVisibility(View.VISIBLE);
            }


            // Cambiar el color y fondo del chip de rol dinámicamente
            GradientDrawable rolBackground = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.chip_background_generic).mutate(); // Usar un drawable genérico y cambiar color
            int colorTextoRol = ContextCompat.getColor(context, R.color.text_primary_on_accent); // Un color de texto que contraste
            int colorBordeRol = ContextCompat.getColor(context, R.color.text_secondary);
            int colorFondoRol = ContextCompat.getColor(context, R.color.background_light_gray); // Color por defecto

            switch (invitacion.getRolAsignado()) {
                case ConstantesApp.ROL_VENDEDOR:
                    colorFondoRol = ContextCompat.getColor(context, R.color.seller_light);
                    colorTextoRol = ContextCompat.getColor(context, R.color.seller_primary);
                    colorBordeRol = ContextCompat.getColor(context, R.color.seller_primary);
                    break;
                case ConstantesApp.ROL_PRODUCCION:
                    colorFondoRol = ContextCompat.getColor(context, R.color.production_light);
                    colorTextoRol = ContextCompat.getColor(context, R.color.production_primary);
                    colorBordeRol = ContextCompat.getColor(context, R.color.production_primary);
                    break;
                case ConstantesApp.ROL_ADMIN:
                    colorFondoRol = ContextCompat.getColor(context, R.color.admin_light);
                    colorTextoRol = ContextCompat.getColor(context, R.color.admin_primary);
                    colorBordeRol = ContextCompat.getColor(context, R.color.admin_primary);
                    break;
            }
            rolBackground.setColor(colorFondoRol);
            rolBackground.setStroke(2, colorBordeRol);
            tvInvitacionRol.setBackground(rolBackground);
            tvInvitacionRol.setTextColor(colorTextoRol);


            btnReenviarInvitacion.setOnClickListener(v -> listener.onReenviarClick(invitacion, getAdapterPosition()));
            btnCancelarInvitacion.setOnClickListener(v -> listener.onCancelarClick(invitacion, getAdapterPosition()));
        }

        private String capitalizar(String texto) {
            if (texto == null || texto.isEmpty()) {
                return texto;
            }
            return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
        }
    }
}