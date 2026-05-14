package com.example.padelandmore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PublicacionesAdapter extends RecyclerView.Adapter<PublicacionesAdapter.PublicacionVH> {

    private final List<PublicacionModel> publicaciones;

    public PublicacionesAdapter(List<PublicacionModel> publicaciones) {
        this.publicaciones = publicaciones;
    }

    @NonNull
    @Override
    public PublicacionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evento, parent, false);
        return new PublicacionVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionVH holder, int position) {
        PublicacionModel publicacion = publicaciones.get(position);
        holder.txtTitulo.setText(publicacion.titulo != null && !publicacion.titulo.isEmpty() ? publicacion.titulo : "Publicación");
        holder.txtTexto.setText(publicacion.texto != null ? publicacion.texto : "");
        holder.txtAutor.setText(publicacion.autorNombre != null ? publicacion.autorNombre : "");
        holder.txtFecha.setText(formatear(publicacion.timestamp));
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    static class PublicacionVH extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtTexto, txtAutor, txtFecha;

        PublicacionVH(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtTexto = itemView.findViewById(R.id.txtLugar);
            txtAutor = itemView.findViewById(R.id.txtFechaHora);
            txtFecha = itemView.findViewById(R.id.txtAsistencia);
        }
    }

    private String formatear(Timestamp timestamp) {
        if (timestamp == null) return "";
        long diff = System.currentTimeMillis() - timestamp.toDate().getTime();
        long minutos = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutos < 1) return "Ahora";
        if (minutos < 60) return minutos + " min";
        long horas = TimeUnit.MILLISECONDS.toHours(diff);
        if (horas < 24) return horas + " h";
        long dias = TimeUnit.MILLISECONDS.toDays(diff);
        return dias + " d";
    }
}

