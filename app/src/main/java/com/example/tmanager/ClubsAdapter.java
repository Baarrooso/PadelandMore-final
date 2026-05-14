package com.example.tmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
        // Mostrar nombre y ubicación en dos líneas dentro del botón
        String text = club.getNombre() + "\n" + club.getUbicacion();
        holder.btnClubItem.setText(text);

        holder.btnClubItem.setOnClickListener(v -> {
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
        Button btnClubItem;

        public ClubViewHolder(@NonNull View itemView) {
            super(itemView);
            btnClubItem = itemView.findViewById(R.id.btnClubItem);
        }
    }
}

