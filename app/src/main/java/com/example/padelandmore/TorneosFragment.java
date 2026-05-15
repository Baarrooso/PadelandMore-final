package com.example.padelandmore;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.padelandmore.network.AWSConnection;
import com.example.padelandmore.network.Backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TorneosFragment extends Fragment {

    private final List<String> torneos = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_torneos, container, false);

        ImageButton btnVolver = view.findViewById(R.id.btn_volver_torneos);
        btnVolver.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        ListView listTorneos = view.findViewById(R.id.listTorneos);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, torneos);
        listTorneos.setAdapter(adapter);

        cargarTorneos();

        listTorneos.setOnItemClickListener((parent, v, position, id) -> apuntarseATorneo(position));

        com.google.android.material.floatingactionbutton.FloatingActionButton fabCrearTorneo =
                view.findViewById(R.id.fabCrearTorneo);

        AuthUtil.isJugador(requireContext(), isJugador -> {
            if (isJugador) {
                fabCrearTorneo.setVisibility(View.GONE);
                fabCrearTorneo.hide();
            } else {
                fabCrearTorneo.setVisibility(View.VISIBLE);
                fabCrearTorneo.show();
                fabCrearTorneo.setOnClickListener(v -> mostrarDialogoTorneo());
            }
        });

        return view;
    }

    private void mostrarDialogoTorneo() {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_torneo_padel, null, false);

        EditText edtNombre = form.findViewById(R.id.edtNombreTorneo);
        EditText edtCiudad = form.findViewById(R.id.edtCiudadTorneo);
        EditText edtFecha = form.findViewById(R.id.edtFechaTorneo);
        EditText edtNivel = form.findViewById(R.id.edtNivelTorneo);

        new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo torneo")
                .setView(form)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombre = edtNombre.getText().toString().trim();
                    String ciudad = edtCiudad.getText().toString().trim();
                    String fecha = edtFecha.getText().toString().trim();
                    String nivel = edtNivel.getText().toString().trim();

                    if (nombre.isEmpty() || ciudad.isEmpty() || fecha.isEmpty() || nivel.isEmpty()) {
                        Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    crearTorneo(nombre, ciudad, fecha, nivel);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearTorneo(String nombre, String ciudad, String fecha, String nivel) {
        long timestamp = System.currentTimeMillis();

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = AWSConnection.insertarTorneo(nombre, ciudad, fecha, nivel, timestamp, timestamp);

            requireActivity().runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(requireContext(), "Torneo creado", Toast.LENGTH_SHORT).show();
                    cargarTorneos();
                } else {
                    Toast.makeText(requireContext(), "Error al guardar el torneo", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void cargarTorneos() {
        Backend.getCollection(requireContext(), "torneos_padel", resp -> {
            torneos.clear();
            Object data = resp.get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (!(item instanceof Map)) continue;
                    Map<?, ?> doc = (Map<?, ?>) item;
                    String nombre = valueOf(doc.get("nombre"));
                    String ciudad = valueOf(doc.get("ciudad"));
                    String fecha = valueOf(doc.get("fecha"));
                    String nivel = valueOf(doc.get("nivel"));
                    String linea = (nombre == null ? "Torneo" : nombre)
                            + "\n" + (ciudad == null ? "Ciudad" : ciudad)
                            + " | " + (fecha == null ? "Fecha" : fecha)
                            + " | Nivel " + (nivel == null ? "N/A" : nivel);
                    torneos.add(linea);
                }
            }
            adapter.notifyDataSetChanged();
        }, t -> Toast.makeText(requireContext(), "Error cargando torneos", Toast.LENGTH_SHORT).show());
    }

    private void apuntarseATorneo(int position) {
        if (torneos.isEmpty()) return;
        String uid = Backend.getCurrentUid(requireContext());
        if (uid == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesion", Toast.LENGTH_SHORT).show();
            return;
        }
        String seleccionado = torneos.get(position);
        Map<String, Object> data = new HashMap<>();
        data.put("userUid", uid);
        data.put("torneoResumen", seleccionado);
        data.put("creado", System.currentTimeMillis());
        Backend.postToCollection(requireContext(), "inscripciones_torneo_padel", data,
                resp -> Toast.makeText(requireContext(), "Inscripcion enviada", Toast.LENGTH_SHORT).show(),
                t -> Toast.makeText(requireContext(), "No se pudo inscribir", Toast.LENGTH_SHORT).show());
    }

    private String valueOf(Object value) {
        return value == null ? null : value.toString();
    }
}
