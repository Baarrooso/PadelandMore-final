package com.example.tmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MatchNivelFragment extends Fragment {

    private final List<String> jugadores = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private FirebaseFirestore db;
    private int nivelObjetivo = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_match_nivel, container, false);

        db = FirebaseFirestore.getInstance();

        TextView txtNivel = view.findViewById(R.id.txtNivelBuscado);
        SeekBar seekNivel = view.findViewById(R.id.seekNivelBuscado);
        EditText edtCiudad = view.findViewById(R.id.edtCiudadBuscada);
        Button btnBuscar = view.findViewById(R.id.btnBuscarNivel);
        ListView listJugadores = view.findViewById(R.id.listJugadoresNivel);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, jugadores);
        listJugadores.setAdapter(adapter);

        txtNivel.setText("Nivel buscado: " + nivelObjetivo);

        seekNivel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nivelObjetivo = Math.max(1, progress + 1);
                txtNivel.setText("Nivel buscado: " + nivelObjetivo);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnBuscar.setOnClickListener(v -> buscarJugadores(edtCiudad.getText().toString().trim()));
        buscarJugadores("");

        return view;
    }

    private void buscarJugadores(String ciudad) {
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(snapshot -> {
                    jugadores.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Long nivel = doc.getLong("nivelPadel");
                        String ciudadDoc = doc.getString("ciudadPadel");
                        String nombre = doc.getString("nombre");

                        if (nivel == null || nombre == null) {
                            continue;
                        }

                        boolean nivelCompatible = Math.abs(nivel.intValue() - nivelObjetivo) <= 1;
                        boolean ciudadCompatible = ciudad.isEmpty()
                                || (ciudadDoc != null && ciudadDoc.equalsIgnoreCase(ciudad));

                        if (nivelCompatible && ciudadCompatible) {
                            String jugador = nombre + " | Nivel " + nivel;
                            if (ciudadDoc != null && !ciudadDoc.isEmpty()) {
                                jugador += " | " + ciudadDoc;
                            }
                            jugadores.add(jugador);
                        }
                    }

                    if (jugadores.isEmpty()) {
                        jugadores.add("No hay jugadores para ese filtro. Prueba otro nivel o ciudad.");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "No se pudo buscar jugadores", Toast.LENGTH_SHORT).show()
                );
    }
}

