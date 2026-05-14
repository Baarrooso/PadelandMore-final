package com.example.padelandmore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CompanerosAdapter extends RecyclerView.Adapter<CompanerosAdapter.CompaneroViewHolder> {

    private List<String> companeros;

    public CompanerosAdapter(List<String> companeros) {
        this.companeros = companeros;
    }

    @NonNull
    @Override
    public CompaneroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_companero, parent, false);
        return new CompaneroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompaneroViewHolder holder, int position) {
        holder.txtNombreCompanero.setText(companeros.get(position));
    }

    @Override
    public int getItemCount() {
        return companeros.size();
    }

    static class CompaneroViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombreCompanero;

        CompaneroViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreCompanero = itemView.findViewById(R.id.txtNombreCompanero);
        }
    }
}

