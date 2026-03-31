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

public class TiendaFragment extends Fragment {

    private final List<String> productos = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tienda, container, false);

        db = FirebaseFirestore.getInstance();

        ListView listProductos = view.findViewById(R.id.listProductos);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, productos);
        listProductos.setAdapter(adapter);

        cargarProductos();

        return view;
    }

    private void cargarProductos() {
        db.collection("tienda")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String producto = document.get("nombre") + " - $" +
                                    document.get("precio");
                            productos.add(producto);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

