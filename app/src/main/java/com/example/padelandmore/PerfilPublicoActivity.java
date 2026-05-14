package com.example.padelandmore;

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
import com.example.padelandmore.network.Backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilPublicoActivity extends AppCompatActivity {

    private final List<Map<String, String>> partidos = new ArrayList<>();
    private final List<PublicacionModel> publicaciones = new ArrayList<>();

    private String currentUid;
    private String targetUid;
    private boolean isFollowing;
    private String followId;

    private ImageView imgFotoPerfil;
    private TextView txtNombre, txtAlias, txtNivel, txtPartidos, txtSeguidores, txtSeguidos, txtVacio;
    private Button btnSeguir;
    private PartidosAdapter partidosAdapter;
    private PublicacionesAdapter publicacionesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_publico);

        currentUid = Backend.getCurrentUid(this);
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
        partidosAdapter = new PartidosAdapter(this, partidos);
        recyclerPartidos.setAdapter(partidosAdapter);

        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(this));
        publicacionesAdapter = new PublicacionesAdapter(publicaciones);
        recyclerPublicaciones.setAdapter(publicacionesAdapter);

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnSeguir.setOnClickListener(v -> alternarSeguimiento());

        cargarPerfil();
    }

    private void cargarPerfil() {
        Backend.getUser(this, targetUid, resp -> {
            String nombre = texto(resp.get("nombre"), "Sin nombre");
            String alias = texto(resp.get("alias"), null);
            String fotoUrl = texto(resp.get("fotoUrl"), null);
            String nivel = texto(resp.get("nivel"), null);
            Long nivelPadel = numero(resp.get("nivelPadel"));
            long seguidoresCount = valorLong(resp.get("seguidoresCount"));
            long seguidosCount = valorLong(resp.get("seguidosCount"));

            txtNombre.setText(nombre);
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
        }, t -> {
            Toast.makeText(this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void cargarEstadoSeguimiento() {
        if (currentUid == null) return;

        Backend.getCollection(this, "seguimientos", resp -> {
            isFollowing = false;
            followId = null;

            Object data = resp.get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (!(item instanceof Map)) continue;

                    @SuppressWarnings("unchecked")
                    Map<String, Object> doc = (Map<String, Object>) item;
                    if (currentUid.equals(texto(doc.get("followerUid"), null)) && targetUid.equals(texto(doc.get("followingUid"), null))) {
                        isFollowing = true;
                        followId = texto(doc.get("id"), null);
                        break;
                    }
                }
            }

            actualizarTextoBotonSeguir();
        }, t -> actualizarTextoBotonSeguir());
    }

    private void actualizarTextoBotonSeguir() {
        btnSeguir.setText(isFollowing ? R.string.siguiendo : R.string.seguir);
    }

    private void alternarSeguimiento() {
        if (currentUid == null) {
            Toast.makeText(this, "Debes iniciar sesión para seguir", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFollowing) {
            if (followId == null) {
                return;
            }

            Backend.deleteCollectionItem(this, "seguimientos", followId, resp -> {
                isFollowing = false;
                actualizarTextoBotonSeguir();
                cambiarContadoresSeguimiento(false);
            }, t -> Toast.makeText(this, "No se pudo dejar de seguir", Toast.LENGTH_SHORT).show());
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("followerUid", currentUid);
            data.put("followingUid", targetUid);
            data.put("createdAt", System.currentTimeMillis());

            Backend.postToCollection(this, "seguimientos", data, resp -> {
                Object created = resp.get("data");
                if (created instanceof Map) {
                    followId = texto(((Map<?, ?>) created).get("id"), null);
                }

                isFollowing = true;
                actualizarTextoBotonSeguir();
                cambiarContadoresSeguimiento(true);
            }, t -> Toast.makeText(this, "No se pudo seguir", Toast.LENGTH_SHORT).show());
        }
    }

    private void cambiarContadoresSeguimiento(boolean seguir) {
        int delta = seguir ? 1 : -1;

        Backend.getUser(this, currentUid, resp -> {
            Map<String, Object> update = new HashMap<>();
            update.put("seguidosCount", Math.max(0, valorLong(resp.get("seguidosCount")) + delta));
            update.put("actualizado", System.currentTimeMillis());
            Backend.updateCollectionItem(this, "users", currentUid, update, r -> {}, t -> {});
        }, t -> {});

        Backend.getUser(this, targetUid, resp -> {
            Map<String, Object> update = new HashMap<>();
            update.put("seguidoresCount", Math.max(0, valorLong(resp.get("seguidoresCount")) + delta));
            update.put("actualizado", System.currentTimeMillis());
            Backend.updateCollectionItem(this, "users", targetUid, update, r -> cargarPerfil(), t -> {});
        }, t -> {});
    }

    private void cargarPartidos() {
        partidos.clear();

        Backend.getCollection(this, "partidos", resp -> {
            Object data = resp.get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (!(item instanceof Map)) continue;

                    @SuppressWarnings("unchecked")
                    Map<String, Object> doc = (Map<String, Object>) item;
                    if (!esPartidoDelUsuario(doc)) {
                        continue;
                    }

                    Map<String, String> partido = new HashMap<>();
                    partido.put("rival", obtenerRival(doc));
                    partido.put("resultado", texto(doc.get("resultado"), "Pendiente"));
                    partido.put("fecha", texto(doc.get("fecha"), ""));
                    partidos.add(partido);
                }
            }

            txtPartidos.setText(String.valueOf(partidos.size()));
            partidosAdapter.notifyDataSetChanged();
            actualizarEstadoVacio();
        }, t -> {});
    }

    private void cargarPublicaciones() {
        publicaciones.clear();

        Backend.getCollection(this, "publicaciones", resp -> {
            Object data = resp.get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (!(item instanceof Map)) continue;

                    @SuppressWarnings("unchecked")
                    Map<String, Object> doc = (Map<String, Object>) item;
                    if (!esPublicacionDelUsuario(doc)) {
                        continue;
                    }

                    PublicacionModel publicacion = new PublicacionModel();
                    publicacion.titulo = texto(doc.get("titulo"), "Publicación");
                    publicacion.texto = texto(doc.get("texto"), texto(doc.get("contenido"), ""));
                    publicacion.autorNombre = texto(doc.get("autorNombre"), txtNombre.getText().toString());

                    Long ts = numero(doc.get("timestamp"));
                    if (ts == null) {
                        ts = numero(doc.get("creado"));
                    }
                    if (ts != null) {
                        publicacion.timestamp = new com.google.firebase.Timestamp(new java.util.Date(ts));
                    }

                    publicaciones.add(publicacion);
                }
            }

            publicacionesAdapter.notifyDataSetChanged();
            actualizarEstadoVacio();
        }, t -> {});
    }

    private void actualizarEstadoVacio() {
        if (partidos.isEmpty() && publicaciones.isEmpty()) {
            txtVacio.setVisibility(View.VISIBLE);
            txtVacio.setText(R.string.sin_contenido_perfil_publico);
        } else {
            txtVacio.setVisibility(View.GONE);
        }
    }

    private boolean esPartidoDelUsuario(Map<String, Object> doc) {
        String jugador1Uid = texto(doc.get("jugador1Uid"), null);
        String jugador2Uid = texto(doc.get("jugador2Uid"), null);
        String jugadorUid = texto(doc.get("jugadorUid"), null);
        return targetUid.equals(jugador1Uid)
                || targetUid.equals(jugador2Uid)
                || targetUid.equals(jugadorUid)
                || targetUid.equals(texto(doc.get("userUid"), null));
    }

    private boolean esPublicacionDelUsuario(Map<String, Object> doc) {
        String autorUid = texto(doc.get("autorUid"), null);
        String userUid = texto(doc.get("userUid"), null);
        String uid = texto(doc.get("uid"), null);
        return targetUid.equals(autorUid)
                || targetUid.equals(userUid)
                || targetUid.equals(uid);
    }

    private String obtenerRival(Map<String, Object> doc) {
        String jugador1Uid = texto(doc.get("jugador1Uid"), null);
        String jugador2Uid = texto(doc.get("jugador2Uid"), null);
        String jugador1Nombre = texto(doc.get("jugador1Nombre"), null);
        String jugador2Nombre = texto(doc.get("jugador2Nombre"), null);

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

    private long valorLong(Object valor) {
        Long num = numero(valor);
        return num != null ? num : 0L;
    }

    private Long numero(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String texto(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String texto = value.toString();
        return texto.isEmpty() ? fallback : texto;
    }
}
