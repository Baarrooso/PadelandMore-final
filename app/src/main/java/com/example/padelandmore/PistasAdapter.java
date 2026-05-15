package com.example.padelandmore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PistasAdapter extends RecyclerView.Adapter<PistasAdapter.PistaViewHolder> {

    private List<Pista> pistas;
    private OnPistaClickListener listener;
    private int selectedPosition = -1;

    public interface OnPistaClickListener {
        void onPistaSelected(Pista pista);
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

        // Resaltar seleccionada
        if (selectedPosition == position) {
            holder.container.setBackgroundColor(0xFFFFA500); // Color naranja
        } else {
            holder.container.setBackgroundColor(0x00000000); // Transparente
        }

        holder.container.setOnClickListener(v -> {
            int old = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(old);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onPistaSelected(pista);
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
        LinearLayout container;

        public PistaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPistaNombre = itemView.findViewById(R.id.tvPistaNombre);
            tvPistaInfo = itemView.findViewById(R.id.tvPistaInfo);
            container = itemView.findViewById(R.id.pistaContainer);
        }
    }
}
