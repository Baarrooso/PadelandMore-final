package com.example.padelandmore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.padelandmore.network.Backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SorteosFragment extends Fragment {

    private final List<String> sorteos = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sorteos, container, false);

        ImageButton btnVolver = view.findViewById(R.id.btn_volver_sorteos);
        btnVolver.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        ListView listSorteos = view.findViewById(R.id.listSorteos);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sorteos);
        listSorteos.setAdapter(adapter);

        cargarSorteos();

        // Al pulsar un sorteo, el usuario puede apuntarse (si está logueado)
        listSorteos.setOnItemClickListener((parent, v, position, id) -> {
            if (sorteos.isEmpty()) return;

            String userUid = Backend.getCurrentUid(requireContext());
            if (userUid == null) {
                android.widget.Toast.makeText(requireContext(), "Debes iniciar sesión para apuntarte", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            String seleccionado = sorteos.get(position);

            java.util.HashMap<String, Object> data = new java.util.HashMap<>();
            data.put("userUid", userUid);
            data.put("sorteoResumen", seleccionado);
            data.put("creado", System.currentTimeMillis());

            Backend.postToCollection(requireContext(), "inscripciones_sorteos", data,
                    ref -> android.widget.Toast.makeText(requireContext(), "Inscripción enviada al sorteo", android.widget.Toast.LENGTH_SHORT).show(),
                    e -> android.widget.Toast.makeText(requireContext(), "No se pudo inscribir", android.widget.Toast.LENGTH_SHORT).show());
        });

        return view;
    }

    private void cargarSorteos() {
        Backend.getCollection(requireContext(), "sorteos", resp -> {
            sorteos.clear();

            Object data = resp.get("data");
            if (data instanceof List) {
                List<?> items = (List<?>) data;
                if (items.isEmpty()) {
                    sorteos.add("No hay sorteos disponibles en este momento");
                } else {
                    for (Object item : items) {
                        if (!(item instanceof Map)) {
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        Map<String, Object> document = (Map<String, Object>) item;
                        String nombre = valorTexto(document.get("nombre"), "Sorteo");
                        String premio = valorTexto(document.get("premio"), null);
                        String fecha = valorTexto(document.get("fecha"), null);

                        String sorteoInfo = nombre;
                        if (premio != null) {
                            sorteoInfo += " - Premio: " + premio;
                        }
                        if (fecha != null) {
                            sorteoInfo += " (" + fecha + ")";
                        }
                        sorteos.add(sorteoInfo);
                    }
                }
            }

            if (sorteos.isEmpty()) {
                sorteos.add("Error cargando sorteos");
            }

            adapter.notifyDataSetChanged();
        }, t -> {
            sorteos.clear();
            sorteos.add("Error cargando sorteos");
            adapter.notifyDataSetChanged();
        });
    }

    private String valorTexto(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String texto = value.toString();
        return texto.isEmpty() ? fallback : texto;
    }
}

