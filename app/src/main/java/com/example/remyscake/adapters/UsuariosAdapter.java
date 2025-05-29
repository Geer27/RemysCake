package com.example.remyscake.adapters;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.remyscake.R;
import com.example.remyscake.models.Usuario;
import com.example.remyscake.utils.ConstantesApp;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {

    private List<Usuario> listaUsuarios;
    private Context contexto;
    private OnUsuarioActionsListener listener;

    public interface OnUsuarioActionsListener {
        void onEditRolClick(Usuario usuario);
    }

    public UsuariosAdapter(List<Usuario> listaUsuarios, Context contexto, OnUsuarioActionsListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.contexto = contexto;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        holder.bind(usuario, listener, contexto);
    }

    @Override
    public int getItemCount() {
        return listaUsuarios == null ? 0 : listaUsuarios.size();
    }

    public void actualizarLista(List<Usuario> nuevaLista) {
        this.listaUsuarios = nuevaLista;
        notifyDataSetChanged();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        CardView cvInicialesUsuarioItem;
        TextView tvInicialesUsuarioItem, tvNombreUsuarioItem, tvEmailUsuarioItem, tvRolUsuarioItem;
        ImageButton btnEditarRolUsuarioItem;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            cvInicialesUsuarioItem = itemView.findViewById(R.id.cvInicialesUsuarioItem);
            tvInicialesUsuarioItem = itemView.findViewById(R.id.tvInicialesUsuarioItem);
            tvNombreUsuarioItem = itemView.findViewById(R.id.tvNombreUsuarioItem);
            tvEmailUsuarioItem = itemView.findViewById(R.id.tvEmailUsuarioItem);
            tvRolUsuarioItem = itemView.findViewById(R.id.tvRolUsuarioItem);
            btnEditarRolUsuarioItem = itemView.findViewById(R.id.btnEditarRolUsuarioItem);
        }

        public void bind(final Usuario usuario, final OnUsuarioActionsListener listener, Context contexto) {
            tvNombreUsuarioItem.setText(usuario.getNombreCompleto() != null ? usuario.getNombreCompleto() : "Nombre no disponible");
            tvEmailUsuarioItem.setText(usuario.getCorreoElectronico() != null ? usuario.getCorreoElectronico() : "Email no disponible");

            String rolTexto = "Rol: Desconocido";
            int colorFondoIniciales = ContextCompat.getColor(contexto, R.color.text_tertiary);
            int colorTextoRol = ContextCompat.getColor(contexto, R.color.text_secondary);

            if (usuario.getRol() != null) {
                switch (usuario.getRol()) {
                    case ConstantesApp.ROL_ADMIN:
                        rolTexto = "Rol: Administrador";
                        colorFondoIniciales = ContextCompat.getColor(contexto, R.color.admin_secondary);
                        colorTextoRol = ContextCompat.getColor(contexto, R.color.admin_secondary);
                        break;
                    case ConstantesApp.ROL_VENDEDOR:
                        rolTexto = "Rol: Vendedor";
                        colorFondoIniciales = ContextCompat.getColor(contexto, R.color.seller_primary);
                        colorTextoRol = ContextCompat.getColor(contexto, R.color.seller_primary);
                        break;
                    case ConstantesApp.ROL_PRODUCCION:
                        rolTexto = "Rol: Producción";
                        colorFondoIniciales = ContextCompat.getColor(contexto, R.color.production_primary);
                        colorTextoRol = ContextCompat.getColor(contexto, R.color.production_primary);
                        break;
                }
            }
            tvRolUsuarioItem.setText(rolTexto);
            tvRolUsuarioItem.setTextColor(colorTextoRol);
            cvInicialesUsuarioItem.setCardBackgroundColor(colorFondoIniciales);


            // Poner iniciales
            if (usuario.getNombreCompleto() != null && !usuario.getNombreCompleto().trim().isEmpty()) {
                String[] partesNombre = usuario.getNombreCompleto().trim().split("\\s+");
                String iniciales = "";
                if (partesNombre.length > 0 && !partesNombre[0].isEmpty()) {
                    iniciales += partesNombre[0].charAt(0);
                }
                if (partesNombre.length > 1 && !partesNombre[partesNombre.length - 1].isEmpty()) {
                    iniciales += partesNombre[partesNombre.length - 1].charAt(0);
                }
                tvInicialesUsuarioItem.setText(iniciales.toUpperCase());
            } else {
                tvInicialesUsuarioItem.setText("U");
            }

            btnEditarRolUsuarioItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditRolClick(usuario);
                }
            });

            // itemView.setOnClickListener(v -> {
            //    if (listener != null) {
            //        listener.onUsuarioClick(usuario);
            //    }
            // });
        }
    }
}