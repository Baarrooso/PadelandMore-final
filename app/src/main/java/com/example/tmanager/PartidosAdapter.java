package com.example.tmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class PartidosAdapter extends RecyclerView.Adapter<PartidosAdapter.PartidoViewHolder> {

    private List<Map<String, String>> partidos;

    public PartidosAdapter(List<Map<String, String>> partidos) {
        this.partidos = partidos;
    }

    @NonNull
    @Override
    public PartidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partido, parent, false);
        return new PartidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PartidoViewHolder holder, int position) {
        Map<String, String> partido = partidos.get(position);
        holder.txtRival.setText(partido.getOrDefault("rival", "Desconocido"));
        holder.txtResultado.setText(partido.getOrDefault("resultado", "Pendiente"));
        holder.txtFecha.setText(partido.getOrDefault("fecha", ""));

        // Color según resultado
        String resultado = partido.getOrDefault("resultado", "").toLowerCase();
        if ("victoria".equals(resultado)) {
            holder.txtResultado.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_light));
        } else if ("derrota".equals(resultado)) {
            holder.txtResultado.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_light));
        }
    }

    @Override
    public int getItemCount() {
        return partidos.size();
    }

    static class PartidoViewHolder extends RecyclerView.ViewHolder {
        TextView txtRival, txtResultado, txtFecha;

        PartidoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRival = itemView.findViewById(R.id.txtRival);
            txtResultado = itemView.findViewById(R.id.txtResultado);
            txtFecha = itemView.findViewById(R.id.txtFecha);
        }
    }
}

