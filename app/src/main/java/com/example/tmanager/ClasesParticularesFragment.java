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

public class ClasesParticularesFragment extends Fragment {

    private final List<String> clases = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_clases_particulares, container, false);

        db = FirebaseFirestore.getInstance();

        ListView listClases = view.findViewById(R.id.listClases);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, clases);
        listClases.setAdapter(adapter);

        cargarClases();

        return view;
    }

    private void cargarClases() {
        db.collection("clases_particulares")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        clases.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String clase = document.get("entrenador") + " - " +
                                    document.get("horario") + " - $" +
                                    document.get("precio");
                            clases.add(clase);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

