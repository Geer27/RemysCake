package com.example.remyscake.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.remyscake.R;
import com.example.remyscake.models.Pastel;

import java.util.List;
import java.util.Locale;

public class CatalogoAdapter extends RecyclerView.Adapter<CatalogoAdapter.PastelViewHolder> {

    private List<Pastel> listaPasteles;
    private Context contexto;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Pastel pastel);
        void onEditClick(Pastel pastel); // Para el botón de editar
    }

    public CatalogoAdapter(List<Pastel> listaPasteles, Context contexto, OnItemClickListener listener) {
        this.listaPasteles = listaPasteles;
        this.contexto = contexto;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PastelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_producto_catalogo, parent, false);
        return new PastelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastelViewHolder holder, int position) {
        Pastel pastelActual = listaPasteles.get(position);
        holder.bind(pastelActual, listener);
    }

    @Override
    public int getItemCount() {
        return listaPasteles == null ? 0 : listaPasteles.size();
    }

    // Método para actualizar la lista desde la actividad
    public void actualizarLista(List<Pastel> nuevaLista) {
        this.listaPasteles = nuevaLista;
        notifyDataSetChanged();
    }

    static class PastelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductoImagen;         // Coincide con tu ID
        TextView tvProductoNombre;          // Coincide con tu ID
        TextView tvProductoCategoria;       // Coincide con tu ID
        TextView tvProductoPrecio;          // Coincide con tu ID
        // No hay TextView para disponibilidad en tu layout, así que lo quitamos del ViewHolder
        ImageButton btnEditarProducto;      // Coincide con tu ID

        public PastelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductoImagen = itemView.findViewById(R.id.ivProductoImagen);
            tvProductoNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvProductoCategoria = itemView.findViewById(R.id.tvProductoCategoria);
            tvProductoPrecio = itemView.findViewById(R.id.tvProductoPrecio);
            btnEditarProducto = itemView.findViewById(R.id.btnEditarProducto);
        }

        public void bind(final Pastel pastel, final OnItemClickListener listener) {
            tvProductoNombre.setText(pastel.getNombre());
            tvProductoCategoria.setText(pastel.getCategoria() != null ? pastel.getCategoria() : "N/A");
            tvProductoPrecio.setText(String.format(Locale.getDefault(), "$%.2f", pastel.getPrecioBase()));

            // Lógica para disponibilidad (si decides añadirla al layout o manejarla de otra forma)
            // if (pastel.isDisponible()) { ... } else { ... }

            if (pastel.getImagenUrl() != null && !pastel.getImagenUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(pastel.getImagenUrl())
                        .placeholder(R.drawable.ic_cake) // Usa tu placeholder
                        .error(R.drawable.ic_broken_image)   // Usa tu placeholder de error
                        .into(ivProductoImagen);
            } else {
                // Si no hay imagen URL, podrías dejar el src del XML o poner un placeholder
                ivProductoImagen.setImageResource(R.drawable.ic_cake); // Placeholder por defecto
                // Si originalmente tenías un tint y quieres mantenerlo para el placeholder:
                // ivProductoImagen.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.admin_primary), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            // Si ya no usas el tint directamente en el XML para el placeholder:
            // ivProductoImagen.clearColorFilter(); // Para asegurarte que no se tinte la imagen cargada


            itemView.setOnClickListener(v -> listener.onItemClick(pastel));
            btnEditarProducto.setOnClickListener(v -> listener.onEditClick(pastel));
        }
    }

}
