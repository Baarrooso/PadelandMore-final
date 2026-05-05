package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MatchNivelFragment extends Fragment {

    private final List<UsuarioComunidadModel> usuarios = new ArrayList<>();
    private UsuariosComunidadAdapter adapter;

    private FirebaseFirestore db;
    private String currentUid;
    private final Set<String> followingIds = new HashSet<>();
    private int nivelObjetivo = 3;

    private EditText edtBuscarNombre;
    private EditText edtCiudad;
    private TextView txtNivel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_match_nivel, container, false);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        txtNivel = view.findViewById(R.id.txtNivelBuscado);
        SeekBar seekNivel = view.findViewById(R.id.seekNivelBuscado);
        edtBuscarNombre = view.findViewById(R.id.edtBuscarNombreComunidad);
        edtCiudad = view.findViewById(R.id.edtCiudadBuscada);
        Button btnBuscar = view.findViewById(R.id.btnBuscarNivel);
        RecyclerView recycler = view.findViewById(R.id.recyclerComunidad);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UsuariosComunidadAdapter(usuarios, new UsuariosComunidadAdapter.OnUsuarioActionListener() {
            @Override
            public void onPerfilClick(UsuarioComunidadModel usuario) {
                abrirPerfilPublico(usuario);
            }

            @Override
            public void onSeguirClick(UsuarioComunidadModel usuario) {
                alternarSeguimiento(usuario);
            }
        });
        recycler.setAdapter(adapter);

        actualizarTextoNivel();

        seekNivel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nivelObjetivo = Math.max(1, progress + 1);
                actualizarTextoNivel();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnBuscar.setOnClickListener(v -> buscarUsuarios());
        buscarUsuarios();

        return view;
    }

    private void actualizarTextoNivel() {
        if (txtNivel != null) {
            txtNivel.setText(getString(R.string.nivel_buscado, nivelObjetivo));
        }
    }

    private void buscarUsuarios() {
        final String textoBusqueda = edtBuscarNombre.getText().toString().trim().toLowerCase(Locale.ROOT);
        final String ciudadBusqueda = edtCiudad.getText().toString().trim().toLowerCase(Locale.ROOT);

        if (currentUid == null) {
            Toast.makeText(requireContext(), "Inicia sesión para usar el seguimiento", Toast.LENGTH_SHORT).show();
        }

        db.collection("usuarios")
                .get()
                .addOnSuccessListener(snapshot -> {
                    usuarios.clear();
                    followingIds.clear();

                    if (currentUid != null) {
                        cargarSiguiendo(textoBusqueda, ciudadBusqueda, snapshot);
                    } else {
                        filtrarUsuarios(textoBusqueda, ciudadBusqueda, snapshot);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "No se pudo cargar la comunidad", Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarSiguiendo(String textoBusqueda, String ciudadBusqueda, Iterable<QueryDocumentSnapshot> snapshot) {
        db.collection("seguimientos")
                .whereEqualTo("followerUid", currentUid)
                .get()
                .addOnSuccessListener(seguimientos -> {
                    for (QueryDocumentSnapshot doc : seguimientos) {
                        String targetUid = doc.getString("followingUid");
                        if (targetUid != null) {
                            followingIds.add(targetUid);
                        }
                    }
                    filtrarUsuarios(textoBusqueda, ciudadBusqueda, snapshot);
                })
                .addOnFailureListener(e -> filtrarUsuarios(textoBusqueda, ciudadBusqueda, snapshot));
    }

    private void filtrarUsuarios(String textoBusqueda, String ciudadBusqueda, Iterable<QueryDocumentSnapshot> snapshot) {
        for (QueryDocumentSnapshot doc : snapshot) {
            String uid = doc.getId();
            if (uid.equals(currentUid)) {
                continue;
            }

            String nombre = doc.getString("nombre");
            String alias = doc.getString("alias");
            String ciudadDoc = doc.getString("ciudadPadel");
            Long nivel = doc.getLong("nivelPadel");

            if (nombre == null) {
                continue;
            }

            boolean nivelCompatible = nivel == null || Math.abs(nivel.intValue() - nivelObjetivo) <= 1;
            boolean ciudadCompatible = ciudadBusqueda.isEmpty()
                    || (ciudadDoc != null && ciudadDoc.toLowerCase(Locale.ROOT).contains(ciudadBusqueda));
            boolean textoCompatible = textoBusqueda.isEmpty()
                    || contiene(nombre, textoBusqueda)
                    || contiene(alias, textoBusqueda)
                    || contiene(ciudadDoc, textoBusqueda);

            if (nivelCompatible && ciudadCompatible && textoCompatible) {
                UsuarioComunidadModel usuario = new UsuarioComunidadModel();
                usuario.uid = uid;
                usuario.nombre = nombre;
                usuario.alias = alias;
                usuario.ciudad = ciudadDoc;
                usuario.nivelNumerico = nivel != null ? nivel : 0;
                usuario.nivelTexto = nivel != null ? "Nivel " + nivel : "";
                usuario.fotoUrl = doc.getString("fotoUrl");
                usuario.seguidoresCount = valorLong(doc.getLong("seguidoresCount"));
                usuario.seguidosCount = valorLong(doc.getLong("seguidosCount"));
                usuario.isFollowing = followingIds.contains(uid);
                usuarios.add(usuario);
            }
        }

        if (usuarios.isEmpty()) {
            UsuarioComunidadModel vacio = new UsuarioComunidadModel();
            vacio.nombre = getString(R.string.sin_resultados_comunidad);
            vacio.uid = "";
            usuarios.add(vacio);
        }

        adapter.notifyDataSetChanged();
    }

    private boolean contiene(String texto, String busqueda) {
        return texto != null && texto.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    private long valorLong(Long valor) {
        return valor != null ? valor : 0L;
    }

    private void abrirPerfilPublico(UsuarioComunidadModel usuario) {
        if (usuario.uid == null || usuario.uid.isEmpty()) {
            return;
        }

        Intent i = new Intent();
        i.setClassName(requireContext(), "com.example.tmanager.PerfilPublicoActivity");
        i.putExtra("uid", usuario.uid);
        startActivity(i);
    }

    private void alternarSeguimiento(UsuarioComunidadModel usuario) {
        if (currentUid == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para seguir usuarios", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = currentUid + "_" + usuario.uid;
        DocumentReference ref = db.collection("seguimientos").document(docId);

        if (usuario.isFollowing) {
            ref.delete()
                    .addOnSuccessListener(unused -> {
                        actualizarContadoresSeguimiento(usuario.uid, false);
                        usuario.isFollowing = false;
                        usuario.seguidoresCount = Math.max(0, usuario.seguidoresCount - 1);
                        adapter.notifyDataSetChanged();
                    });
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("followerUid", currentUid);
            data.put("followingUid", usuario.uid);
            data.put("createdAt", FieldValue.serverTimestamp());

            ref.set(data)
                    .addOnSuccessListener(unused -> {
                        actualizarContadoresSeguimiento(usuario.uid, true);
                        usuario.isFollowing = true;
                        usuario.seguidoresCount += 1;
                        adapter.notifyDataSetChanged();
                    });
        }
    }

    private void actualizarContadoresSeguimiento(String targetUid, boolean seguir) {
        Map<String, Object> incremento = new HashMap<>();
        incremento.put("seguidosCount", FieldValue.increment(seguir ? 1 : -1));

        Map<String, Object> incrementoDestino = new HashMap<>();
        incrementoDestino.put("seguidoresCount", FieldValue.increment(seguir ? 1 : -1));

        if (currentUid != null) {
            db.collection("usuarios").document(currentUid).update(incremento);
        }
        db.collection("usuarios").document(targetUid).update(incrementoDestino);
    }
}

