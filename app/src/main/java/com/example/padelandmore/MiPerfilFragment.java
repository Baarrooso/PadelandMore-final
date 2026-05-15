package com.example.padelandmore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.padelandmore.network.AWSConnection;
import com.example.padelandmore.network.Backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class MiPerfilFragment extends Fragment {

    private Spinner spinnerNivel, spinnerMano;
    private String currentUid;
    private static final int NIVEL_MAXIMO = 5;

    // Vistas de datos
    private TextView txtNombreUsuario, txtPartidosCount, txtSeguidoresCount, txtSeguidosCount;
    private TextView txtFiabilidadPorcentaje, txtNivelActual, txtPuntosJuego;
    private TextView txtPartidosGanados, txtPartidosPerdidos, txtEfectividadPorcentaje;
    private ImageView imgFotoPerfil, imgNivelPadel;
    private RecyclerView recyclerUltimosPartidos, recyclerCompaneros, recyclerClubes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mi_perfil, container, false);

        currentUid = Backend.getCurrentUid(requireContext());

        // Inicializar todas las vistas
        inicializarVistas(v);

        // Cargar datos del usuario
        // Cargar datos una vez la vista ya está lista
        if (isAdded()) {
            cargarDatosUsuario();
            cargarEstadisticas();
            cargarUltimosPartidos();
            cargarCompanerosHabituales();
            cargarClubesHabituales();
        }

        return v;
    }

    private void inicializarVistas(View v) {
        // Textos
        txtNombreUsuario = v.findViewById(R.id.txtNombreUsuario);
        txtPartidosCount = v.findViewById(R.id.txtPartidosCount);
        txtSeguidoresCount = v.findViewById(R.id.txtSeguidoresCount);
        txtSeguidosCount = v.findViewById(R.id.txtSeguidosCount);
        txtFiabilidadPorcentaje = v.findViewById(R.id.txtFiabilidadPorcentaje);
        txtNivelActual = v.findViewById(R.id.txtNivelActual);
        txtPuntosJuego = v.findViewById(R.id.txtPuntosJuego);
        txtPartidosGanados = v.findViewById(R.id.txtPartidosGanados);
        txtPartidosPerdidos = v.findViewById(R.id.txtPartidosPerdidos);
        txtEfectividadPorcentaje = v.findViewById(R.id.txtEfectividadPorcentaje);

        // Imágenes
        imgFotoPerfil = v.findViewById(R.id.imgFotoPerfil);
        imgNivelPadel = v.findViewById(R.id.imgNivelPadel);

        // RecyclerViews
        recyclerUltimosPartidos = v.findViewById(R.id.recyclerUltimosPartidos);
        recyclerCompaneros = v.findViewById(R.id.recyclerCompaneros);
        recyclerClubes = v.findViewById(R.id.recyclerClubes);

        recyclerUltimosPartidos.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCompaneros.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerClubes.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Spinners
        spinnerNivel = v.findViewById(R.id.spinnerNivel);
        spinnerMano = v.findViewById(R.id.spinnerMano);

        spinnerNivel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= NIVEL_MAXIMO) {
                    spinnerNivel.setSelection(NIVEL_MAXIMO - 1);
                    Toast.makeText(requireContext(), "Nivel máximo: Profesional", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Botones
        ImageButton btnCambiarFoto = v.findViewById(R.id.btnCambiarFoto);

        btnCambiarFoto.setOnClickListener(v1 -> Toast.makeText(requireContext(), "Funcionalidad próximamente", Toast.LENGTH_SHORT).show());
    }

    private void cargarDatosUsuario() {
        if (currentUid == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cargar datos directamente desde AWS RDS
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            // Reutilizamos obtenerRolUsuario o similar, pero mejor traemos el usuario completo
            AWSConnection.Usuario user = AWSConnection.obtenerUsuarioCompleto(currentUid);
            if (isAdded() && user != null) {
                requireActivity().runOnUiThread(() -> {
                    txtNombreUsuario.setText(user.nombre);
                    if (user.nivel != null) {
                        int posicion = getIndexInArray(R.array.niveles_padel, user.nivel);
                        spinnerNivel.setSelection(posicion);
                    }
                    if (user.mano != null) {
                        int posicion = getIndexInArray(R.array.mano_juego, user.mano);
                        spinnerMano.setSelection(posicion);
                    }
                    if (user.nivelPadel != null) {
                        txtNivelActual.setText(String.format(Locale.getDefault(), "%.1f", user.nivelPadel));
                    }
                    txtSeguidoresCount.setText(String.valueOf(user.seguidoresCount));
                    txtSeguidosCount.setText(String.valueOf(user.seguidosCount));
                });
            }
        });
    }

    private void cargarEstadisticas() {
        if (currentUid == null) return;

        Backend.getCollection(requireContext(), "partidos", resp -> {
            if (!isAdded()) return;
            int ganados = 0;
            int perdidos = 0;

            for (Map<String, Object> doc : collectionData(resp)) {
                if (!currentUid.equals(valueOf(doc.get("jugador1Uid")))) {
                    continue;
                }

                String resultado = valueOf(doc.get("resultado"));
                if ("victoria".equalsIgnoreCase(resultado)) {
                    ganados++;
                } else if ("derrota".equalsIgnoreCase(resultado)) {
                    perdidos++;
                }
            }

            int total = ganados + perdidos;
            int efectividad = total > 0 ? (ganados * 100) / total : 0;

            txtPartidosCount.setText(String.valueOf(total));
            txtPartidosGanados.setText(String.valueOf(ganados));
            txtPartidosPerdidos.setText(String.valueOf(perdidos));
            txtEfectividadPorcentaje.setText(String.format(Locale.getDefault(), "%d%%", efectividad));
            txtPuntosJuego.setText("+2.1 / -1.8");
        }, t -> {});
    }

    private void cargarUltimosPartidos() {
        if (currentUid == null) return;

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            List<Map<String, String>> reservas = AWSConnection.obtenerReservasUsuario(currentUid);
            List<Map<String, String>> partidos = new ArrayList<>();

            for (Map<String, String> res : reservas) {
                Map<String, String> partido = new HashMap<>();
                String rival = res.get("rival");
                partido.put("rival", (rival != null && !rival.isEmpty()) ? rival : "Pendiente de rival");
                partido.put("club", res.get("club"));
                partido.put("pista", res.get("pista"));
                partido.put("resultado", res.get("resultado"));
                partido.put("fecha", res.get("dia") + " " + res.get("hora"));
                partido.put("partidoId", res.get("id"));
                partidos.add(partido);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (!partidos.isEmpty()) {
                        List<Map<String, String>> displayList = (partidos.size() > 5) ? partidos.subList(0, 5) : partidos;
                        PartidosAdapter adapter = new PartidosAdapter(requireContext(), displayList);
                        recyclerUltimosPartidos.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private void cargarCompanerosHabituales() {
        if (currentUid == null) return;

        Backend.getCollection(requireContext(), "partidos", resp -> {
            Map<String, Integer> companeros = new HashMap<>();

            for (Map<String, Object> doc : collectionData(resp)) {
                if (!currentUid.equals(valueOf(doc.get("jugador1Uid")))) {
                    continue;
                }

                String rival = valueOf(doc.get("jugador2Nombre"));
                if (rival != null) {
                    companeros.put(rival, companeros.getOrDefault(rival, 0) + 1);
                }
            }

            List<String> top5 = new ArrayList<>();
            companeros.entrySet().stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(5)
                    .forEach(e -> top5.add(e.getKey()));

            if (!top5.isEmpty()) {
                CompanerosAdapter adapter = new CompanerosAdapter(top5);
                recyclerCompaneros.setAdapter(adapter);
            }
        }, t -> {});
    }

    private void cargarClubesHabituales() {
        if (currentUid == null) return;

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            List<Map<String, String>> reservas = AWSConnection.obtenerReservasUsuario(currentUid);
            Map<String, Integer> clubesMap = new HashMap<>();

            for (Map<String, String> res : reservas) {
                String club = res.get("club");
                if (club != null) {
                    clubesMap.put(club, clubesMap.getOrDefault(club, 0) + 1);
                }
            }

            List<String> topClubes = new ArrayList<>();
            clubesMap.entrySet().stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(5)
                    .forEach(e -> topClubes.add(e.getKey()));

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (!topClubes.isEmpty()) {
                        ClubesAdapter adapter = new ClubesAdapter(topClubes);
                        recyclerClubes.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private int getIndexInArray(int arrayId, String value) {
        String[] array = getResources().getStringArray(arrayId);
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return 0;
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
