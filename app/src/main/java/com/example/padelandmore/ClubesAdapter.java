package com.example.padelandmore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClubesAdapter extends RecyclerView.Adapter<ClubesAdapter.ClubViewHolder> {

    private List<String> clubes;

    public ClubesAdapter(List<String> clubes) {
        this.clubes = clubes;
    }

    @NonNull
    @Override
    public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_club, parent, false);
        return new ClubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {
        holder.txtNombreClub.setText(clubes.get(position));
    }

    @Override
    public int getItemCount() {
        return clubes.size();
    }

    static class ClubViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombreClub;

        ClubViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreClub = itemView.findViewById(R.id.txtNombreClub);
        }
    }
}

