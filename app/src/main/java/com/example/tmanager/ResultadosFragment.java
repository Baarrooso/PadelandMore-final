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

public class ResultadosFragment extends Fragment {

    private final List<String> resultados = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_resultados, container, false);

        db = FirebaseFirestore.getInstance();

        ListView listResultados = view.findViewById(R.id.listResultados);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, resultados);
        listResultados.setAdapter(adapter);

        cargarResultados();

        return view;
    }

    private void cargarResultados() {
        db.collection("resultados")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        resultados.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String resultado = document.get("equipo1") + " vs " +
                                    document.get("equipo2") + " - " +
                                    document.get("marcador");
                            resultados.add(resultado);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

