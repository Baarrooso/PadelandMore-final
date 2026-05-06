package com.example.tmanager;

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

        ImageButton btnVolver = view.findViewById(R.id.btn_volver_sorteos);
        btnVolver.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        db = FirebaseFirestore.getInstance();

        ListView listSorteos = view.findViewById(R.id.listSorteos);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sorteos);
        listSorteos.setAdapter(adapter);

        cargarSorteos();

        // Al pulsar un sorteo, el usuario puede apuntarse (si está logueado)
        listSorteos.setOnItemClickListener((parent, v, position, id) -> {
            if (sorteos.isEmpty()) return;

            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                android.widget.Toast.makeText(requireContext(), "Debes iniciar sesión para apuntarte", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            String seleccionado = sorteos.get(position);

            FirebaseFirestore.getInstance()
                    .collection("inscripciones_sorteos")
                    .add(new java.util.HashMap<String, Object>() {{
                        put("userUid", user.getUid());
                        put("sorteoResumen", seleccionado);
                    }})
                    .addOnSuccessListener(ref ->
                            android.widget.Toast.makeText(requireContext(), "Inscripción enviada al sorteo", android.widget.Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            android.widget.Toast.makeText(requireContext(), "No se pudo inscribir", android.widget.Toast.LENGTH_SHORT).show()
                    );
        });

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

