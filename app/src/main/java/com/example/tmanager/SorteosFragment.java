package com.example.tmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SorteosFragment extends Fragment {

    private final List<String> sorteos = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sorteos, container, false);

        db = FirebaseFirestore.getInstance();

        ListView listSorteos = view.findViewById(R.id.listSorteos);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sorteos);
        listSorteos.setAdapter(adapter);

        cargarSorteos();

        return view;
    }

    private void cargarSorteos() {
        db.collection("sorteos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sorteos.clear();
                        if (task.getResult().isEmpty()) {
                            sorteos.add("No hay sorteos disponibles en este momento");
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String nombre = document.getString("nombre");
                                String premio = document.getString("premio");
                                String fecha = document.getString("fecha");

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
                        adapter.notifyDataSetChanged();
                    } else {
                        sorteos.add("Error cargando sorteos");
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

