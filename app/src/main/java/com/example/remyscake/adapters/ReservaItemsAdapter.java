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
import java.util.Locale;

public class ReservaItemsAdapter extends RecyclerView.Adapter<ReservaItemsAdapter.ItemViewHolder> {

    private List<ItemPedido> listaItems;
    private Context contexto;

    public ReservaItemsAdapter(List<ItemPedido> listaItems, Context contexto) {
        this.listaItems = listaItems;
        this.contexto = contexto;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_reserva_items_detalle, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemPedido item = listaItems.get(position);

        holder.tvItemNombreProducto.setText(item.getProductoNombre());
        holder.tvItemCantidad.setText("x" + item.getCantidad());
        double precioTotalItem = item.getPrecioUnitario() * item.getCantidad();
        holder.tvItemPrecioTotal.setText(String.format(Locale.getDefault(), "$%.2f", precioTotalItem));
    }

    @Override
    public int getItemCount() {
        return listaItems == null ? 0 : listaItems.size();
    }

    public void actualizarLista(List<ItemPedido> nuevaLista) {
        this.listaItems = nuevaLista;
        notifyDataSetChanged();
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemNombreProducto, tvItemCantidad, tvItemPrecioTotal;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemNombreProducto = itemView.findViewById(R.id.tvItemNombreProducto);
            tvItemCantidad = itemView.findViewById(R.id.tvItemCantidad);
            tvItemPrecioTotal = itemView.findViewById(R.id.tvItemPrecioTotal);
        }
    }
}