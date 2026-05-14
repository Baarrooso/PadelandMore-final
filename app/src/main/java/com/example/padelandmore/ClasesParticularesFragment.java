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

public class ClasesParticularesFragment extends Fragment {

    private final List<String> clases = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_clases_particulares, container, false);

        ListView listClases = view.findViewById(R.id.listClases);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, clases);
        listClases.setAdapter(adapter);

        cargarClases();

        return view;
    }

    private void cargarClases() {
        Backend.getCollection(requireContext(), "clases_particulares", resp -> {
            clases.clear();

            Object data = resp.get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (!(item instanceof Map)) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> document = (Map<String, Object>) item;
                    String entrenador = valorTexto(document.get("entrenador"), "Sin entrenador");
                    String horario = valorTexto(document.get("horario"), "Sin horario");
                    String precio = valorTexto(document.get("precio"), "0");
                    clases.add(entrenador + " - " + horario + " - $" + precio);
                }
            }

            if (clases.isEmpty()) {
                clases.add("No hay clases particulares disponibles");
            }

            adapter.notifyDataSetChanged();
        }, t -> {
            clases.clear();
            clases.add("Error cargando clases");
            adapter.notifyDataSetChanged();
        });
    }

    private String valorTexto(Object value, String fallback) {
        return value != null ? value.toString() : fallback;
    }
}

