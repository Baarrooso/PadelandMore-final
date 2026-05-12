package com.example.tmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClubsAdapter extends RecyclerView.Adapter<ClubsAdapter.ClubViewHolder> {

    private List<Club> clubes;
    private OnClubClickListener listener;

    public interface OnClubClickListener {
        void onClubClick(Club club);
    }

    public ClubsAdapter(List<Club> clubes, OnClubClickListener listener) {
        this.clubes = clubes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_club_reserva, parent, false);
        return new ClubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {
        Club club = clubes.get(position);
        holder.tvClubNombre.setText(club.getNombre());
        holder.tvClubUbicacion.setText(club.getUbicacion());

        holder.container.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClubClick(club);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clubes.size();
    }

    public static class ClubViewHolder extends RecyclerView.ViewHolder {
        TextView tvClubNombre;
        TextView tvClubUbicacion;
        LinearLayout container;

        public ClubViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClubNombre = itemView.findViewById(R.id.tvClubNombre);
            tvClubUbicacion = itemView.findViewById(R.id.tvClubUbicacion);
            container = itemView.findViewById(R.id.clubContainer);
        }
    }
}

