package com.example.remyscake.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.remyscake.R;
import com.example.remyscake.models.ItemPedido;

import java.util.List;

public class ItemsProduccionAdapter extends RecyclerView.Adapter<ItemsProduccionAdapter.ViewHolder> {

    private List<ItemPedido> listaItems;
    private Context contexto;

    public ItemsProduccionAdapter(List<ItemPedido> listaItems, Context contexto) {
        this.listaItems = listaItems;
        this.contexto = contexto;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(  R.layout.item_reserva_items_detalle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemPedido item = listaItems.get(position);
        holder.tvItemNombreProducto.setText(item.getProductoNombre());
        holder.tvItemCantidad.setText("x" + item.getCantidad());
        if (holder.tvItemPrecioTotal != null) {
            holder.tvItemPrecioTotal.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaItems == null ? 0 : listaItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemNombreProducto, tvItemCantidad, tvItemPrecioTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemNombreProducto = itemView.findViewById(R.id.tvItemNombreProducto);
            tvItemCantidad = itemView.findViewById(R.id.tvItemCantidad);
            tvItemPrecioTotal = itemView.findViewById(R.id.tvItemPrecioTotal);
        }
    }
}