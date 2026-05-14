package com.example.padelandmore;

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

import com.example.padelandmore.network.Backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MatchNivelFragment extends Fragment {

    private final List<UsuarioComunidadModel> usuarios = new ArrayList<>();
    private final Set<String> followingIds = new HashSet<>();
    private final Map<String, String> followDocIds = new HashMap<>();

    private UsuariosComunidadAdapter adapter;
    private String currentUid;
    private int currentSeguidosCount = 0;
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

        currentUid = Backend.getCurrentUid(requireContext());

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

        if (currentUid != null) {
            Backend.getUser(requireContext(), currentUid, resp -> {
                Number seguidos = numberValue(resp.get("seguidosCount"));
                currentSeguidosCount = seguidos == null ? 0 : seguidos.intValue();
            }, t -> {});
        }

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

        Backend.getCollection(requireContext(), "users", resp -> {
            usuarios.clear();
            followingIds.clear();
            followDocIds.clear();

            if (currentUid != null) {
                cargarSiguiendoYFiltrar(textoBusqueda, ciudadBusqueda, resp);
            } else {
                filtrarUsuarios(textoBusqueda, ciudadBusqueda, resp);
            }
        }, t -> Toast.makeText(requireContext(), "No se pudo cargar la comunidad", Toast.LENGTH_SHORT).show());
    }

    private void cargarSiguiendoYFiltrar(String textoBusqueda, String ciudadBusqueda, Map<String, Object> usersResponse) {
        Backend.getCollection(requireContext(), "seguimientos", resp -> {
            for (Map<String, Object> doc : collectionData(resp)) {
                String followerUid = valueOf(doc.get("followerUid"));
                String followingUid = valueOf(doc.get("followingUid"));
                String id = valueOf(doc.get("id"));

                if (currentUid != null && currentUid.equals(followerUid) && followingUid != null) {
                    followingIds.add(followingUid);
                    if (id != null) {
                        followDocIds.put(followingUid, id);
                    }
                }
            }

            filtrarUsuarios(textoBusqueda, ciudadBusqueda, usersResponse);
        }, t -> filtrarUsuarios(textoBusqueda, ciudadBusqueda, usersResponse));
    }

    private void filtrarUsuarios(String textoBusqueda, String ciudadBusqueda, Map<String, Object> response) {
        for (Map<String, Object> doc : collectionData(response)) {
            String uid = valueOf(doc.get("uid"));
            if (uid == null || uid.equals(currentUid)) {
                continue;
            }

            String nombre = valueOf(doc.get("nombre"));
            String alias = valueOf(doc.get("alias"));
            String ciudadDoc = valueOf(doc.get("ciudadPadel"));
            Number nivel = numberValue(doc.get("nivelPadel"));

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
                usuario.nivelNumerico = nivel != null ? nivel.longValue() : 0L;
                usuario.nivelTexto = nivel != null ? "Nivel " + nivel : "";
                usuario.fotoUrl = valueOf(doc.get("fotoUrl"));
                usuario.seguidoresCount = valorLong(numberValue(doc.get("seguidoresCount")));
                usuario.seguidosCount = valorLong(numberValue(doc.get("seguidosCount")));
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

    private long valorLong(Number valor) {
        return valor != null ? valor.longValue() : 0L;
    }

    private List<Map<String, Object>> collectionData(Map<String, Object> response) {
        List<Map<String, Object>> result = new ArrayList<>();
        Object data = response.get("data");
        if (data instanceof List) {
            for (Object item : (List<?>) data) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) item;
                    result.add(map);
                }
            }
        }
        return result;
    }

    private void abrirPerfilPublico(UsuarioComunidadModel usuario) {
        if (usuario.uid == null || usuario.uid.isEmpty()) {
            return;
        }

        Intent i = new Intent();
        i.setClassName(requireContext(), "com.example.padelandmore.PerfilPublicoActivity");
        i.putExtra("uid", usuario.uid);
        startActivity(i);
    }

    private void alternarSeguimiento(UsuarioComunidadModel usuario) {
        if (currentUid == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para seguir usuarios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (usuario.isFollowing) {
            String followId = followDocIds.get(usuario.uid);
            if (followId == null) {
                return;
            }

            Backend.deleteCollectionItem(requireContext(), "seguimientos", followId, resp -> {
                usuario.isFollowing = false;
                usuario.seguidoresCount = Math.max(0, usuario.seguidoresCount - 1);
                followingIds.remove(usuario.uid);
                followDocIds.remove(usuario.uid);
                actualizarContadoresSeguimiento(usuario.uid, false);
                adapter.notifyDataSetChanged();
            }, t -> Toast.makeText(requireContext(), "No se pudo dejar de seguir", Toast.LENGTH_SHORT).show());
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("followerUid", currentUid);
            data.put("followingUid", usuario.uid);
            data.put("createdAt", System.currentTimeMillis());

            Backend.postToCollection(requireContext(), "seguimientos", data, resp -> {
                Object created = resp.get("data");
                if (created instanceof Map) {
                    String id = valueOf(((Map<?, ?>) created).get("id"));
                    if (id != null) {
                        followDocIds.put(usuario.uid, id);
                    }
                }

                usuario.isFollowing = true;
                usuario.seguidoresCount += 1;
                followingIds.add(usuario.uid);
                actualizarContadoresSeguimiento(usuario.uid, true);
                adapter.notifyDataSetChanged();
            }, t -> Toast.makeText(requireContext(), "No se pudo seguir al usuario", Toast.LENGTH_SHORT).show());
        }
    }

    private void actualizarContadoresSeguimiento(String targetUid, boolean seguir) {
        int delta = seguir ? 1 : -1;

        if (currentUid != null) {
            Backend.getUser(requireContext(), currentUid, resp -> {
                Number actual = numberValue(resp.get("seguidosCount"));
                Map<String, Object> update = new HashMap<>();
                update.put("seguidosCount", Math.max(0, (actual == null ? 0 : actual.intValue()) + delta));
                update.put("actualizado", System.currentTimeMillis());
                Backend.updateCollectionItem(requireContext(), "users", currentUid, update, r -> {}, t -> {});
            }, t -> {});
        }

        Backend.getUser(requireContext(), targetUid, resp -> {
            Number actual = numberValue(resp.get("seguidoresCount"));
            Map<String, Object> update = new HashMap<>();
            update.put("seguidoresCount", Math.max(0, (actual == null ? 0 : actual.intValue()) + delta));
            update.put("actualizado", System.currentTimeMillis());
            Backend.updateCollectionItem(requireContext(), "users", targetUid, update, r -> {}, t -> {});
        }, t -> {});
    }

    private String valueOf(Object value) {
        return value == null ? null : value.toString();
    }

    private Number numberValue(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }

        if (value == null) {
            return null;
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
