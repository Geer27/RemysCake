package com.example.remyscake.adapters;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.remyscake.R;
import com.example.remyscake.models.ItemPedido;

import java.util.List;
import java.util.Locale;

public class ProductosSeleccionadosAdapter extends RecyclerView.Adapter<ProductosSeleccionadosAdapter.ViewHolder> {

    private List<ItemPedido> listaItems;
    private Context contexto;
    private OnItemPedidoInteractionListener listener;

    public interface OnItemPedidoInteractionListener {
        void onCantidadAumentada(ItemPedido item, int posicion);
        void onCantidadDisminuida(ItemPedido item, int posicion);
        void onItemEliminado(ItemPedido item, int posicion);
    }

    public ProductosSeleccionadosAdapter(List<ItemPedido> listaItems, Context contexto, OnItemPedidoInteractionListener listener) {
        this.listaItems = listaItems;
        this.contexto = contexto;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_producto_seleccionado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemPedido item = listaItems.get(position);

        holder.tvProductoNombre.setText(item.getProductoNombre());
        holder.tvProductoPrecioUnitario.setText(String.format(Locale.getDefault(), "$%.2f c/u", item.getPrecioUnitario()));
        holder.tvCantidad.setText(String.valueOf(item.getCantidad()));

        double precioTotalItem = item.getPrecioUnitario() * item.getCantidad();
        holder.tvPrecioTotal.setText(String.format(Locale.getDefault(), "$%.2f", precioTotalItem));


        if (item.getImagenUrl() != null && !item.getImagenUrl().isEmpty()) {
            Glide.with(contexto)
                    .load(item.getImagenUrl())
                    .placeholder(R.drawable.ic_cake)
                    .error(R.drawable.ic_broken_image)
                    .into(holder.ivProductoImagen);
        } else {
            holder.ivProductoImagen.setImageResource(R.drawable.ic_cake); // Placeholder por defecto
        }

        holder.btnAumentarCantidad.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCantidadAumentada(item, holder.getAdapterPosition());
            }
        });
        holder.btnDisminuirCantidad.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCantidadDisminuida(item, holder.getAdapterPosition());
            }
        });
        holder.btnEliminarProducto.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemEliminado(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaItems == null ? 0 : listaItems.size();
    }

    public void actualizarLista(List<ItemPedido> nuevaLista) {
        this.listaItems = nuevaLista;
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductoImagen;
        TextView tvProductoNombre, tvProductoPrecioUnitario, tvCantidad, tvPrecioTotal;
        ImageButton btnAumentarCantidad, btnDisminuirCantidad, btnEliminarProducto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductoImagen = itemView.findViewById(R.id.ivProductoImagen);
            tvProductoNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvProductoPrecioUnitario = itemView.findViewById(R.id.tvProductoPrecioUnitario);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            btnAumentarCantidad = itemView.findViewById(R.id.btnAumentarCantidad);
            btnDisminuirCantidad = itemView.findViewById(R.id.btnDisminuirCantidad);
            tvPrecioTotal = itemView.findViewById(R.id.tvPrecioTotal);
            btnEliminarProducto = itemView.findViewById(R.id.btnEliminarProducto);
        }
    }
}