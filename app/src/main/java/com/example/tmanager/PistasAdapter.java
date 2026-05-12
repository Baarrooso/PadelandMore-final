package com.example.tmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PistasAdapter extends RecyclerView.Adapter<PistasAdapter.PistaViewHolder> {

    private List<Pista> pistas;
    private OnPistaClickListener listener;

    public interface OnPistaClickListener {
        void onReservarClick(Pista pista);
    }

    public PistasAdapter(List<Pista> pistas, OnPistaClickListener listener) {
        this.pistas = pistas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pista, parent, false);
        return new PistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PistaViewHolder holder, int position) {
        Pista pista = pistas.get(position);
        holder.tvPistaNombre.setText(pista.getNombre());
        holder.tvPistaInfo.setText(pista.getTipo() + " - Capacidad: " + pista.getCapacidad());
        holder.tvPistaPrecio.setText(String.format("€ %.2f", pista.getPrecio()));

        holder.btnReservar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReservarClick(pista);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pistas.size();
    }

    public static class PistaViewHolder extends RecyclerView.ViewHolder {
        TextView tvPistaNombre;
        TextView tvPistaInfo;
        TextView tvPistaPrecio;
        Button btnReservar;
        LinearLayout container;

        public PistaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPistaNombre = itemView.findViewById(R.id.tvPistaNombre);
            tvPistaInfo = itemView.findViewById(R.id.tvPistaInfo);
            tvPistaPrecio = itemView.findViewById(R.id.tvPistaPrecio);
            btnReservar = itemView.findViewById(R.id.btnReservarPista);
            container = itemView.findViewById(R.id.pistaContainer);
        }
    }
}

