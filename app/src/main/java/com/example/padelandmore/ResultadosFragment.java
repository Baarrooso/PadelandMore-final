package com.example.padelandmore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.padelandmore.network.Backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultadosFragment extends Fragment {

    private final List<String> resultados = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_resultados, container, false);

        ListView listResultados = view.findViewById(R.id.listResultados);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, resultados);
        listResultados.setAdapter(adapter);

        cargarResultados();

        return view;
    }

    private void cargarResultados() {
        Backend.getCollection(requireContext(), "partidos", resp -> {
            resultados.clear();

            Object data = resp.get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (!(item instanceof Map)) continue;

                    @SuppressWarnings("unchecked")
                    Map<String, Object> document = (Map<String, Object>) item;
                    String jugador1 = texto(document.get("jugador1Nombre"), texto(document.get("jugador1Uid"), "Equipo 1"));
                    String jugador2 = texto(document.get("jugador2Nombre"), texto(document.get("jugador2Uid"), "Equipo 2"));
                    String marcador = texto(document.get("resultado"), "Pendiente");
                    resultados.add(jugador1 + " vs " + jugador2 + " - " + marcador);
                }
            }

            if (resultados.isEmpty()) {
                resultados.add("No hay resultados disponibles");
            }

            adapter.notifyDataSetChanged();
        }, t -> {
            resultados.clear();
            resultados.add("Error cargando resultados");
            adapter.notifyDataSetChanged();
        });
    }

    private String texto(Object value, String fallback) {
        return value != null && !value.toString().trim().isEmpty() ? value.toString() : fallback;
    }
}

