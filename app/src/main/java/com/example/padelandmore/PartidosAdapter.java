package com.example.padelandmore;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class PartidosAdapter extends RecyclerView.Adapter<PartidosAdapter.PartidoViewHolder> {

    private final List<Map<String, String>> partidos;
    private final Context context;

    public PartidosAdapter(Context context, List<Map<String, String>> partidos) {
        this.context = context;
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
        String resultadoRaw = partido.get("resultado");
        String resultado = resultadoRaw != null ? resultadoRaw.toLowerCase() : "";
        if ("victoria".equals(resultado)) {
            holder.txtResultado.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_light));
        } else if ("derrota".equals(resultado)) {
            holder.txtResultado.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_light));
        }

        // Click para abrir detalle del partido
        holder.itemView.setOnClickListener(v -> {
            // Usar nombre de clase como string para evitar dependencia de compilación en algunos entornos
            Intent intent = new Intent();
            intent.setClassName(context, "com.example.padelandmore.PartidoDetalleActivity");
            // Pasar todos los datos del mapa como extras
            for (Map.Entry<String, String> entry : partido.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return partidos.size();
    }

    public static class PartidoViewHolder extends RecyclerView.ViewHolder {
        TextView txtRival, txtResultado, txtFecha;

        PartidoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRival = itemView.findViewById(R.id.txtRival);
            txtResultado = itemView.findViewById(R.id.txtResultado);
            txtFecha = itemView.findViewById(R.id.txtFecha);
        }
    }
}

