package com.example.tmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilPublicoActivity extends AppCompatActivity {

    private final List<Map<String, String>> partidos = new ArrayList<>();
    private final List<PublicacionModel> publicaciones = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null;

    private String targetUid;
    private boolean isFollowing;

    private ImageView imgFotoPerfil;
    private TextView txtNombre, txtAlias, txtNivel, txtPartidos, txtSeguidores, txtSeguidos, txtVacio;
    private Button btnSeguir;
    private PartidosAdapter partidosAdapter;
    private PublicacionesAdapter publicacionesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_publico);

        targetUid = getIntent().getStringExtra("uid");
        if (targetUid == null || targetUid.trim().isEmpty()) {
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btnRetrocesoPerfilPublico);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfilPublico);
        txtNombre = findViewById(R.id.txtNombrePerfilPublico);
        txtAlias = findViewById(R.id.txtAliasPerfilPublico);
        txtNivel = findViewById(R.id.txtNivelPerfilPublico);
        txtPartidos = findViewById(R.id.txtPartidosPerfilPublico);
        txtSeguidores = findViewById(R.id.txtSeguidoresPerfilPublico);
        txtSeguidos = findViewById(R.id.txtSeguidosPerfilPublico);
        txtVacio = findViewById(R.id.txtVacioPerfilPublico);
        btnSeguir = findViewById(R.id.btnSeguirPerfilPublico);

        RecyclerView recyclerPartidos = findViewById(R.id.recyclerPartidosPerfilPublico);
        RecyclerView recyclerPublicaciones = findViewById(R.id.recyclerPublicacionesPerfilPublico);

        recyclerPartidos.setLayoutManager(new LinearLayoutManager(this));
        partidosAdapter = new PartidosAdapter(partidos);
        recyclerPartidos.setAdapter(partidosAdapter);

        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(this));
        publicacionesAdapter = new PublicacionesAdapter(publicaciones);
        recyclerPublicaciones.setAdapter(publicacionesAdapter);

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnSeguir.setOnClickListener(v -> alternarSeguimiento());

        cargarPerfil();
    }

    private void cargarPerfil() {
        db.collection("usuarios").document(targetUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String nombre = doc.getString("nombre");
                    String alias = doc.getString("alias");
                    String fotoUrl = doc.getString("fotoUrl");
                    String nivel = doc.getString("nivel");
                    Long nivelPadel = doc.getLong("nivelPadel");
                    long seguidoresCount = valorLong(doc.getLong("seguidoresCount"));
                    long seguidosCount = valorLong(doc.getLong("seguidosCount"));

                    txtNombre.setText(nombre != null ? nombre : "Sin nombre");
                    txtAlias.setText(alias != null && !alias.trim().isEmpty() ? "@" + alias : getString(R.string.tu_perfil));
                    txtNivel.setText(nivel != null ? nivel : (nivelPadel != null ? "Nivel " + nivelPadel : ""));
                    txtSeguidores.setText(String.valueOf(seguidoresCount));
                    txtSeguidos.setText(String.valueOf(seguidosCount));

                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Glide.with(this).load(fotoUrl).circleCrop().into(imgFotoPerfil);
                    } else {
                        imgFotoPerfil.setImageResource(R.drawable.userlogo);
                    }

                    if (currentUid != null && !currentUid.equals(targetUid)) {
                        btnSeguir.setVisibility(View.VISIBLE);
                        cargarEstadoSeguimiento();
                    } else {
                        btnSeguir.setVisibility(View.GONE);
                    }

                    cargarPartidos();
                    cargarPublicaciones();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void cargarEstadoSeguimiento() {
        if (currentUid == null) return;

        db.collection("seguimientos")
                .document(currentUid + "_" + targetUid)
                .get()
                .addOnSuccessListener(doc -> {
                    isFollowing = doc.exists();
                    actualizarTextoBotonSeguir();
                });
    }

    private void actualizarTextoBotonSeguir() {
        btnSeguir.setText(isFollowing ? R.string.siguiendo : R.string.seguir);
    }

    private void alternarSeguimiento() {
        if (currentUid == null) {
            Toast.makeText(this, "Debes iniciar sesión para seguir", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference ref = db.collection("seguimientos").document(currentUid + "_" + targetUid);

        if (isFollowing) {
            ref.delete().addOnSuccessListener(unused -> {
                isFollowing = false;
                actualizarTextoBotonSeguir();
                cambiarContadoresSeguimiento(false);
            });
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("followerUid", currentUid);
            data.put("followingUid", targetUid);
            data.put("createdAt", FieldValue.serverTimestamp());

            ref.set(data).addOnSuccessListener(unused -> {
                isFollowing = true;
                actualizarTextoBotonSeguir();
                cambiarContadoresSeguimiento(true);
            });
        }
    }

    private void cambiarContadoresSeguimiento(boolean seguir) {
        Map<String, Object> current = new HashMap<>();
        current.put("seguidosCount", FieldValue.increment(seguir ? 1 : -1));

        Map<String, Object> target = new HashMap<>();
        target.put("seguidoresCount", FieldValue.increment(seguir ? 1 : -1));

        db.collection("usuarios").document(currentUid).update(current);
        db.collection("usuarios").document(targetUid).update(target)
                .addOnSuccessListener(unused -> cargarPerfil());
    }

    private void cargarPartidos() {
        partidos.clear();

        db.collection("partidos")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (!esPartidoDelUsuario(doc)) {
                            continue;
                        }

                        Map<String, String> partido = new HashMap<>();
                        partido.put("rival", obtenerRival(doc));
                        partido.put("resultado", valorTexto(doc.getString("resultado"), "Pendiente"));
                        partido.put("fecha", valorTexto(doc.getString("fecha"), ""));
                        partidos.add(partido);
                    }

                    txtPartidos.setText(String.valueOf(partidos.size()));
                    partidosAdapter.notifyDataSetChanged();
                    actualizarEstadoVacio();
                });
    }

    private void cargarPublicaciones() {
        publicaciones.clear();

        db.collection("publicaciones")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (!esPublicacionDelUsuario(doc)) {
                            continue;
                        }

                        PublicacionModel publicacion = new PublicacionModel();
                        publicacion.titulo = valorTexto(doc.getString("titulo"), "Publicación");
                        publicacion.texto = valorTexto(doc.getString("texto"), valorTexto(doc.getString("contenido"), ""));
                        publicacion.autorNombre = valorTexto(doc.getString("autorNombre"), txtNombre.getText().toString());
                        publicacion.timestamp = doc.getTimestamp("timestamp");
                        publicaciones.add(publicacion);
                    }

                    publicacionesAdapter.notifyDataSetChanged();
                    actualizarEstadoVacio();
                });
    }

    private void actualizarEstadoVacio() {
        if (partidos.isEmpty() && publicaciones.isEmpty()) {
            txtVacio.setVisibility(View.VISIBLE);
            txtVacio.setText(R.string.sin_contenido_perfil_publico);
        } else {
            txtVacio.setVisibility(View.GONE);
        }
    }

    private boolean esPartidoDelUsuario(QueryDocumentSnapshot doc) {
        String jugador1Uid = doc.getString("jugador1Uid");
        String jugador2Uid = doc.getString("jugador2Uid");
        String jugadorUid = doc.getString("jugadorUid");
        return targetUid.equals(jugador1Uid)
                || targetUid.equals(jugador2Uid)
                || targetUid.equals(jugadorUid);
    }

    private boolean esPublicacionDelUsuario(QueryDocumentSnapshot doc) {
        String autorUid = doc.getString("autorUid");
        String userUid = doc.getString("userUid");
        String uid = doc.getString("uid");
        return targetUid.equals(autorUid)
                || targetUid.equals(userUid)
                || targetUid.equals(uid);
    }

    private String obtenerRival(QueryDocumentSnapshot doc) {
        String jugador1Uid = doc.getString("jugador1Uid");
        String jugador2Uid = doc.getString("jugador2Uid");
        String jugador1Nombre = doc.getString("jugador1Nombre");
        String jugador2Nombre = doc.getString("jugador2Nombre");

        if (targetUid.equals(jugador1Uid) && jugador2Nombre != null) {
            return jugador2Nombre;
        }
        if (targetUid.equals(jugador2Uid) && jugador1Nombre != null) {
            return jugador1Nombre;
        }
        if (jugador2Nombre != null) {
            return jugador2Nombre;
        }
        if (jugador1Nombre != null) {
            return jugador1Nombre;
        }
        return "Rival";
    }

    private long valorLong(Long valor) {
        return valor != null ? valor : 0L;
    }

    private String valorTexto(String valor, String fallback) {
        return valor != null && !valor.trim().isEmpty() ? valor : fallback;
    }
}

