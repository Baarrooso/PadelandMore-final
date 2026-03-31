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

public class BlogFragment extends Fragment {

    private final List<String> articulos = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_blog, container, false);

        db = FirebaseFirestore.getInstance();

        ListView listArticulos = view.findViewById(R.id.listArticulos);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, articulos);
        listArticulos.setAdapter(adapter);

        cargarArticulos();

        return view;
    }

    private void cargarArticulos() {
        db.collection("blog")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        articulos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String articulo = document.get("titulo") + " - " +
                                    document.get("autor");
                            articulos.add(articulo);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

