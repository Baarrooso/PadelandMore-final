package com.example.padelandmore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UsuariosComunidadAdapter extends RecyclerView.Adapter<UsuariosComunidadAdapter.UsuarioVH> {

    public interface OnUsuarioActionListener {
        void onPerfilClick(UsuarioComunidadModel usuario);
        void onSeguirClick(UsuarioComunidadModel usuario);
    }

    private final List<UsuarioComunidadModel> usuarios;
    private final OnUsuarioActionListener listener;

    public UsuariosComunidadAdapter(List<UsuarioComunidadModel> usuarios, OnUsuarioActionListener listener) {
        this.usuarios = usuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_companero, parent, false);
        return new UsuarioVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioVH holder, int position) {
        UsuarioComunidadModel usuario = usuarios.get(position);

        holder.txtNombre.setText(usuario.nombre != null ? usuario.nombre : "Sin nombre");
        holder.txtMeta.setText(formatearMeta(usuario));
        holder.txtSeguidores.setText(holder.itemView.getContext().getString(R.string.seguidores_count, usuario.seguidoresCount));

        boolean esPlaceholder = usuario.uid == null || usuario.uid.isEmpty();

        if (!esPlaceholder && usuario.fotoUrl != null && !usuario.fotoUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(usuario.fotoUrl)
                    .circleCrop()
                    .into(holder.imgFoto);
        } else {
            holder.imgFoto.setImageResource(R.drawable.userlogo);
        }

        holder.btnSeguir.setVisibility(esPlaceholder ? View.GONE : View.VISIBLE);
        if (!esPlaceholder) {
            holder.btnSeguir.setText(usuario.isFollowing ? "Siguiendo" : "Seguir");
            holder.btnSeguir.setActivated(usuario.isFollowing);
        }

        holder.itemView.setOnClickListener(esPlaceholder ? null : v -> listener.onPerfilClick(usuario));
        holder.btnSeguir.setOnClickListener(esPlaceholder ? null : v -> listener.onSeguirClick(usuario));
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    private String formatearMeta(UsuarioComunidadModel usuario) {
        StringBuilder sb = new StringBuilder();
        if (usuario.alias != null && !usuario.alias.trim().isEmpty()) {
            sb.append("@").append(usuario.alias.trim());
        }
        if (usuario.nivelTexto != null && !usuario.nivelTexto.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(usuario.nivelTexto.trim());
        }
        if (usuario.ciudad != null && !usuario.ciudad.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(usuario.ciudad.trim());
        }
        return sb.length() > 0 ? sb.toString() : "Jugador de pádel";
    }

    static class UsuarioVH extends RecyclerView.ViewHolder {
        ImageView imgFoto;
        TextView txtNombre, txtMeta, txtSeguidores;
        Button btnSeguir;

        UsuarioVH(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imgCompatComunidad);
            txtNombre = itemView.findViewById(R.id.txtNombreCompanero);
            txtMeta = itemView.findViewById(R.id.txtMetaCompanero);
            txtSeguidores = itemView.findViewById(R.id.txtSeguidoresCompanero);
            btnSeguir = itemView.findViewById(R.id.btnSeguirCompanero);
        }
    }
}

