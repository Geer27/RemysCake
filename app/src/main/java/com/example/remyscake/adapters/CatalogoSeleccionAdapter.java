package com.example.remyscake.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.remyscake.R;
import com.example.remyscake.models.Pastel;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class CatalogoSeleccionAdapter extends RecyclerView.Adapter<CatalogoSeleccionAdapter.ViewHolder> {

    private List<Pastel> listaPasteles;
    private Context contexto;
    private OnProductoSeleccionadoListener listener;

    public interface OnProductoSeleccionadoListener {
        void onProductoSeleccionado(Pastel pastel);
    }

    public CatalogoSeleccionAdapter(List<Pastel> listaPasteles, Context contexto, OnProductoSeleccionadoListener listener) {
        this.listaPasteles = listaPasteles;
        this.contexto = contexto;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_producto_seleccion_catalogo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pastel pastel = listaPasteles.get(position);

        holder.tvProductoNombre.setText(pastel.getNombre());
        holder.tvProductoDesc.setText(pastel.getDescripcion());
        holder.tvProductoPrecio.setText(String.format(Locale.getDefault(), "$%.2f", pastel.getPrecioBase()));

        if (pastel.getImagenUrl() != null && !pastel.getImagenUrl().isEmpty()) {
            Glide.with(contexto).load(pastel.getImagenUrl())
                    .placeholder(R.drawable.ic_cake)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(holder.ivProductoImagen);
        } else {
            holder.ivProductoImagen.setImageResource(R.drawable.ic_cake);
        }

        holder.btnSeleccionar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductoSeleccionado(pastel);
            }
        });

        holder.btnSeleccionar.setEnabled(pastel.isDisponible());
        holder.itemView.setAlpha(pastel.isDisponible() ? 1.0f : 0.5f);
    }

    @Override
    public int getItemCount() {
        return listaPasteles == null ? 0 : listaPasteles.size();
    }

    public void actualizarLista(List<Pastel> nuevaLista) {
        this.listaPasteles = nuevaLista;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductoImagen;
        TextView tvProductoNombre, tvProductoDesc, tvProductoPrecio;
        MaterialButton btnSeleccionar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductoImagen = itemView.findViewById(R.id.ivProductoCatalogoImagenSel);
            tvProductoNombre = itemView.findViewById(R.id.tvProductoCatalogoNombreSel);
            tvProductoDesc = itemView.findViewById(R.id.tvProductoCatalogoDescSel);
            tvProductoPrecio = itemView.findViewById(R.id.tvProductoCatalogoPrecioSel);
            btnSeleccionar = itemView.findViewById(R.id.btnSeleccionarEsteProducto);
        }
    }
}
